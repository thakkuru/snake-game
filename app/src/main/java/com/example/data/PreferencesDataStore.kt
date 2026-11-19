package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.LcdPalette
import com.example.model.SpeedTier
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.snakeDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "pocket_snake_prefs")

class PreferencesDataStore(private val context: Context) {

  private object Keys {
    val LCD_PALETTE = stringPreferencesKey("lcd_palette")
    val SPEED_TIER = stringPreferencesKey("speed_tier")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
  }

  val preferences: Flow<UserPreferences> =
    context.snakeDataStore.data.map { prefs ->
      UserPreferences(
        lcdPalette = LcdPalette.fromKey(prefs[Keys.LCD_PALETTE]),
        speedTier = SpeedTier.fromKey(prefs[Keys.SPEED_TIER]),
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
      )
    }

  suspend fun setLcdPalette(palette: LcdPalette) {
    context.snakeDataStore.edit { prefs -> prefs[Keys.LCD_PALETTE] = palette.storageKey }
  }

  suspend fun setSpeedTier(speedTier: SpeedTier) {
    context.snakeDataStore.edit { prefs -> prefs[Keys.SPEED_TIER] = speedTier.storageKey }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.snakeDataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setHapticsEnabled(enabled: Boolean) {
    context.snakeDataStore.edit { prefs -> prefs[Keys.HAPTICS_ENABLED] = enabled }
  }
}
