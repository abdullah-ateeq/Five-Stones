package com.example.fivestones.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fivestones.FiveStonesApplication
import com.example.fivestones.data.AppSettings
import com.example.fivestones.data.BoardTheme
import com.example.fivestones.data.GameStats
import com.example.fivestones.data.PreferencesRepository
import com.example.fivestones.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** App-wide settings and statistics, shared by Home, Game, Settings and the theme. */
class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val stats: StateFlow<GameStats> =
        repository.stats.stateIn(viewModelScope, SharingStarted.Eagerly, GameStats())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSoundEnabled(enabled: Boolean) = launch { repository.setSoundEnabled(enabled) }
    fun setHapticsEnabled(enabled: Boolean) = launch { repository.setHapticsEnabled(enabled) }
    fun setShowLastMove(enabled: Boolean) = launch { repository.setShowLastMove(enabled) }
    fun setShowWinningLine(enabled: Boolean) = launch { repository.setShowWinningLine(enabled) }
    fun resetStats() = launch { repository.resetStats() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FiveStonesApplication
                SettingsViewModel(app.repository)
            }
        }
    }
}
