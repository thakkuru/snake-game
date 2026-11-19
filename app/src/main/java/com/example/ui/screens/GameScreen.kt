package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Direction
import com.example.model.GamePhase
import com.example.model.GameUiState
import com.example.model.SpeedTier
import com.example.ui.components.ConsoleBezelScreen
import com.example.ui.components.ConsoleChassisBackground
import com.example.ui.components.ConsoleHardwareButton
import com.example.ui.components.ConsoleSpeakerGrille
import com.example.ui.components.LcdGameOverOverlay
import com.example.ui.components.LcdPausedOverlay
import com.example.ui.components.LcdPlayfieldCanvas
import com.example.ui.components.LcdReadyOverlay
import com.example.ui.theme.ConsoleShellPuttyShadow
import com.example.ui.theme.ConsoleTextInk
import com.example.ui.theme.ConsoleTextMuted
import com.example.ui.theme.LocalLcdPalette
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GameScreen(
  gameState: GameUiState,
  onSwipeDirection: (Direction) -> Unit,
  onStartClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
  onRestartClick: () -> Unit,
  onTitleClick: () -> Unit,
  onSelectSpeedTier: (SpeedTier) -> Unit,
  onOpenRecords: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  val swipeThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }
  val shakeOffsetPx = remember { Animatable(0f) }
  var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }

  LaunchedEffect(gameState.phase) {
    if (gameState.phase == GamePhase.GAME_OVER) {
      val amplitudes = listOf(12f, -10f, 8f, -6f, 3f, 0f)
      for (target in amplitudes) {
        shakeOffsetPx.animateTo(target, animationSpec = tween(durationMillis = 36))
      }
    } else {
      shakeOffsetPx.snapTo(0f)
    }
  }

  ConsoleChassisBackground(
    modifier =
      modifier
        .fillMaxSize()
        .pointerInput(swipeThresholdPx) {
          detectDragGestures(
            onDragStart = { accumulatedDrag = Offset.Zero },
            onDragEnd = { accumulatedDrag = Offset.Zero },
            onDragCancel = { accumulatedDrag = Offset.Zero },
            onDrag = { change, dragAmount ->
              change.consume()
              accumulatedDrag += dragAmount
              val dx = accumulatedDrag.x
              val dy = accumulatedDrag.y
              if (abs(dx) >= swipeThresholdPx || abs(dy) >= swipeThresholdPx) {
                val direction =
                  if (abs(dx) > abs(dy)) {
                    if (dx > 0f) Direction.RIGHT else Direction.LEFT
                  } else {
                    if (dy > 0f) Direction.DOWN else Direction.UP
                  }
                onSwipeDirection(direction)
                accumulatedDrag = Offset.Zero
              }
            },
          )
        }
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .widthIn(max = 520.dp)
          .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Handheld Bezel & LCD Screen
      ConsoleBezelScreen(
        isPowerActive = gameState.phase == GamePhase.PLAYING,
        modifier =
          Modifier.weight(1f)
            .offset { IntOffset(shakeOffsetPx.value.roundToInt(), 0) },
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Compact LCD HUD Strip
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.weight(1f).padding(end = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              LcdHudMetric(
                label = stringResource(R.string.hud_score),
                value = gameState.score.toString().padStart(4, '0'),
                testTag = "hud_score_value",
              )
              LcdHudMetric(
                label = stringResource(R.string.hud_best),
                value = gameState.bestScore.toString().padStart(4, '0'),
                testTag = "hud_best_value",
              )
              LcdHudMetric(
                label = stringResource(R.string.hud_len),
                value = gameState.snake.size.toString().padStart(2, '0'),
                testTag = "hud_length_value",
              )
            }

            Box(
              modifier =
                Modifier.testTag("pause_resume_button")
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(4.dp))
                  .background(palette.screenGridDot)
                  .border(1.5.dp, palette.pixelInk, RoundedCornerShape(4.dp))
                  .clickable(
                    enabled =
                      gameState.phase == GamePhase.PLAYING || gameState.phase == GamePhase.PAUSED
                  ) {
                    if (gameState.phase == GamePhase.PLAYING) {
                      onPauseClick()
                    } else if (gameState.phase == GamePhase.PAUSED) {
                      onResumeClick()
                    }
                  },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector =
                  if (gameState.phase == GamePhase.PAUSED) Icons.Filled.PlayArrow
                  else Icons.Filled.Pause,
                contentDescription = stringResource(R.string.cd_pause_game),
                tint =
                  if (gameState.phase == GamePhase.PLAYING || gameState.phase == GamePhase.PAUSED) {
                    palette.pixelInk
                  } else {
                    palette.pixelInk.copy(alpha = 0.3f)
                  },
                modifier = Modifier.size(20.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Responsive Square LCD Matrix Viewport
          BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
          ) {
            val squareSize = minOf(maxWidth, maxHeight)
            Box(modifier = Modifier.size(squareSize)) {
              LcdPlayfieldCanvas(
                gameState = gameState,
                modifier = Modifier.fillMaxSize().testTag("lcd_playfield"),
              )

              androidx.compose.animation.AnimatedVisibility(
                visible = gameState.phase == GamePhase.READY,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(140)),
              ) {
                LcdReadyOverlay(
                  selectedSpeedTier = gameState.speedTier,
                  onSelectSpeedTier = onSelectSpeedTier,
                  onStartClick = onStartClick,
                  modifier = Modifier.fillMaxSize(),
                )
              }

              androidx.compose.animation.AnimatedVisibility(
                visible = gameState.phase == GamePhase.PAUSED,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120)),
              ) {
                LcdPausedOverlay(
                  onResumeClick = onResumeClick,
                  onRestartClick = onRestartClick,
                  onTitleClick = onTitleClick,
                  modifier = Modifier.fillMaxSize(),
                )
              }

              androidx.compose.animation.AnimatedVisibility(
                visible = gameState.phase == GamePhase.GAME_OVER,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(140)),
              ) {
                LcdGameOverOverlay(
                  gameState = gameState,
                  onPlayAgainClick = onRestartClick,
                  onRecordsClick = onOpenRecords,
                  modifier = Modifier.fillMaxSize(),
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Console Branding, Swipe Cue & Speaker Grille Strip
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.Swipe,
            contentDescription = null,
            tint = ConsoleTextMuted,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text =
              when (gameState.speedTier) {
                SpeedTier.CHILL -> "${stringResource(R.string.speed_chill)} • SWIPE ANYWHERE"
                SpeedTier.CLASSIC -> "${stringResource(R.string.speed_classic)} • SWIPE ANYWHERE"
                SpeedTier.TURBO -> "${stringResource(R.string.speed_turbo)} • SWIPE ANYWHERE"
              },
            style = MaterialTheme.typography.labelMedium,
            color = ConsoleTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        ConsoleSpeakerGrille()
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Console Hardware Buttons (Records / Settings)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ConsoleHardwareButton(
          text = stringResource(R.string.action_records),
          onClick = {
            if (gameState.phase == GamePhase.PLAYING) onPauseClick()
            onOpenRecords()
          },
          modifier = Modifier.weight(1f),
          testTag = "records_nav_button",
        )
        ConsoleHardwareButton(
          text = stringResource(R.string.action_settings),
          onClick = {
            if (gameState.phase == GamePhase.PLAYING) onPauseClick()
            onOpenSettings()
          },
          modifier = Modifier.weight(1f),
          testTag = "settings_nav_button",
        )
      }
    }
  }
}

@Composable
private fun LcdHudMetric(
  label: String,
  value: String,
  testTag: String,
) {
  val palette = LocalLcdPalette.current
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = palette.pixelInkMid,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall,
      color = palette.pixelInk,
      modifier = Modifier.testTag(testTag),
    )
  }
}
