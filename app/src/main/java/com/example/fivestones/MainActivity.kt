package com.example.fivestones

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fivestones.data.ThemeMode
import com.example.fivestones.ui.FiveStonesNavHost
import com.example.fivestones.ui.game.GameViewModel
import com.example.fivestones.ui.settings.SettingsViewModel
import com.example.fivestones.ui.theme.FiveStonesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FiveStonesApplication

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            val darkTheme = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            LaunchedEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            FiveStonesTheme(darkTheme = darkTheme, boardTheme = settings.boardTheme) {
                FiveStonesNavHost(
                    settings = settings,
                    settingsViewModel = settingsViewModel,
                    gameViewModel = gameViewModel,
                    soundPlayer = app.soundPlayer,
                )
            }
        }
    }
}
