package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.model.LcdPalette

val LocalLcdPalette = staticCompositionLocalOf { LcdPalette.CLASSIC_OLIVE }

private val PocketConsoleColorScheme =
  lightColorScheme(
    primary = ConsoleAccentBerry,
    onPrimary = ConsoleButtonText,
    primaryContainer = ConsoleShellPuttyLight,
    onPrimaryContainer = ConsoleTextInk,
    secondary = ConsoleButtonFace,
    onSecondary = ConsoleButtonText,
    secondaryContainer = ConsoleShellPuttyShadow,
    onSecondaryContainer = ConsoleTextInk,
    tertiary = ConsoleBezelDark,
    onTertiary = ConsoleButtonText,
    background = ConsoleShellPutty,
    onBackground = ConsoleTextInk,
    surface = ConsoleShellPuttyLight,
    onSurface = ConsoleTextInk,
    surfaceVariant = ConsoleShellPuttyShadow,
    onSurfaceVariant = ConsoleTextMuted,
    outline = ConsoleBezelStroke,
    error = ConsoleAccentBerryDark,
    onError = ConsoleButtonText,
  )

@Composable
fun PocketSnakeTheme(
  lcdPalette: LcdPalette = LcdPalette.CLASSIC_OLIVE,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalLcdPalette provides lcdPalette) {
    MaterialTheme(
      colorScheme = PocketConsoleColorScheme,
      typography = AppTypography,
      content = content,
    )
  }
}
