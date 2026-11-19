package com.example.data

import com.example.model.AggregateStats
import com.example.model.LcdPalette
import com.example.model.SpeedTier
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SnakeRepository(
  private val gameRunDao: GameRunDao,
  private val preferencesDataStore: PreferencesDataStore,
) {
  val topRuns: Flow<List<GameRunEntity>> = gameRunDao.getTopRuns()

  val stats: Flow<AggregateStats> =
    gameRunDao.getAggregateStats().map { row ->
      AggregateStats(
        bestScore = row.bestScore,
        totalGames = row.totalGames,
        totalFoodEaten = row.totalFoodEaten,
        longestSnake = row.longestSnake,
      )
    }

  val preferences: Flow<UserPreferences> = preferencesDataStore.preferences

  suspend fun recordRun(
    score: Int,
    snakeLength: Int,
    foodEaten: Int,
    speedTier: SpeedTier,
    durationSeconds: Int,
  ) {
    gameRunDao.insertRun(
      GameRunEntity(
        score = score,
        snakeLength = snakeLength,
        foodEaten = foodEaten,
        speedTierName = speedTier.storageKey,
        durationSeconds = durationSeconds,
      )
    )
  }

  suspend fun clearAllRecords() {
    gameRunDao.clearAllRuns()
  }

  suspend fun setLcdPalette(palette: LcdPalette) {
    preferencesDataStore.setLcdPalette(palette)
  }

  suspend fun setSpeedTier(speedTier: SpeedTier) {
    preferencesDataStore.setSpeedTier(speedTier)
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    preferencesDataStore.setSoundEnabled(enabled)
  }

  suspend fun setHapticsEnabled(enabled: Boolean) {
    preferencesDataStore.setHapticsEnabled(enabled)
  }
}
