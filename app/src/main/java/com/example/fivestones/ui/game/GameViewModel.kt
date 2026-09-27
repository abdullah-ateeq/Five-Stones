package com.example.fivestones.ui.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fivestones.FiveStonesApplication
import com.example.fivestones.game.BoardPosition
import com.example.fivestones.game.FiveStonesState
import com.example.fivestones.game.GameRules
import com.example.fivestones.game.Player
import com.example.fivestones.game.TOTAL_INTERSECTIONS
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val game: FiveStonesState = FiveStonesState(),
    /** Increments on every placement so the UI animates only newly placed stones (not undo). */
    val placementId: Int = 0,
    /** Increments per match; used to key one-shot end-of-match effects. */
    val matchId: Int = 0,
)

data class MatchResult(
    val matchId: Int,
    val winner: Player?,
    val moves: Int,
    val winningLineLength: Int,
    val durationMillis: Long,
    val finalState: FiveStonesState,
) {
    val coveragePercent: Int get() = moves * 100 / TOTAL_INTERSECTIONS
}

sealed interface GameEvent {
    data object StonePlaced : GameEvent
    data class Won(val player: Player) : GameEvent
    data object Draw : GameEvent
}

class GameViewModel(
    private val recordResult: suspend (winner: Player?, moves: Int) -> Unit,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _lastResult = MutableStateFlow<MatchResult?>(null)
    val lastResult: StateFlow<MatchResult?> = _lastResult.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var unlockJob: Job? = null

    // Active play time: accumulated only while the game screen is visible.
    private var accumulatedMillis = 0L
    private var resumedAt: Long? = null

    val hasMatchInProgress: Boolean
        get() = _uiState.value.game.let { it.moveCount > 0 && !it.isGameOver }

    fun placeStone(position: BoardPosition) {
        val current = _uiState.value
        if (current.game.isAnimating) return
        val next = GameRules.place(current.game, position) ?: return

        _uiState.value = current.copy(
            game = next.copy(isAnimating = true),
            placementId = current.placementId + 1,
        )
        _events.tryEmit(GameEvent.StonePlaced)

        if (next.isGameOver) finishMatch(next, current.matchId)

        unlockJob?.cancel()
        unlockJob = viewModelScope.launch {
            delay(PLACEMENT_LOCK_MILLIS)
            _uiState.update { it.copy(game = it.game.copy(isAnimating = false)) }
        }
    }

    fun undo() {
        if (!_uiState.value.game.canUndo) return
        _uiState.update { it.copy(game = GameRules.undo(it.game)) }
    }

    /** Starts a fresh match (Restart, Play Again, or a new match from Home). */
    fun newMatch() {
        unlockJob?.cancel()
        accumulatedMillis = 0L
        resumedAt = resumedAt?.let { clock() }
        _uiState.update { GameUiState(placementId = it.placementId, matchId = it.matchId + 1) }
    }

    fun onGameScreenVisible() {
        if (resumedAt == null) resumedAt = clock()
    }

    fun onGameScreenHidden() {
        resumedAt?.let { accumulatedMillis += clock() - it }
        resumedAt = null
    }

    private fun elapsedMillis(): Long = accumulatedMillis + (resumedAt?.let { clock() - it } ?: 0L)

    private fun finishMatch(state: FiveStonesState, matchId: Int) {
        val duration = elapsedMillis()
        onGameScreenHidden()
        _lastResult.value = MatchResult(
            matchId = matchId,
            winner = state.winner,
            moves = state.moveCount,
            winningLineLength = state.winningPositions.size,
            durationMillis = duration,
            finalState = state.copy(isAnimating = false),
        )
        _events.tryEmit(state.winner?.let { GameEvent.Won(it) } ?: GameEvent.Draw)
        viewModelScope.launch { recordResult(state.winner, state.moveCount) }
    }

    companion object {
        const val PLACEMENT_LOCK_MILLIS = 160L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FiveStonesApplication
                GameViewModel(recordResult = app.repository::recordResult)
            }
        }
    }
}
