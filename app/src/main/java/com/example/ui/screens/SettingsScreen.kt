package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.LcdPalette
import com.example.model.UserPreferences
import com.example.ui.components.ConsoleChassisBackground
import com.example.ui.theme.ConsoleAccentBerry
import com.example.ui.theme.ConsoleButtonText
import com.example.ui.theme.ConsoleShellPuttyLight
import com.example.ui.theme.ConsoleShellPuttyShadow
import com.example.ui.theme.ConsoleTextInk
import com.example.ui.theme.ConsoleTextMuted

@Composable
fun SettingsScreen(
  preferences: UserPreferences,
  onSelectPalette: (LcdPalette) -> Unit,
  onToggleSound: (Boolean) -> Unit,
  onToggleHaptics: (Boolean) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ConsoleChassisBackground(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .widthIn(max = 560.dp)
          .align(Alignment.Center)
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back),
            tint = ConsoleTextInk,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.settings_title),
          style = MaterialTheme.typography.titleMedium,
          color = ConsoleTextInk,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Column(
        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // LCD Palette Selection Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(R.string.settings_lcd_palette),
            style = MaterialTheme.typography.labelLarge,
            color = ConsoleTextMuted,
          )

          LcdPalette.entries.forEach { option ->
            val label =
              when (option) {
                LcdPalette.CLASSIC_OLIVE -> stringResource(R.string.palette_classic_olive)
                LcdPalette.POCKET_AMBER -> stringResource(R.string.palette_pocket_amber)
                LcdPalette.BACKLIT_TEAL -> stringResource(R.string.palette_backlit_teal)
              }
            PaletteOptionCard(
              title = label,
              paletteOption = option,
              isSelected = option == preferences.lcdPalette,
              onClick = { onSelectPalette(option) },
            )
          }
        }

        // Audio & Haptics Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(R.string.settings_feedback_header),
            style = MaterialTheme.typography.labelLarge,
            color = ConsoleTextMuted,
          )

          SettingsToggleCard(
            icon = Icons.Filled.VolumeUp,
            title = stringResource(R.string.settings_sound_label),
            subtitle = stringResource(R.string.settings_sound_desc),
            checked = preferences.soundEnabled,
            onCheckedChange = onToggleSound,
            testTag = "sound_toggle_switch",
          )

          SettingsToggleCard(
            icon = Icons.Filled.Vibration,
            title = stringResource(R.string.settings_haptics_label),
            subtitle = stringResource(R.string.settings_haptics_desc),
            checked = preferences.hapticsEnabled,
            onCheckedChange = onToggleHaptics,
            testTag = "haptics_toggle_switch",
          )
        }

        // Classic Rules Summary Card
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(ConsoleShellPuttyLight.copy(alpha = 0.75f))
              .border(1.5.dp, ConsoleShellPuttyShadow, RoundedCornerShape(12.dp))
              .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = stringResource(R.string.settings_rules_header),
            style = MaterialTheme.typography.labelLarge,
            color = ConsoleTextInk,
          )
          Text(
            text = stringResource(R.string.settings_rules_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = ConsoleTextMuted,
          )
        }
      }
    }
  }
}

@Composable
private fun PaletteOptionCard(
  title: String,
  paletteOption: LcdPalette,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .testTag("palette_option_${paletteOption.storageKey.lowercase()}")
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(12.dp))
        .background(if (isSelected) ConsoleShellPuttyLight else ConsoleShellPuttyLight.copy(alpha = 0.55f))
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) ConsoleAccentBerry else ConsoleShellPuttyShadow,
          shape = RoundedCornerShape(12.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Live mini LCD pixel swatch
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(paletteOption.screenBg)
            .border(2.dp, paletteOption.screenBorder, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          Box(modifier = Modifier.size(7.dp).background(paletteOption.pixelInk))
          Box(modifier = Modifier.size(7.dp).background(paletteOption.pixelInkMid))
          Box(modifier = Modifier.size(7.dp).background(paletteOption.pixelInk))
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = ConsoleTextInk,
      )
    }

    if (isSelected) {
      Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = null,
        tint = ConsoleAccentBerry,
      )
    }
  }
}

@Composable
private fun SettingsToggleCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(ConsoleShellPuttyLight.copy(alpha = 0.75f))
        .border(1.dp, ConsoleShellPuttyShadow, RoundedCornerShape(12.dp))
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      modifier = Modifier.weight(1f).padding(end = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = ConsoleTextInk,
        modifier = Modifier.size(22.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = ConsoleTextInk,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = ConsoleTextMuted,
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(testTag),
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = ConsoleButtonText,
          checkedTrackColor = ConsoleAccentBerry,
        ),
    )
  }
}
