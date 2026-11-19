package com.example

import com.example.domain.SnakeGameEngine
import com.example.model.CollisionReason
import com.example.model.Direction
import com.example.model.GRID_SIZE
import com.example.model.GamePhase
import com.example.model.GameUiState
import com.example.model.GridPoint
import com.example.model.SpeedTier
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnakeGameEngineTest {

  private val engine = SnakeGameEngine(Random(42))

  @Test
  fun `solid outer wall collision triggers game over`() {
    val state =
      GameUiState(
        gridSize = GRID_SIZE,
        phase = GamePhase.PLAYING,
        snake = listOf(GridPoint(GRID_SIZE - 1, 5), GridPoint(GRID_SIZE - 2, 5)),
        food = GridPoint(2, 2),
        direction = Direction.RIGHT,
        speedTier = SpeedTier.CLASSIC,
      )

    val result = engine.step(state, nextDirection = null, tickDurationMs = 130L)

    assertTrue(result.collided)
    assertEquals(GamePhase.GAME_OVER, result.state.phase)
    assertEquals(CollisionReason.WALL, result.state.collisionReason)
  }

  @Test
  fun `eating food grows snake increments score and marks new high score`() {
    val state =
      GameUiState(
        gridSize = GRID_SIZE,
        phase = GamePhase.PLAYING,
        snake = listOf(GridPoint(5, 5), GridPoint(4, 5), GridPoint(3, 5)),
        food = GridPoint(6, 5),
        direction = Direction.RIGHT,
        score = 0,
        bestScore = 10,
        speedTier = SpeedTier.CLASSIC, // 15 points per food
      )

    val result = engine.step(state, nextDirection = null, tickDurationMs = 130L)

    assertTrue(result.ateFood)
    assertFalse(result.collided)
    assertTrue(result.reachedNewHighScore)
    assertTrue(result.state.isNewHighScore)
    assertEquals(4, result.state.snake.size)
    assertEquals(15, result.state.score)
    assertEquals(15, result.state.bestScore)
    assertFalse(result.state.food in result.state.snake)
  }

  @Test
  fun `opposite direction input is ignored to prevent instant reversal`() {
    val state =
      GameUiState(
        gridSize = GRID_SIZE,
        phase = GamePhase.PLAYING,
        snake = listOf(GridPoint(5, 5), GridPoint(4, 5), GridPoint(3, 5)),
        food = GridPoint(12, 12),
        direction = Direction.RIGHT,
        speedTier = SpeedTier.CLASSIC,
      )

    val result = engine.step(state, nextDirection = Direction.LEFT, tickDurationMs = 130L)

    assertFalse(result.collided)
    assertEquals(Direction.RIGHT, result.state.direction)
    assertEquals(GridPoint(6, 5), result.state.snake.first())
  }

  @Test
  fun `moving onto vacating tail tip without eating is safe`() {
    val state =
      GameUiState(
        gridSize = GRID_SIZE,
        phase = GamePhase.PLAYING,
        snake =
          listOf(
            GridPoint(5, 5),
            GridPoint(6, 5),
            GridPoint(6, 6),
            GridPoint(5, 6),
          ),
        food = GridPoint(15, 15),
        direction = Direction.LEFT,
        speedTier = SpeedTier.CLASSIC,
      )

    val result = engine.step(state, nextDirection = Direction.DOWN, tickDurationMs = 130L)

    assertFalse(result.collided)
    assertEquals(GamePhase.PLAYING, result.state.phase)
    assertEquals(GridPoint(5, 6), result.state.snake.first())
  }

  @Test
  fun `self collision ends game with SELF reason`() {
    val state =
      GameUiState(
        gridSize = GRID_SIZE,
        phase = GamePhase.PLAYING,
        snake =
          listOf(
            GridPoint(5, 5),
            GridPoint(6, 5),
            GridPoint(6, 6),
            GridPoint(5, 6),
            GridPoint(4, 6),
          ),
        food = GridPoint(15, 15),
        direction = Direction.LEFT,
        speedTier = SpeedTier.CLASSIC,
      )

    val result = engine.step(state, nextDirection = Direction.DOWN, tickDurationMs = 130L)

    assertTrue(result.collided)
    assertEquals(GamePhase.GAME_OVER, result.state.phase)
    assertEquals(CollisionReason.SELF, result.state.collisionReason)
  }
}
