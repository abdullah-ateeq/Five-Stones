package com.example.fivestones.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fivestones.data.AppSettings
import com.example.fivestones.data.SoundPlayer
import com.example.fivestones.ui.about.AboutScreen
import com.example.fivestones.ui.game.GameScreen
import com.example.fivestones.ui.game.GameViewModel
import com.example.fivestones.ui.home.HomeScreen
import com.example.fivestones.ui.howto.HowToPlayScreen
import com.example.fivestones.ui.result.ResultScreen
import com.example.fivestones.ui.settings.SettingsScreen
import com.example.fivestones.ui.settings.SettingsViewModel
import com.example.fivestones.ui.splash.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun FiveStonesNavHost(
    settings: AppSettings,
    settingsViewModel: SettingsViewModel,
    gameViewModel: GameViewModel,
    soundPlayer: SoundPlayer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) { entry ->
            val stats by settingsViewModel.stats.collectAsStateWithLifecycle()
            val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
            val inProgress = gameState.game.moveCount > 0 && !gameState.game.isGameOver
            HomeScreen(
                stats = stats,
                hasMatchInProgress = inProgress,
                onPlay = entry.guard {
                    if (gameState.game.isGameOver) gameViewModel.newMatch()
                    navController.navigate(Routes.GAME) { launchSingleTop = true }
                },
                onNewMatch = entry.guard {
                    gameViewModel.newMatch()
                    navController.navigate(Routes.GAME) { launchSingleTop = true }
                },
                onHowToPlay = entry.guard { navController.navigate(Routes.HOW_TO_PLAY) },
                onSettings = entry.guard { navController.navigate(Routes.SETTINGS) },
                onAbout = entry.guard { navController.navigate(Routes.ABOUT) },
            )
        }

        composable(Routes.GAME) { entry ->
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                soundPlayer = soundPlayer,
                onHome = entry.guard { navController.popBackStack(Routes.HOME, inclusive = false) },
                onMatchFinished = entry.guard {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.RESULT) { entry ->
            val result by gameViewModel.lastResult.collectAsStateWithLifecycle()
            ResultScreen(
                result = result,
                settings = settings,
                onPlayAgain = entry.guard {
                    gameViewModel.newMatch()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
                onHome = entry.guard { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }

        composable(Routes.HOW_TO_PLAY) { entry ->
            HowToPlayScreen(onBack = entry.guard { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) { entry ->
            val stats by settingsViewModel.stats.collectAsStateWithLifecycle()
            SettingsScreen(
                settings = settings,
                stats = stats,
                viewModel = settingsViewModel,
                onBack = entry.guard { navController.popBackStack() },
            )
        }

        composable(Routes.ABOUT) { entry ->
            AboutScreen(onBack = entry.guard { navController.popBackStack() })
        }
    }
}

/**
 * Wraps a navigation action so it only runs while this destination is resumed. This drops
 * rapid double taps and taps on a screen that is already animating out.
 */
private fun NavBackStackEntry.guard(action: () -> Unit): () -> Unit = {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action()
}
