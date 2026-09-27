package com.example.fivestones.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.game.Player
import com.example.fivestones.ui.board.StoneIcon
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.ContentColumn
import com.example.fivestones.ui.components.ScreenTopBar
import com.example.fivestones.ui.components.SectionSurface
import com.example.fivestones.ui.components.StoneOrnament

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val features = listOf(
        R.string.feature_local,
        R.string.feature_board,
        R.string.feature_strategy,
        R.string.feature_offline,
        R.string.feature_themes,
    )
    AppBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenTopBar(stringResource(R.string.title_about), onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                ContentColumn(Modifier.padding(bottom = 32.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        StoneOrnament(stoneSize = 20)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            stringResource(R.string.about_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.about_version),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            stringResource(R.string.about_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                    SectionSurface {
                        features.forEachIndexed { index, feature ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                StoneIcon(if (index % 2 == 0) Player.PLAYER_ONE else Player.PLAYER_TWO, 16.dp)
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    stringResource(feature),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        stringResource(R.string.about_footer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
