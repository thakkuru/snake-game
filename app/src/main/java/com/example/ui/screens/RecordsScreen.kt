package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameRunEntity
import com.example.model.AggregateStats
import com.example.ui.components.ConsoleBezelScreen
import com.example.ui.components.ConsoleChassisBackground
import com.example.ui.theme.ConsoleAccentBerry
import com.example.ui.theme.ConsoleShellPuttyLight
import com.example.ui.theme.ConsoleTextInk
import com.example.ui.theme.ConsoleTextMuted
import com.example.ui.theme.LocalLcdPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
  stats: AggregateStats,
  topRuns: List<GameRunEntity>,
  onBackClick: () -> Unit,
  onClearRecords: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = {
        Text(
          text = stringResource(R.string.clear_dialog_title),
          style = MaterialTheme.typography.titleMedium,
        )
      },
      text = {
        Text(
          text = stringResource(R.string.clear_dialog_message),
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showClearDialog = false
            onClearRecords()
          },
          modifier = Modifier.testTag("confirm_clear_records_button"),
        ) {
          Text(
            text = stringResource(R.string.clear_dialog_confirm),
            style = MaterialTheme.typography.labelLarge,
            color = ConsoleAccentBerry,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) {
          Text(
            text = stringResource(R.string.clear_dialog_cancel),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      },
      containerColor = ConsoleShellPuttyLight,
    )
  }

  ConsoleChassisBackground(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .widthIn(max = 560.dp)
          .align(Alignment.Center),
    ) {
      // Top Hardware Navigation Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("records_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back),
            tint = ConsoleTextInk,
          )
        }

        Text(
          text = stringResource(R.string.records_title),
          style = MaterialTheme.typography.titleMedium,
          color = ConsoleTextInk,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        IconButton(
          onClick = { showClearDialog = true },
          enabled = topRuns.isNotEmpty(),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("clear_records_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = stringResource(R.string.action_clear_records),
            tint = if (topRuns.isNotEmpty()) ConsoleAccentBerry else ConsoleTextMuted.copy(alpha = 0.4f),
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // LCD Stats & Leaderboard Console Screen
      ConsoleBezelScreen(
        isPowerActive = topRuns.isNotEmpty(),
        modifier = Modifier.weight(1f),
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // 2x2 Summary Stat Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            LcdStatTile(
              label = stringResource(R.string.stat_best_score),
              value = stats.bestScore.toString(),
              modifier = Modifier.weight(1f),
            )
            LcdStatTile(
              label = stringResource(R.string.stat_games_played),
              value = stats.totalGames.toString(),
              modifier = Modifier.weight(1f),
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            LcdStatTile(
              label = stringResource(R.string.stat_food_eaten),
              value = stats.totalFoodEaten.toString(),
              modifier = Modifier.weight(1f),
            )
            LcdStatTile(
              label = stringResource(R.string.stat_longest_snake),
              value = stats.longestSnake.toString(),
              modifier = Modifier.weight(1f),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = stringResource(R.string.top_runs_header),
            style = MaterialTheme.typography.labelMedium,
            color = palette.pixelInkMid,
          )

          Spacer(modifier = Modifier.height(6.dp))

          if (topRuns.isEmpty()) {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .weight(1f)
                  .border(1.5.dp, palette.pixelInk, RoundedCornerShape(4.dp))
                  .background(palette.screenGridDot.copy(alpha = 0.55f))
                  .padding(20.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.EmojiEvents,
                  contentDescription = null,
                  tint = palette.pixelInkMid,
                  modifier = Modifier.size(36.dp),
                )
                Text(
                  text = stringResource(R.string.records_empty_title),
                  style = MaterialTheme.typography.titleSmall,
                  color = palette.pixelInk,
                )
                Text(
                  text = stringResource(R.string.records_empty_hint),
                  style = MaterialTheme.typography.labelSmall,
                  color = palette.pixelInkMid,
                  textAlign = TextAlign.Center,
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().weight(1f).testTag("top_runs_list"),
              contentPadding = PaddingValues(bottom = 6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              itemsIndexed(topRuns, key = { _, run -> run.id }) { index, run ->
                LcdRunRow(rank = index + 1, run = run)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LcdStatTile(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  val palette = LocalLcdPalette.current
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(4.dp))
        .background(palette.screenGridDot.copy(alpha = 0.7f))
        .border(1.5.dp, palette.pixelInk, RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = palette.pixelInkMid,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall,
      color = palette.pixelInk,
    )
  }
}

@Composable
private fun LcdRunRow(rank: Int, run: GameRunEntity) {
  val palette = LocalLcdPalette.current
  val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
  val dateText = remember(run.playedAtMillis) { formatter.format(Date(run.playedAtMillis)) }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .background(
          if (rank == 1) palette.pixelInk.copy(alpha = 0.14f)
          else palette.screenGridDot.copy(alpha = 0.45f)
        )
        .border(1.dp, palette.pixelInk.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "#$rank",
        style = MaterialTheme.typography.labelLarge,
        color = palette.pixelInkMid,
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "${run.score} PTS",
          style = MaterialTheme.typography.titleSmall,
          color = palette.pixelInk,
        )
        Text(
          text = "${run.speedTierName} • LEN ${run.snakeLength}",
          style = MaterialTheme.typography.labelSmall,
          color = palette.pixelInkMid,
        )
      }
    }

    Text(
      text = dateText,
      style = MaterialTheme.typography.labelSmall,
      color = palette.pixelInkMid,
    )
  }
}
