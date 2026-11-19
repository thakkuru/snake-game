package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundPlayer
import com.example.data.GameRunEntity
import com.example.data.SnakeRepository
import com.example.domain.SnakeGameEngine
import com.example.model.AggregateStats
import com.example.model.Direction
import com.example.model.FeedbackEvent
import com.example.model.GamePhase
import com.example.model.GameUiState
import com.example.model.LcdPalette
import com.example.model.SpeedTier
import com.example.model.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SnakeViewModel(
  private val repository: SnakeRepository,
  private val soundPlayer: SoundPlayer,
  private val engine: SnakeGameEngine = SnakeGameEngine(),
) : ViewModel() {

  val preferences: StateFlow<UserPreferences> =
    repository.preferences.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = UserPreferences(),
    )

  val stats: StateFlow<AggregateStats> =
    repository.stats.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = AggregateStats(),
    )

  val topRuns: StateFlow<List<GameRunEntity>> =
    repository.topRuns.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList(),
    )

  private val _gameState =
    MutableStateFlow(engine.createReadyState(bestScore = 0, speedTier = SpeedTier.CLASSIC))
  val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

  private val _feedbackEvents = MutableSharedFlow<FeedbackEvent>(extraBufferCapacity = 16)
  val feedbackEvents: SharedFlow<FeedbackEvent> = _feedbackEvents.asSharedFlow()

  private val directionQueue = ArrayDeque<Direction>(2)
  private var tickJob: Job? = null

  init {
    viewModelScope.launch {
      combine(stats, preferences) { currentStats, currentPrefs ->
        currentStats to currentPrefs
      }.collect { (currentStats, currentPrefs) ->
        val current = _gameState.value
        val updatedBest = maxOf(current.bestScore, currentStats.bestScore)
        if (current.phase == GamePhase.READY) {
          _gameState.value =
            engine.createReadyState(bestScore = currentStats.bestScore, speedTier = currentPrefs.speedTier)
        } else if (updatedBest != current.bestScore) {
          _gameState.value = current.copy(bestScore = updatedBest)
        }
      }
    }
  }

  fun startGame(initialDirection: Direction = Direction.RIGHT) {
    directionQueue.clear()
    val best = maxOf(stats.value.bestScore, _gameState.value.bestScore)
    val speed = preferences.value.speedTier
    _gameState.value =
      engine.startPlayingState(
        bestScore = best,
        speedTier = speed,
        initialDirection = initialDirection,
      )
    emitFeedback(FeedbackEvent.TURN)
    startTickLoop()
  }

  fun onSwipeDirection(swipeDirection: Direction) {
    val current = _gameState.value
    when (current.phase) {
      GamePhase.READY -> startGame(initialDirection = swipeDirection)
      GamePhase.PLAYING -> {
        val lastPlanned = directionQueue.lastOrNull() ?: current.direction
        if (
          swipeDirection != lastPlanned &&
            !swipeDirection.isOpposite(lastPlanned) &&
            directionQueue.size < 2
        ) {
          directionQueue.addLast(swipeDirection)
        }
      }
      GamePhase.PAUSED,
      GamePhase.GAME_OVER -> Unit
    }
  }

  fun pauseGame() {
    if (_gameState.value.phase == GamePhase.PLAYING) {
      tickJob?.cancel()
      tickJob = null
      _gameState.value = _gameState.value.copy(phase = GamePhase.PAUSED)
    }
  }

  fun resumeGame() {
    if (_gameState.value.phase == GamePhase.PAUSED) {
      _gameState.value = _gameState.value.copy(phase = GamePhase.PLAYING)
      startTickLoop()
    }
  }

  fun restartGame() {
    startGame(Direction.RIGHT)
  }

  fun returnToTitle() {
    tickJob?.cancel()
    tickJob = null
    directionQueue.clear()
    _gameState.value =
      engine.createReadyState(
        bestScore = stats.value.bestScore,
        speedTier = preferences.value.speedTier,
      )
  }

  fun selectSpeedTier(speedTier: SpeedTier) {
    viewModelScope.launch {
      repository.setSpeedTier(speedTier)
    }
    if (_gameState.value.phase == GamePhase.READY) {
      _gameState.value = _gameState.value.copy(speedTier = speedTier)
    }
  }

  fun setLcdPalette(palette: LcdPalette) {
    viewModelScope.launch { repository.setLcdPalette(palette) }
  }

  fun setSoundEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.setSoundEnabled(enabled) }
  }

  fun setHapticsEnabled(enabled: Boolean) {
    viewModelScope.launch { repository.setHapticsEnabled(enabled) }
  }

  fun clearAllRecords() {
    viewModelScope.launch {
      repository.clearAllRecords()
      if (_gameState.value.phase == GamePhase.READY) {
        _gameState.value = _gameState.value.copy(bestScore = 0)
      }
    }
  }

  private fun startTickLoop() {
    tickJob?.cancel()
    tickJob =
      viewModelScope.launch {
        while (isActive && _gameState.value.phase == GamePhase.PLAYING) {
          val current = _gameState.value
          val tickDelayMs = engine.calculateTickDelayMs(current.speedTier, current.foodEaten)
          delay(tickDelayMs)
          if (!isActive || _gameState.value.phase != GamePhase.PLAYING) break

          val latest = _gameState.value
          val queuedDirection = directionQueue.removeFirstOrNull()
          val turned =
            queuedDirection != null &&
              queuedDirection != latest.direction &&
              !queuedDirection.isOpposite(latest.direction)

          val stepResult = engine.step(latest, queuedDirection, tickDelayMs)
          _gameState.value = stepResult.state

          when {
            stepResult.collided -> {
              emitFeedback(FeedbackEvent.GAME_OVER)
              persistCompletedRun(stepResult.state)
              break
            }
            stepResult.reachedNewHighScore -> emitFeedback(FeedbackEvent.NEW_RECORD)
            stepResult.ateFood -> emitFeedback(FeedbackEvent.EAT)
            turned -> emitFeedback(FeedbackEvent.TURN)
          }
        }
      }
  }

  private fun persistCompletedRun(finalState: GameUiState) {
    val durationSec = (finalState.elapsedMillis / 1000L).toInt().coerceAtLeast(1)
    viewModelScope.launch {
      repository.recordRun(
        score = finalState.score,
        snakeLength = finalState.snake.size,
        foodEaten = finalState.foodEaten,
        speedTier = finalState.speedTier,
        durationSeconds = durationSec,
      )
    }
  }

  private fun emitFeedback(event: FeedbackEvent) {
    if (preferences.value.soundEnabled) {
      soundPlayer.play(event)
    }
    _feedbackEvents.tryEmit(event)
  }

  override fun onCleared() {
    tickJob?.cancel()
    super.onCleared()
  }

  class Factory(
    private val repository: SnakeRepository,
    private val soundPlayer: SoundPlayer,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return SnakeViewModel(repository = repository, soundPlayer = soundPlayer) as T
    }
  }
}
