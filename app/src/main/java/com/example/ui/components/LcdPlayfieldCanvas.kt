package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.model.Direction
import com.example.model.GamePhase
import com.example.model.GameUiState
import com.example.ui.theme.LocalLcdPalette

@Composable
fun LcdPlayfieldCanvas(
  gameState: GameUiState,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  val playfieldDescription = stringResource(R.string.cd_lcd_playfield)

  Canvas(
    modifier =
      modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .semantics { contentDescription = playfieldDescription }
  ) {
    val gridCount = gameState.gridSize
    val frameStroke = size.minDimension * 0.014f
    val boardLeft = frameStroke * 1.4f
    val boardTop = frameStroke * 1.4f
    val boardSize = size.minDimension - (frameStroke * 2.8f)
    val cellSize = boardSize / gridCount

    // Outer solid LCD perimeter wall
    drawRect(
      color = palette.screenBorder,
      topLeft = Offset(frameStroke * 0.5f, frameStroke * 0.5f),
      size = Size(size.minDimension - frameStroke, size.minDimension - frameStroke),
      style = Stroke(width = frameStroke),
    )

    // Subtle dot-matrix grid cells (inactive LCD segments)
    val cellInset = cellSize * 0.10f
    val innerCell = cellSize - (cellInset * 2f)
    for (y in 0 until gridCount) {
      for (x in 0 until gridCount) {
        val left = boardLeft + x * cellSize + cellInset
        val top = boardTop + y * cellSize + cellInset
        drawRect(
          color = palette.screenGridDot,
          topLeft = Offset(left, top),
          size = Size(innerCell, innerCell),
        )
      }
    }

    // Food pellet: classic 4-dot diamond pixel cluster that pulses on alternate ticks
    val food = gameState.food
    val foodLeft = boardLeft + food.x * cellSize
    val foodTop = boardTop + food.y * cellSize
    val dotSize = cellSize * 0.30f
    val blinkOn =
      gameState.phase != GamePhase.PLAYING || (gameState.tickCount % 4L != 3L)
    if (blinkOn) {
      val foodColor = palette.pixelInk
      val shadowOffset = cellSize * 0.08f
      val dotCenters =
        listOf(
          Offset(foodLeft + cellSize * 0.5f, foodTop + cellSize * 0.20f),
          Offset(foodLeft + cellSize * 0.5f, foodTop + cellSize * 0.80f),
          Offset(foodLeft + cellSize * 0.20f, foodTop + cellSize * 0.5f),
          Offset(foodLeft + cellSize * 0.80f, foodTop + cellSize * 0.5f),
        )
      dotCenters.forEach { center ->
        drawRect(
          color = palette.pixelShadow,
          topLeft =
            Offset(
              center.x - dotSize * 0.5f + shadowOffset,
              center.y - dotSize * 0.5f + shadowOffset,
            ),
          size = Size(dotSize, dotSize),
        )
        drawRect(
          color = foodColor,
          topLeft = Offset(center.x - dotSize * 0.5f, center.y - dotSize * 0.5f),
          size = Size(dotSize, dotSize),
        )
      }
    }

    // Snake segments from tail to head
    val snake = gameState.snake
    val shadowShift = cellSize * 0.09f
    for (index in snake.indices.reversed()) {
      val segment = snake[index]
      val isHead = index == 0
      val isTail = index == snake.lastIndex
      val segInset =
        when {
          isHead -> cellSize * 0.05f
          isTail -> cellSize * 0.16f
          else -> cellSize * 0.09f
        }
      val segSize = cellSize - (segInset * 2f)
      val segLeft = boardLeft + segment.x * cellSize + segInset
      val segTop = boardTop + segment.y * cellSize + segInset

      val isFlashingEat = isHead && gameState.eatFlashTicks > 0
      val segColor =
        when {
          isFlashingEat -> palette.pixelInkMid
          isHead -> palette.pixelInk
          index % 2 == 1 -> palette.pixelInk
          else -> palette.pixelInkMid
        }

      // LCD reflective pixel shadow
      drawRect(
        color = palette.pixelShadow,
        topLeft = Offset(segLeft + shadowShift, segTop + shadowShift),
        size = Size(segSize, segSize),
      )

      // Main pixel segment block
      drawRect(
        color = segColor,
        topLeft = Offset(segLeft, segTop),
        size = Size(segSize, segSize),
      )

      // Subtle inner highlight pixel on head & eyes facing direction
      if (isHead) {
        val eyeSize = segSize * 0.22f
        val (eye1, eye2) =
          when (gameState.direction) {
            Direction.RIGHT ->
              Pair(
                Offset(segLeft + segSize * 0.62f, segTop + segSize * 0.18f),
                Offset(segLeft + segSize * 0.62f, segTop + segSize * 0.60f),
              )
            Direction.LEFT ->
              Pair(
                Offset(segLeft + segSize * 0.16f, segTop + segSize * 0.18f),
                Offset(segLeft + segSize * 0.16f, segTop + segSize * 0.60f),
              )
            Direction.UP ->
              Pair(
                Offset(segLeft + segSize * 0.18f, segTop + segSize * 0.16f),
                Offset(segLeft + segSize * 0.60f, segTop + segSize * 0.16f),
              )
            Direction.DOWN ->
              Pair(
                Offset(segLeft + segSize * 0.18f, segTop + segSize * 0.62f),
                Offset(segLeft + segSize * 0.60f, segTop + segSize * 0.62f),
              )
          }
        drawRect(
          color = palette.screenBg,
          topLeft = eye1,
          size = Size(eyeSize, eyeSize),
        )
        drawRect(
          color = palette.screenBg,
          topLeft = eye2,
          size = Size(eyeSize, eyeSize),
        )
      }
    }
  }
}
