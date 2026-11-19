package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRunDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRun(run: GameRunEntity)

  @Query("SELECT * FROM game_runs ORDER BY score DESC, playedAtMillis DESC LIMIT :limit")
  fun getTopRuns(limit: Int = 25): Flow<List<GameRunEntity>>

  @Query(
    """
    SELECT
      COALESCE(MAX(score), 0) AS bestScore,
      COUNT(*) AS totalGames,
      COALESCE(SUM(foodEaten), 0) AS totalFoodEaten,
      COALESCE(MAX(snakeLength), 0) AS longestSnake
    FROM game_runs
    """
  )
  fun getAggregateStats(): Flow<AggregateStatsRow>

  @Query("DELETE FROM game_runs")
  suspend fun clearAllRuns()
}
