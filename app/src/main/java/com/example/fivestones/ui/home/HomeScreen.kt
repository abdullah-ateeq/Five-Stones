package com.example.fivestones.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.data.GameStats
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.PrimaryButton
import com.example.fivestones.ui.components.StoneOrnament

@Composable
fun HomeScreen(
    stats: GameStats,
    hasMatchInProgress: Boolean,
    onPlay: () -> Unit,
    onNewMatch: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    AppBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.weight(1f))
            StoneOrnament(stoneSize = 22)
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.app_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PrimaryButton(
                    text = stringResource(if (hasMatchInProgress) R.string.action_resume else R.string.action_play),
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (hasMatchInProgress) {
                    TextButton(onClick = onNewMatch, modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            stringResource(R.string.action_new_match),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(8.dp))
                HomeLink(stringResource(R.string.title_how_to_play), onHowToPlay)
                HomeLink(stringResource(R.string.title_settings), onSettings)
                HomeLink(stringResource(R.string.title_about), onAbout)
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
            StatsStrip(stats)
        }
        }
    }
}

@Composable
private fun HomeLink(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun StatsStrip(stats: GameStats) {
    Row(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val items = listOf(
            stringResource(R.string.stat_games) to stats.gamesPlayed,
            stringResource(R.string.stat_p1_wins) to stats.playerOneWins,
            stringResource(R.string.stat_p2_wins) to stats.playerTwoWins,
            stringResource(R.string.stat_draws) to stats.draws,
        )
        items.forEachIndexed { index, (label, value) ->
            if (index > 0) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = MaterialTheme.typography.bodyLarge.letterSpacing),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
