package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ConsoleAccentBerry
import com.example.ui.theme.ConsoleAccentBerryDark
import com.example.ui.theme.ConsoleBezelDark
import com.example.ui.theme.ConsoleBezelInner
import com.example.ui.theme.ConsoleBezelStroke
import com.example.ui.theme.ConsoleButtonFace
import com.example.ui.theme.ConsoleButtonText
import com.example.ui.theme.ConsoleLedActive
import com.example.ui.theme.ConsoleLedIdle
import com.example.ui.theme.ConsoleShellPutty
import com.example.ui.theme.ConsoleShellPuttyLight
import com.example.ui.theme.ConsoleShellPuttyShadow
import com.example.ui.theme.ConsoleTextMuted
import com.example.ui.theme.LocalLcdPalette

@Composable
fun ConsoleBezelScreen(
  isPowerActive: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val lcdPalette = LocalLcdPalette.current
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .shadow(6.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(ConsoleBezelDark, ConsoleBezelInner)
          )
        )
        .border(2.dp, ConsoleBezelStroke, RoundedCornerShape(18.dp))
        .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(8.dp)
              .clip(CircleShape)
              .background(if (isPowerActive) ConsoleLedActive else ConsoleLedIdle)
              .border(1.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "BATT",
          style = MaterialTheme.typography.labelSmall,
          color = ConsoleShellPuttyShadow,
        )
      }

      Row(
        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
      ) {
        Box(
          modifier =
            Modifier.weight(1f)
              .height(2.dp)
              .background(ConsoleAccentBerry.copy(alpha = 0.85f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.console_brand_model),
          style = MaterialTheme.typography.labelSmall,
          color = ConsoleShellPuttyShadow,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(lcdPalette.screenBg)
          .border(2.dp, lcdPalette.screenBorder, RoundedCornerShape(6.dp))
          .padding(6.dp)
    ) {
      content()
    }
  }
}

@Composable
fun ConsoleHardwareButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPrimary: Boolean = false,
  enabled: Boolean = true,
  testTag: String? = null,
) {
  val bgTop = if (isPrimary) ConsoleAccentBerry else ConsoleButtonFace
  val bgBottom = if (isPrimary) ConsoleAccentBerryDark else ConsoleBezelInner
  val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier

  Surface(
    modifier =
      taggedModifier
        .minimumInteractiveComponentSize()
        .shadow(3.dp, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
        .border(1.5.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
        .clickable(enabled = enabled, onClick = onClick),
    color = Color.Transparent,
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) ConsoleButtonText else ConsoleButtonText.copy(alpha = 0.5f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun ConsoleChassisBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier =
      modifier.background(
        Brush.verticalGradient(
          colors = listOf(ConsoleShellPuttyLight, ConsoleShellPutty, ConsoleShellPuttyShadow)
        )
      ),
    content = content,
  )
}

@Composable
fun ConsoleSpeakerGrille(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(5) {
      Box(
        modifier =
          Modifier.width(4.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(ConsoleTextMuted.copy(alpha = 0.38f))
      )
    }
  }
}
