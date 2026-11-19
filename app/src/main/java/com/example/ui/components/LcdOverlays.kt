package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CollisionReason
import com.example.model.GameUiState
import com.example.model.SpeedTier
import com.example.ui.theme.LocalLcdPalette

@Composable
fun LcdReadyOverlay(
  selectedSpeedTier: SpeedTier,
  onSelectSpeedTier: (SpeedTier) -> Unit,
  onStartClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  Box(
    modifier =
      modifier
        .background(palette.screenBg.copy(alpha = 0.90f))
        .padding(horizontal = 8.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = stringResource(R.string.ready_title),
        style = MaterialTheme.typography.titleMedium,
        color = palette.pixelInk,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = stringResource(R.string.ready_subtitle),
        style = MaterialTheme.typography.labelSmall,
        color = palette.pixelInkMid,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SpeedTier.entries.forEach { tier ->
          val isSelected = tier == selectedSpeedTier
          val label =
            when (tier) {
              SpeedTier.CHILL -> stringResource(R.string.speed_chill)
              SpeedTier.CLASSIC -> stringResource(R.string.speed_classic)
              SpeedTier.TURBO -> stringResource(R.string.speed_turbo)
            }
          Box(
            modifier =
              Modifier.testTag("speed_tier_${tier.storageKey.lowercase()}")
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) palette.pixelInk else palette.screenGridDot)
                .border(1.5.dp, palette.pixelInk, RoundedCornerShape(4.dp))
                .clickable { onSelectSpeedTier(tier) }
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) palette.screenBg else palette.pixelInk,
              maxLines = 1,
            )
          }
        }
      }

      LcdPixelButton(
        text = stringResource(R.string.action_start),
        onClick = onStartClick,
        testTag = "start_game_button",
      )
    }
  }
}

@Composable
fun LcdPausedOverlay(
  onResumeClick: () -> Unit,
  onRestartClick: () -> Unit,
  onTitleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  Box(
    modifier =
      modifier
        .background(palette.screenBg.copy(alpha = 0.92f))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = stringResource(R.string.paused_title),
        style = MaterialTheme.typography.headlineSmall,
        color = palette.pixelInk,
      )
      Text(
        text = stringResource(R.string.paused_subtitle),
        style = MaterialTheme.typography.labelSmall,
        color = palette.pixelInkMid,
        textAlign = TextAlign.Center,
      )
      LcdPixelButton(
        text = stringResource(R.string.action_resume),
        onClick = onResumeClick,
        testTag = "resume_game_button",
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LcdPixelButton(
          text = stringResource(R.string.action_restart),
          onClick = onRestartClick,
          filled = false,
          testTag = "pause_restart_button",
        )
        LcdPixelButton(
          text = stringResource(R.string.action_quit_title),
          onClick = onTitleClick,
          filled = false,
          testTag = "pause_title_button",
        )
      }
    }
  }
}

@Composable
fun LcdGameOverOverlay(
  gameState: GameUiState,
  onPlayAgainClick: () -> Unit,
  onRecordsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  val reasonText =
    when (gameState.collisionReason) {
      CollisionReason.WALL -> stringResource(R.string.collision_wall)
      CollisionReason.SELF -> stringResource(R.string.collision_self)
      CollisionReason.NONE -> ""
    }

  Box(
    modifier =
      modifier
        .background(palette.screenBg.copy(alpha = 0.94f))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (gameState.isNewHighScore) {
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(4.dp))
              .background(palette.pixelInk)
              .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = stringResource(R.string.new_high_score_badge),
            style = MaterialTheme.typography.labelSmall,
            color = palette.screenBg,
          )
        }
      }

      Text(
        text = stringResource(R.string.game_over_title),
        style = MaterialTheme.typography.headlineSmall,
        color = palette.pixelInk,
        textAlign = TextAlign.Center,
      )

      if (reasonText.isNotEmpty()) {
        Text(
          text = reasonText,
          style = MaterialTheme.typography.labelSmall,
          color = palette.pixelInkMid,
        )
      }

      Column(
        modifier =
          Modifier.fillMaxWidth(0.86f)
            .border(2.dp, palette.pixelInk, RoundedCornerShape(4.dp))
            .background(palette.screenGridDot.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        LcdSummaryRow(label = stringResource(R.string.hud_score), value = gameState.score.toString())
        LcdSummaryRow(label = stringResource(R.string.hud_best), value = gameState.bestScore.toString())
        LcdSummaryRow(
          label = stringResource(R.string.hud_len),
          value = gameState.snake.size.toString(),
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LcdPixelButton(
          text = stringResource(R.string.action_play_again),
          onClick = onPlayAgainClick,
          testTag = "play_again_button",
        )
        LcdPixelButton(
          text = stringResource(R.string.action_records),
          onClick = onRecordsClick,
          filled = false,
          testTag = "gameover_records_button",
        )
      }
    }
  }
}

@Composable
private fun LcdSummaryRow(label: String, value: String) {
  val palette = LocalLcdPalette.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, style = MaterialTheme.typography.labelMedium, color = palette.pixelInkMid)
    Text(text = value, style = MaterialTheme.typography.titleSmall, color = palette.pixelInk)
  }
}

@Composable
private fun LcdPixelButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  filled: Boolean = true,
  testTag: String? = null,
) {
  val palette = LocalLcdPalette.current
  val base = if (testTag != null) modifier.testTag(testTag) else modifier
  Box(
    modifier =
      base
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(4.dp))
        .background(if (filled) palette.pixelInk else palette.screenBg)
        .border(2.dp, palette.pixelInk, RoundedCornerShape(4.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (filled) palette.screenBg else palette.pixelInk,
    )
  }
}
