package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.FeedbackEvent
import com.example.ui.screens.GameScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.SnakeViewModel

const val ROUTE_GAME = "game"
const val ROUTE_RECORDS = "records"
const val ROUTE_SETTINGS = "settings"

@Composable
fun PocketSnakeNavHost(
  viewModel: SnakeViewModel,
  navController: NavHostController = rememberNavController(),
) {
  val gameState by viewModel.gameState.collectAsStateWithLifecycle()
  val preferences by viewModel.preferences.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val topRuns by viewModel.topRuns.collectAsStateWithLifecycle()
  val hapticFeedback = LocalHapticFeedback.current

  LaunchedEffect(viewModel, preferences.hapticsEnabled) {
    viewModel.feedbackEvents.collect { event ->
      if (preferences.hapticsEnabled) {
        when (event) {
          FeedbackEvent.TURN -> hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          FeedbackEvent.EAT,
          FeedbackEvent.NEW_RECORD,
          FeedbackEvent.GAME_OVER -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
      }
    }
  }

  NavHost(navController = navController, startDestination = ROUTE_GAME) {
    composable(ROUTE_GAME) {
      GameScreen(
        gameState = gameState,
        onSwipeDirection = viewModel::onSwipeDirection,
        onStartClick = { viewModel.startGame() },
        onPauseClick = viewModel::pauseGame,
        onResumeClick = viewModel::resumeGame,
        onRestartClick = viewModel::restartGame,
        onTitleClick = viewModel::returnToTitle,
        onSelectSpeedTier = viewModel::selectSpeedTier,
        onOpenRecords = {
          navController.navigate(ROUTE_RECORDS) { launchSingleTop = true }
        },
        onOpenSettings = {
          navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true }
        },
      )
    }

    composable(ROUTE_RECORDS) {
      RecordsScreen(
        stats = stats,
        topRuns = topRuns,
        onBackClick = { navController.popBackStack() },
        onClearRecords = viewModel::clearAllRecords,
      )
    }

    composable(ROUTE_SETTINGS) {
      SettingsScreen(
        preferences = preferences,
        onSelectPalette = viewModel::setLcdPalette,
        onToggleSound = viewModel::setSoundEnabled,
        onToggleHaptics = viewModel::setHapticsEnabled,
        onBackClick = { navController.popBackStack() },
      )
    }
  }
}
