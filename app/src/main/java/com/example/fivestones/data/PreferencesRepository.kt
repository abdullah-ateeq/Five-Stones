package com.example.fivestones.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fivestones.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class BoardTheme { CLASSIC_WOOD, MIDNIGHT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLastMove: Boolean = true,
    val showWinningLine: Boolean = true,
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val playerOneWins: Int = 0,
    val playerTwoWins: Int = 0,
    val draws: Int = 0,
    /** Fewest total moves in a won match, or null when no match has been won yet. */
    val fastestWinMoves: Int? = null,
)

private val Context.fiveStonesDataStore: DataStore<Preferences> by preferencesDataStore(name = "five_stones")

class PreferencesRepository(context: Context) {

    private val dataStore = context.applicationContext.fiveStonesDataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SHOW_LAST_MOVE = booleanPreferencesKey("show_last_move")
        val SHOW_WINNING_LINE = booleanPreferencesKey("show_winning_line")

        val GAMES_PLAYED = intPreferencesKey("stats_games_played")
        val P1_WINS = intPreferencesKey("stats_p1_wins")
        val P2_WINS = intPreferencesKey("stats_p2_wins")
        val DRAWS = intPreferencesKey("stats_draws")
        val FASTEST_WIN = intPreferencesKey("stats_fastest_win")
    }

    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val settings: Flow<AppSettings> = preferences.map { p ->
        AppSettings(
            themeMode = enumOrDefault(p[Keys.THEME_MODE], ThemeMode.SYSTEM),
            boardTheme = enumOrDefault(p[Keys.BOARD_THEME], BoardTheme.CLASSIC_WOOD),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLastMove = p[Keys.SHOW_LAST_MOVE] ?: true,
            showWinningLine = p[Keys.SHOW_WINNING_LINE] ?: true,
        )
    }.distinctUntilChanged()

    val stats: Flow<GameStats> = preferences.map { p ->
        GameStats(
            gamesPlayed = p[Keys.GAMES_PLAYED] ?: 0,
            playerOneWins = p[Keys.P1_WINS] ?: 0,
            playerTwoWins = p[Keys.P2_WINS] ?: 0,
            draws = p[Keys.DRAWS] ?: 0,
            fastestWinMoves = p[Keys.FASTEST_WIN],
        )
    }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = dataStore.edit { it[Keys.BOARD_THEME] = theme.name }
    suspend fun setSoundEnabled(enabled: Boolean) = dataStore.edit { it[Keys.SOUND] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLastMove(enabled: Boolean) = dataStore.edit { it[Keys.SHOW_LAST_MOVE] = enabled }
    suspend fun setShowWinningLine(enabled: Boolean) = dataStore.edit { it[Keys.SHOW_WINNING_LINE] = enabled }

    /** Records a finished match. [winner] is null for a draw. */
    suspend fun recordResult(winner: Player?, moves: Int) {
        dataStore.edit { p ->
            p[Keys.GAMES_PLAYED] = (p[Keys.GAMES_PLAYED] ?: 0) + 1
            when (winner) {
                Player.PLAYER_ONE -> p[Keys.P1_WINS] = (p[Keys.P1_WINS] ?: 0) + 1
                Player.PLAYER_TWO -> p[Keys.P2_WINS] = (p[Keys.P2_WINS] ?: 0) + 1
                null -> p[Keys.DRAWS] = (p[Keys.DRAWS] ?: 0) + 1
            }
            if (winner != null) {
                val fastest = p[Keys.FASTEST_WIN]
                if (fastest == null || moves < fastest) p[Keys.FASTEST_WIN] = moves
            }
        }
    }

    suspend fun resetStats() {
        dataStore.edit { p ->
            p.remove(Keys.GAMES_PLAYED)
            p.remove(Keys.P1_WINS)
            p.remove(Keys.P2_WINS)
            p.remove(Keys.DRAWS)
            p.remove(Keys.FASTEST_WIN)
        }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
