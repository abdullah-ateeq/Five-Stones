package com.example.fivestones.ui.howto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.game.BoardPosition
import com.example.fivestones.game.Player
import com.example.fivestones.ui.board.GameBoard
import com.example.fivestones.ui.components.AppBackground
import com.example.fivestones.ui.components.ContentColumn
import com.example.fivestones.ui.components.ScreenTopBar
import com.example.fivestones.ui.components.SectionLabel
import com.example.fivestones.ui.components.SectionSurface

private const val DIAGRAM_LINES = 7

private data class Diagram(
    val caption: Int,
    val description: Int,
    val playerOne: List<Pair<Int, Int>>,
    val playerTwo: List<Pair<Int, Int>>,
    val winning: List<Pair<Int, Int>>,
)

private val Diagrams = listOf(
    Diagram(
        R.string.diagram_horizontal, R.string.diagram_horizontal_desc,
        playerOne = (1..5).map { 3 to it },
        playerTwo = listOf(2 to 2, 4 to 3, 2 to 5, 4 to 1),
        winning = (1..5).map { 3 to it },
    ),
    Diagram(
        R.string.diagram_vertical, R.string.diagram_vertical_desc,
        playerOne = listOf(2 to 2, 4 to 4, 1 to 4, 5 to 2),
        playerTwo = (1..5).map { it to 3 },
        winning = (1..5).map { it to 3 },
    ),
    Diagram(
        R.string.diagram_diagonal, R.string.diagram_diagonal_desc,
        playerOne = (1..5).map { it to 6 - it },
        playerTwo = listOf(2 to 2, 3 to 2, 4 to 5, 1 to 2),
        winning = (1..5).map { it to 6 - it },
    ),
    Diagram(
        R.string.diagram_broken, R.string.diagram_broken_desc,
        playerOne = listOf(1 to 1, 2 to 2, 4 to 4, 5 to 5),
        playerTwo = listOf(3 to 3, 2 to 4, 4 to 2),
        winning = emptyList(),
    ),
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    AppBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenTopBar(stringResource(R.string.title_how_to_play), onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                ContentColumn(Modifier.padding(bottom = 32.dp)) {
                    SectionLabel(stringResource(R.string.section_rules))
                    SectionSurface {
                        val rules = listOf(
                            R.string.rule_turns_title to R.string.rule_turns_body,
                            R.string.rule_intersections_title to R.string.rule_intersections_body,
                            R.string.rule_no_moving_title to R.string.rule_no_moving_body,
                            R.string.rule_line_title to R.string.rule_line_body,
                            R.string.rule_first_title to R.string.rule_first_body,
                        )
                        rules.forEachIndexed { index, (title, body) ->
                            RuleRow(index + 1, stringResource(title), stringResource(body))
                        }
                    }

                    SectionLabel(stringResource(R.string.section_examples))
                    Diagrams.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            pair.forEach { DiagramCard(it, Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleRow(number: Int, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            number.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(28.dp),
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DiagramCard(diagram: Diagram, modifier: Modifier = Modifier) {
    val board = List(DIAGRAM_LINES) { r ->
        List(DIAGRAM_LINES) { c ->
            when (r to c) {
                in diagram.playerOne -> Player.PLAYER_ONE
                in diagram.playerTwo -> Player.PLAYER_TWO
                else -> null
            }
        }
    }
    val description = stringResource(diagram.description)
    Column(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameBoard(
            board = board,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            winningPositions = diagram.winning.map { (r, c) -> BoardPosition(r, c) },
            showLastMove = false,
            elevation = 4.dp,
            cornerRadius = 8.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(diagram.caption).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
