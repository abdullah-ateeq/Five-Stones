package com.example.fivestones.ui.game

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fivestones.R
import com.example.fivestones.data.AppSettings
import com.example.fivestones.data.SoundEffect
import com.example.fivestones.data.SoundPlayer
import com.example.fivestones.game.FiveStonesState
import com.example.fivestones.game.Player
import com.example.fivestones.ui.board.GameBoard
import com.example.fivestones.ui.board.StoneIcon
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.AppIcons
import com.example.fivestones.ui.components.ConfirmDialog
import com.example.fivestones.ui.components.ControlButton
import com.example.fivestones.ui.components.playerLabel
import com.example.fivestones.ui.components.playerStoneName
import kotlinx.coroutines.delay

/** Pause on the finished board (winning line visible) before moving to the Result screen. */
private const val RESULT_DELAY_MILLIS = 1800L

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    soundPlayer: SoundPlayer,
    onHome: () -> Unit,
    onMatchFinished: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val game = uiState.game
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        viewModel.onGameScreenVisible()
        onDispose { viewModel.onGameScreenHidden() }
    }

    GameFeedbackEffect(viewModel, settings, soundPlayer)

    val finishedCallback by rememberUpdatedState(onMatchFinished)
    if (game.isGameOver) {
        LaunchedEffect(uiState.matchId) {
            delay(RESULT_DELAY_MILLIS)
            finishedCallback()
        }
    }

    val board: @Composable (Modifier) -> Unit = { modifier ->
        GameBoard(
            board = game.board,
            modifier = modifier,
            lastMove = game.lastMove,
            winningPositions = game.winningPositions,
            showLastMove = settings.showLastMove,
            showWinningLine = settings.showWinningLine,
            placementId = uiState.placementId,
            onIntersectionTap = viewModel::placeStone,
        )
    }
    val controls: @Composable (Modifier) -> Unit = { modifier ->
        GameControls(
            canUndo = game.canUndo,
            canRestart = game.moveCount > 0 && !game.isGameOver,
            onHome = onHome,
            onUndo = viewModel::undo,
            onRestart = { showRestartDialog = true },
            modifier = modifier,
        )
    }

    AppBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val landscapeBoardSide = min(maxHeight - 32.dp, maxWidth * 0.62f)
            if (landscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    board(Modifier.size(landscapeBoardSide))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        PlayerPanel(Player.PLAYER_TWO, game)
                        TurnIndicator(game, Modifier.fillMaxWidth())
                        PlayerPanel(Player.PLAYER_ONE, game)
                        controls(Modifier.fillMaxWidth().padding(top = 8.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val contentWidth = Modifier.fillMaxWidth().widthIn(max = 640.dp)
                    PlayerPanel(Player.PLAYER_TWO, game, contentWidth)
                    TurnIndicator(game, contentWidth.padding(vertical = 8.dp))
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        board(Modifier.size(min(maxWidth, maxHeight)))
                    }
                    Spacer(Modifier.height(12.dp))
                    PlayerPanel(Player.PLAYER_ONE, game, contentWidth)
                    controls(contentWidth.padding(top = 12.dp))
                }
            }
        }
    }

    if (showRestartDialog) {
        ConfirmDialog(
            title = stringResource(R.string.restart_title),
            message = stringResource(R.string.restart_message),
            confirmLabel = stringResource(R.string.action_restart),
            onConfirm = {
                showRestartDialog = false
                viewModel.newMatch()
            },
            onDismiss = { showRestartDialog = false },
        )
    }
}

/** Plays sound and haptics for game events, respecting the user's settings. */
@Composable
private fun GameFeedbackEffect(viewModel: GameViewModel, settings: AppSettings, soundPlayer: SoundPlayer) {
    val view = LocalView.current
    val currentSettings by rememberUpdatedState(settings)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val s = currentSettings
            val (sound, haptic) = when (event) {
                GameEvent.StonePlaced -> SoundEffect.STONE_PLACED to HapticFeedbackConstants.CLOCK_TICK
                is GameEvent.Won -> SoundEffect.WIN to
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.LONG_PRESS
                GameEvent.Draw -> SoundEffect.DRAW to HapticFeedbackConstants.VIRTUAL_KEY
            }
            if (s.soundEnabled) soundPlayer.play(sound)
            if (s.hapticsEnabled) view.performHapticFeedback(haptic)
        }
    }
}

@Composable
private fun PlayerPanel(player: Player, game: FiveStonesState, modifier: Modifier = Modifier) {
    val isWinner = game.winner == player
    val isActive = !game.isGameOver && game.currentPlayer == player
    val highlighted = isActive || isWinner
    val colors = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        if (highlighted) colors.primary else colors.outlineVariant,
        tween(220),
        label = "panelBorder",
    )
    val contentAlpha by animateFloatAsState(
        if (highlighted || game.isDraw) 1f else 0.62f,
        tween(220),
        label = "panelAlpha",
    )
    val stones = game.stonesPlacedBy(player)

    Surface(
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (highlighted) colors.surface else colors.surface.copy(alpha = 0.55f),
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StoneIcon(player, 34.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    playerLabel(player).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface,
                )
                Text(
                    stringResource(R.string.panel_subtitle, playerStoneName(player), stones),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            val tag = when {
                isWinner -> stringResource(R.string.tag_winner)
                isActive -> stringResource(R.string.tag_to_move)
                else -> null
            }
            if (tag != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.primaryContainer,
                ) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnIndicator(game: FiveStonesState, modifier: Modifier = Modifier) {
    val status: Pair<String, Player?> = when {
        game.winner != null -> stringResource(R.string.player_wins, game.winner.number) to game.winner
        game.isDraw -> stringResource(R.string.result_draw) to null
        else -> stringResource(R.string.player_turn, game.currentPlayer.number) to game.currentPlayer
    }
    Column(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "turn",
        ) { (text, player) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player != null) {
                    StoneIcon(player, 16.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (game.isGameOver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Text(
            stringResource(R.string.move_counter, game.moveCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GameControls(
    canUndo: Boolean,
    canRestart: Boolean,
    onHome: () -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val item = Modifier.weight(1f)
        ControlButton(AppIcons.Home, stringResource(R.string.action_home), onHome, item)
        ControlButton(AppIcons.Undo, stringResource(R.string.action_undo), onUndo, item, enabled = canUndo)
        ControlButton(AppIcons.Restart, stringResource(R.string.action_restart), onRestart, item, enabled = canRestart)
    }
}
