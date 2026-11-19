package com.example.domain

import com.example.model.CollisionReason
import com.example.model.Direction
import com.example.model.GRID_SIZE
import com.example.model.GamePhase
import com.example.model.GameUiState
import com.example.model.GridPoint
import com.example.model.SpeedTier
import kotlin.random.Random

data class StepResult(
  val state: GameUiState,
  val ateFood: Boolean,
  val collided: Boolean,
  val reachedNewHighScore: Boolean,
)

class SnakeGameEngine(private val random: Random = Random.Default) {

  fun createReadyState(bestScore: Int, speedTier: SpeedTier): GameUiState {
    val startSnake =
      listOf(
        GridPoint(10, 10),
        GridPoint(9, 10),
        GridPoint(8, 10),
      )
    return GameUiState(
      gridSize = GRID_SIZE,
      phase = GamePhase.READY,
      snake = startSnake,
      food = GridPoint(15, 10),
      direction = Direction.RIGHT,
      score = 0,
      bestScore = bestScore,
      foodEaten = 0,
      speedTier = speedTier,
      isNewHighScore = false,
      collisionReason = CollisionReason.NONE,
      tickCount = 0L,
      eatFlashTicks = 0,
      elapsedMillis = 0L,
    )
  }

  fun startPlayingState(bestScore: Int, speedTier: SpeedTier, initialDirection: Direction): GameUiState {
    val startSnake =
      listOf(
        GridPoint(10, 10),
        GridPoint(9, 10),
        GridPoint(8, 10),
      )
    val safeDirection = if (initialDirection == Direction.LEFT) Direction.RIGHT else initialDirection
    val food = spawnFood(startSnake, GRID_SIZE)
    return GameUiState(
      gridSize = GRID_SIZE,
      phase = GamePhase.PLAYING,
      snake = startSnake,
      food = food,
      direction = safeDirection,
      score = 0,
      bestScore = bestScore,
      foodEaten = 0,
      speedTier = speedTier,
      isNewHighScore = false,
      collisionReason = CollisionReason.NONE,
      tickCount = 0L,
      eatFlashTicks = 0,
      elapsedMillis = 0L,
    )
  }

  fun step(state: GameUiState, nextDirection: Direction?, tickDurationMs: Long): StepResult {
    if (state.phase != GamePhase.PLAYING || state.snake.isEmpty()) {
      return StepResult(state, ateFood = false, collided = false, reachedNewHighScore = false)
    }

    val activeDirection =
      if (nextDirection != null && !nextDirection.isOpposite(state.direction)) {
        nextDirection
      } else {
        state.direction
      }

    val head = state.snake.first()
    val delta = activeDirection.delta()
    val nextHead = GridPoint(head.x + delta.x, head.y + delta.y)

    // Classic solid outer wall collision check
    if (nextHead.x !in 0 until state.gridSize || nextHead.y !in 0 until state.gridSize) {
      return StepResult(
        state =
          state.copy(
            phase = GamePhase.GAME_OVER,
            direction = activeDirection,
            collisionReason = CollisionReason.WALL,
            tickCount = state.tickCount + 1,
          ),
        ateFood = false,
        collided = true,
        reachedNewHighScore = false,
      )
    }

    val willEat = nextHead == state.food
    // Moving into the current tail tip is legal when not growing because the tail vacates it
    val bodyToCheck = if (willEat) state.snake else state.snake.dropLast(1)
    if (nextHead in bodyToCheck) {
      return StepResult(
        state =
          state.copy(
            phase = GamePhase.GAME_OVER,
            direction = activeDirection,
            collisionReason = CollisionReason.SELF,
            tickCount = state.tickCount + 1,
          ),
        ateFood = false,
        collided = true,
        reachedNewHighScore = false,
      )
    }

    val updatedSnake =
      if (willEat) {
        listOf(nextHead) + state.snake
      } else {
        listOf(nextHead) + state.snake.dropLast(1)
      }

    val updatedFoodEaten = if (willEat) state.foodEaten + 1 else state.foodEaten
    val updatedScore =
      if (willEat) state.score + state.speedTier.pointsPerFood else state.score
    val beatBestBefore = state.isNewHighScore
    val isNowNewBest = updatedScore > state.bestScore && updatedScore > 0
    val updatedBestScore = maxOf(state.bestScore, updatedScore)
    val nextFood = if (willEat) spawnFood(updatedSnake, state.gridSize) else state.food
    val nextEatFlash = if (willEat) 2 else maxOf(0, state.eatFlashTicks - 1)

    val nextState =
      state.copy(
        snake = updatedSnake,
        food = nextFood,
        direction = activeDirection,
        score = updatedScore,
        bestScore = updatedBestScore,
        foodEaten = updatedFoodEaten,
        isNewHighScore = beatBestBefore || isNowNewBest,
        tickCount = state.tickCount + 1,
        eatFlashTicks = nextEatFlash,
        elapsedMillis = state.elapsedMillis + tickDurationMs,
      )

    return StepResult(
      state = nextState,
      ateFood = willEat,
      collided = false,
      reachedNewHighScore = !beatBestBefore && isNowNewBest,
    )
  }

  fun calculateTickDelayMs(speedTier: SpeedTier, foodEaten: Int): Long {
    val reduction = foodEaten * speedTier.speedStepMs
    return maxOf(speedTier.minTickMs, speedTier.baseTickMs - reduction)
  }

  fun spawnFood(snake: List<GridPoint>, gridSize: Int): GridPoint {
    val occupied = snake.toHashSet()
    val freeCells = ArrayList<GridPoint>(gridSize * gridSize)
    for (y in 0 until gridSize) {
      for (x in 0 until gridSize) {
        val candidate = GridPoint(x, y)
        if (candidate !in occupied) {
          freeCells.add(candidate)
        }
      }
    }
    if (freeCells.isEmpty()) {
      return snake.last()
    }
    return freeCells[random.nextInt(freeCells.size)]
  }
}
