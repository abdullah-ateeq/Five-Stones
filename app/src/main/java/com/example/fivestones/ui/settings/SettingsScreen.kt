package com.example.fivestones.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.data.AppSettings
import com.example.fivestones.data.BoardTheme
import com.example.fivestones.data.GameStats
import com.example.fivestones.data.ThemeMode
import com.example.fivestones.game.Player
import com.example.fivestones.ui.board.GameBoard
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.ConfirmDialog
import com.example.fivestones.ui.components.ContentColumn
import com.example.fivestones.ui.components.ScreenTopBar
import com.example.fivestones.ui.components.SectionLabel
import com.example.fivestones.ui.components.SectionSurface
import com.example.fivestones.ui.theme.BoardPalettes
import com.example.fivestones.ui.theme.LocalBoardPalette

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStats,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    AppBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenTopBar(stringResource(R.string.title_settings), onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                ContentColumn(Modifier.padding(bottom = 32.dp)) {
                    SectionLabel(stringResource(R.string.section_appearance))
                    SectionSurface {
                        SettingCaption(stringResource(R.string.setting_theme))
                        SegmentedChoice(
                            options = listOf(
                                ThemeMode.LIGHT to stringResource(R.string.theme_light),
                                ThemeMode.DARK to stringResource(R.string.theme_dark),
                                ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                            ),
                            selected = settings.themeMode,
                            onSelect = viewModel::setThemeMode,
                        )
                        Spacer(Modifier.height(8.dp))
                        SettingCaption(stringResource(R.string.setting_board_theme))
                        BoardThemePicker(settings.boardTheme, viewModel::setBoardTheme)
                    }

                    SectionLabel(stringResource(R.string.section_gameplay))
                    SectionSurface {
                        ToggleRow(stringResource(R.string.setting_sound), settings.soundEnabled, viewModel::setSoundEnabled)
                        RowDivider()
                        ToggleRow(stringResource(R.string.setting_haptics), settings.hapticsEnabled, viewModel::setHapticsEnabled)
                        RowDivider()
                        ToggleRow(stringResource(R.string.setting_last_move), settings.showLastMove, viewModel::setShowLastMove)
                        RowDivider()
                        ToggleRow(
                            stringResource(R.string.setting_winning_line),
                            settings.showWinningLine,
                            viewModel::setShowWinningLine,
                        )
                    }

                    SectionLabel(stringResource(R.string.section_statistics))
                    SectionSurface {
                        StatLine(stringResource(R.string.stat_games_played), stats.gamesPlayed.toString())
                        StatLine(stringResource(R.string.stat_p1_wins_long), stats.playerOneWins.toString())
                        StatLine(stringResource(R.string.stat_p2_wins_long), stats.playerTwoWins.toString())
                        StatLine(stringResource(R.string.stat_draws), stats.draws.toString())
                        StatLine(
                            stringResource(R.string.stat_fastest_win),
                            stats.fastestWinMoves?.let { stringResource(R.string.moves_count, it) } ?: "—",
                        )
                        RowDivider()
                        TextButton(
                            onClick = { confirmReset = true },
                            enabled = stats.gamesPlayed > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        ) {
                            Text(
                                stringResource(R.string.action_reset_stats).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (stats.gamesPlayed > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.reset_stats_title),
            message = stringResource(R.string.reset_stats_message),
            confirmLabel = stringResource(R.string.action_reset),
            destructive = true,
            onConfirm = {
                confirmReset = false
                viewModel.resetStats()
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun SettingCaption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

@Composable
private fun <T> SegmentedChoice(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ) {
        Row(Modifier.padding(4.dp).selectableGroup()) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) else null,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardThemePicker(selected: BoardTheme, onSelect: (BoardTheme) -> Unit) {
    val preview = List(5) { r ->
        List(5) { c ->
            when (r to c) {
                1 to 1, 2 to 2, 3 to 3 -> Player.PLAYER_ONE
                1 to 3, 3 to 1 -> Player.PLAYER_TWO
                else -> null
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(
            BoardTheme.CLASSIC_WOOD to stringResource(R.string.board_classic_wood),
            BoardTheme.MIDNIGHT to stringResource(R.string.board_midnight),
        ).forEach { (theme, label) ->
            val isSelected = theme == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(theme) }),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CompositionLocalProvider(LocalBoardPalette provides BoardPalettes.of(theme)) {
                        GameBoard(
                            board = preview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            showLastMove = false,
                            elevation = 3.dp,
                            cornerRadius = 8.dp,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
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
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
