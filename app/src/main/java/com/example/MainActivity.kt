package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.RetroAudioEngine
import com.example.data.PreferencesDataStore
import com.example.data.SnakeDatabase
import com.example.data.SnakeRepository
import com.example.ui.navigation.PocketSnakeNavHost
import com.example.ui.theme.PocketSnakeTheme
import com.example.ui.viewmodel.SnakeViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = SnakeDatabase.getInstance(applicationContext)
    val prefsDataStore = PreferencesDataStore(applicationContext)
    val repository = SnakeRepository(database.gameRunDao(), prefsDataStore)
    val soundPlayer = RetroAudioEngine()
    val factory = SnakeViewModel.Factory(repository = repository, soundPlayer = soundPlayer)

    setContent {
      val snakeViewModel: SnakeViewModel = viewModel(factory = factory)
      val preferences by snakeViewModel.preferences.collectAsStateWithLifecycle()
      val lifecycleOwner = LocalLifecycleOwner.current

      DisposableEffect(lifecycleOwner, snakeViewModel) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_PAUSE) {
            snakeViewModel.pauseGame()
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
      }

      PocketSnakeTheme(lcdPalette = preferences.lcdPalette) {
        PocketSnakeNavHost(viewModel = snakeViewModel)
      }
    }
  }
}
