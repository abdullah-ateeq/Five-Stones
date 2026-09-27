package com.example.fivestones.ui.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.fivestones.R
import com.example.fivestones.data.AppSettings
import com.example.fivestones.ui.board.GameBoard
import com.example.fivestones.ui.board.StoneIcon
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.PrimaryButton
import com.example.fivestones.ui.components.SecondaryButton
import com.example.fivestones.ui.components.SectionSurface
import com.example.fivestones.ui.game.MatchResult

@Composable
fun ResultScreen(
    result: MatchResult?,
    settings: AppSettings,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    // A missing result only happens after process death; there is nothing to show, so go home.
    if (result == null) {
        LaunchedEffect(Unit) { onHome() }
        AppBackground { }
        return
    }

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }

    AppBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val landscapeBoardSide = min(maxHeight - 48.dp, 360.dp)
            val portraitBoardSide = min(maxWidth - 96.dp, 300.dp)
            val enter = Modifier.graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 24.dp.toPx()
            }
            if (landscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .then(enter),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FinalBoard(result, settings, Modifier.size(landscapeBoardSide))
                    Column(
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ResultDetails(result, onPlayAgain, onHome)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .then(enter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ResultHeadline(result)
                    Spacer(Modifier.height(24.dp))
                    FinalBoard(result, settings, Modifier.size(portraitBoardSide))
                    Spacer(Modifier.height(24.dp))
                    Column(Modifier.widthIn(max = 420.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ResultStatsAndActions(result, onPlayAgain, onHome)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ResultDetails(result: MatchResult, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    ResultHeadline(result)
    Spacer(Modifier.height(20.dp))
    ResultStatsAndActions(result, onPlayAgain, onHome)
}

@Composable
private fun ResultHeadline(result: MatchResult) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.match_complete).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        if (result.winner != null) StoneIcon(result.winner, 44.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (result.winner != null) {
                stringResource(R.string.player_wins, result.winner.number)
            } else {
                stringResource(R.string.result_draw)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun ResultStatsAndActions(result: MatchResult, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    SectionSurface {
        StatRow(stringResource(R.string.stat_moves_played), result.moves.toString())
        StatDivider()
        StatRow(
            stringResource(R.string.stat_winning_line),
            if (result.winner != null) stringResource(R.string.stones_count, result.winningLineLength) else "—",
        )
        StatDivider()
        StatRow(stringResource(R.string.stat_duration), formatDuration(result.durationMillis))
        StatDivider()
        StatRow(stringResource(R.string.stat_coverage), stringResource(R.string.percent_value, result.coveragePercent))
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton(stringResource(R.string.action_play_again), onPlayAgain, Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    SecondaryButton(stringResource(R.string.action_home), onHome, Modifier.fillMaxWidth())
}

@Composable
private fun FinalBoard(result: MatchResult, settings: AppSettings, modifier: Modifier) {
    val state = result.finalState
    GameBoard(
        board = state.board,
        modifier = modifier,
        lastMove = state.lastMove,
        winningPositions = state.winningPositions,
        showLastMove = settings.showLastMove,
        showWinningLine = settings.showWinningLine,
        elevation = 8.dp,
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        "%d:%02d:%02d".format(minutes / 60, minutes % 60, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
