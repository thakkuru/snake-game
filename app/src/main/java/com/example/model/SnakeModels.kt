package com.example.model

import androidx.compose.ui.graphics.Color

const val GRID_SIZE = 20

data class GridPoint(val x: Int, val y: Int)

enum class Direction {
  UP,
  DOWN,
  LEFT,
  RIGHT;

  fun isOpposite(other: Direction): Boolean =
    when (this) {
      UP -> other == DOWN
      DOWN -> other == UP
      LEFT -> other == RIGHT
      RIGHT -> other == LEFT
    }

  fun delta(): GridPoint =
    when (this) {
      UP -> GridPoint(0, -1)
      DOWN -> GridPoint(0, 1)
      LEFT -> GridPoint(-1, 0)
      RIGHT -> GridPoint(1, 0)
    }
}

enum class SpeedTier(
  val storageKey: String,
  val baseTickMs: Long,
  val minTickMs: Long,
  val speedStepMs: Long,
  val pointsPerFood: Int,
) {
  CHILL(
    storageKey = "CHILL",
    baseTickMs = 175L,
    minTickMs = 105L,
    speedStepMs = 2L,
    pointsPerFood = 10,
  ),
  CLASSIC(
    storageKey = "CLASSIC",
    baseTickMs = 130L,
    minTickMs = 72L,
    speedStepMs = 2L,
    pointsPerFood = 15,
  ),
  TURBO(
    storageKey = "TURBO",
    baseTickMs = 92L,
    minTickMs = 54L,
    speedStepMs = 1L,
    pointsPerFood = 25,
  );

  companion object {
    fun fromKey(key: String?): SpeedTier =
      entries.firstOrNull { it.storageKey == key } ?: CLASSIC
  }
}

enum class LcdPalette(
  val storageKey: String,
  val screenBg: Color,
  val screenGridDot: Color,
  val pixelInk: Color,
  val pixelInkMid: Color,
  val pixelShadow: Color,
  val screenBorder: Color,
) {
  CLASSIC_OLIVE(
    storageKey = "CLASSIC_OLIVE",
    screenBg = Color(0xFFB5C46D),
    screenGridDot = Color(0xFFA2B35A),
    pixelInk = Color(0xFF142E14),
    pixelInkMid = Color(0xFF335726),
    pixelShadow = Color(0x2E142E14),
    screenBorder = Color(0xFF28421E),
  ),
  POCKET_AMBER(
    storageKey = "POCKET_AMBER",
    screenBg = Color(0xFFE5B153),
    screenGridDot = Color(0xFFD19B3D),
    pixelInk = Color(0xFF331B04),
    pixelInkMid = Color(0xFF5C350D),
    pixelShadow = Color(0x2E331B04),
    screenBorder = Color(0xFF472708),
  ),
  BACKLIT_TEAL(
    storageKey = "BACKLIT_TEAL",
    screenBg = Color(0xFF79CBBF),
    screenGridDot = Color(0xFF64B5A9),
    pixelInk = Color(0xFF0A2A2E),
    pixelInkMid = Color(0xFF1A4B52),
    pixelShadow = Color(0x2E0A2A2E),
    screenBorder = Color(0xFF143C42),
  );

  companion object {
    fun fromKey(key: String?): LcdPalette =
      entries.firstOrNull { it.storageKey == key } ?: CLASSIC_OLIVE
  }
}

enum class GamePhase {
  READY,
  PLAYING,
  PAUSED,
  GAME_OVER,
}

enum class CollisionReason {
  NONE,
  WALL,
  SELF,
}

enum class FeedbackEvent {
  TURN,
  EAT,
  GAME_OVER,
  NEW_RECORD,
}

data class GameUiState(
  val gridSize: Int = GRID_SIZE,
  val phase: GamePhase = GamePhase.READY,
  val snake: List<GridPoint> = emptyList(),
  val food: GridPoint = GridPoint(14, 10),
  val direction: Direction = Direction.RIGHT,
  val score: Int = 0,
  val bestScore: Int = 0,
  val foodEaten: Int = 0,
  val speedTier: SpeedTier = SpeedTier.CLASSIC,
  val isNewHighScore: Boolean = false,
  val collisionReason: CollisionReason = CollisionReason.NONE,
  val tickCount: Long = 0L,
  val eatFlashTicks: Int = 0,
  val elapsedMillis: Long = 0L,
)

data class UserPreferences(
  val lcdPalette: LcdPalette = LcdPalette.CLASSIC_OLIVE,
  val speedTier: SpeedTier = SpeedTier.CLASSIC,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
)

data class AggregateStats(
  val bestScore: Int = 0,
  val totalGames: Int = 0,
  val totalFoodEaten: Int = 0,
  val longestSnake: Int = 0,
)
