package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_runs")
data class GameRunEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val score: Int,
  val snakeLength: Int,
  val foodEaten: Int,
  val speedTierName: String,
  val durationSeconds: Int,
  val playedAtMillis: Long = System.currentTimeMillis(),
)

data class AggregateStatsRow(
  val bestScore: Int,
  val totalGames: Int,
  val totalFoodEaten: Int,
  val longestSnake: Int,
)
