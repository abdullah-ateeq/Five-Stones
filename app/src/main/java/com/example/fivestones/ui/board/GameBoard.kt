package com.example.fivestones.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.game.BoardGeometry
import com.example.fivestones.game.BoardPosition
import com.example.fivestones.game.Player
import com.example.fivestones.ui.theme.BoardPalette
import com.example.fivestones.ui.theme.LocalBoardPalette
import kotlin.math.roundToInt

/**
 * Square board rendered with Canvas. Stones sit on intersections; taps are mapped to the
 * nearest intersection via [BoardGeometry]. When [onIntersectionTap] is null the board is
 * display-only (result preview, diagrams).
 *
 * The caller must give this a square size (e.g. `Modifier.size(side)` or `aspectRatio(1f)`).
 */
@Composable
fun GameBoard(
    board: List<List<Player?>>,
    modifier: Modifier = Modifier,
    lastMove: BoardPosition? = null,
    winningPositions: List<BoardPosition> = emptyList(),
    showLastMove: Boolean = true,
    showWinningLine: Boolean = true,
    placementId: Int = 0,
    onIntersectionTap: ((BoardPosition) -> Unit)? = null,
    elevation: Dp = 10.dp,
    cornerRadius: Dp = 10.dp,
    palette: BoardPalette = LocalBoardPalette.current,
) {
    val lines = board.size
    val shape = RoundedCornerShape(cornerRadius)

    // Placement animation: only runs when placementId advances (never on undo or recomposition).
    val placeProgress = remember { Animatable(1f) }
    var animatedPlacement by rememberSaveable { mutableIntStateOf(placementId) }
    LaunchedEffect(placementId) {
        if (placementId != animatedPlacement) {
            animatedPlacement = placementId
            placeProgress.snapTo(0f)
            placeProgress.animateTo(1f, tween(durationMillis = 160, easing = FastOutSlowInEasing))
        }
    }

    val highlightWin = showWinningLine && winningPositions.isNotEmpty()
    val lineReveal = remember { Animatable(0f) }
    LaunchedEffect(highlightWin, winningPositions) {
        if (highlightWin) {
            lineReveal.snapTo(0f)
            lineReveal.animateTo(1f, tween(durationMillis = 520, delayMillis = 120, easing = FastOutSlowInEasing))
        } else {
            lineReveal.snapTo(0f)
        }
    }
    val pulse = if (highlightWin) {
        rememberInfiniteTransition(label = "winPulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "winPulseValue",
        )
    } else {
        null
    }

    val currentOnTap by rememberUpdatedState(onIntersectionTap)
    val winningSet = remember(winningPositions) { winningPositions.toHashSet() }

    Box(
        modifier = modifier
            .shadow(elevation, shape)
            .drawWithCache {
                val geometry = BoardGeometry(size.width, lines)
                val grain = buildGrain(size.width)
                val radiusPx = cornerRadius.toPx()
                val lineWidth = (size.width / 420f).coerceAtLeast(1f)
                onDrawBehind {
                    drawBoardSurface(palette, grain, radiusPx)
                    drawGrid(geometry, palette, lineWidth)
                }
            }
            .then(
                if (onIntersectionTap != null) {
                    Modifier.pointerInput(lines) {
                        detectTapGestures { offset ->
                            val geometry = BoardGeometry(size.width.toFloat(), lines)
                            geometry.positionAt(offset.x, offset.y)?.let { currentOnTap?.invoke(it) }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val geometry = BoardGeometry(size.width, lines)
            val radius = geometry.stoneRadius
            fun center(p: BoardPosition) = Offset(geometry.x(p.col), geometry.y(p.row))
            val pulseValue = pulse?.value ?: 0f

            if (highlightWin) {
                drawWinGlow(winningPositions.map(::center), radius, palette.winGlow, pulseValue * lineReveal.value)
            }

            for (r in 0 until lines) {
                for (c in 0 until lines) {
                    val player = board[r][c] ?: continue
                    val pos = BoardPosition(r, c)
                    val isNewest = pos == lastMove && placeProgress.value < 1f
                    val progress = if (isNewest) placeProgress.value else 1f
                    val winScale = if (highlightWin && pos in winningSet) 1f + 0.035f * pulseValue else 1f
                    drawStone(
                        center = center(pos),
                        radius = radius * (0.8f + 0.2f * progress) * winScale,
                        player = player,
                        alpha = progress,
                        lift = 1f - progress,
                    )
                }
            }

            if (highlightWin && lineReveal.value > 0f) {
                val start = center(winningPositions.first())
                val end = center(winningPositions.last())
                drawLine(
                    color = palette.winGlow,
                    start = start,
                    end = start + (end - start) * lineReveal.value,
                    strokeWidth = (radius * 0.12f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round,
                    alpha = 0.7f,
                )
            }

            if (showLastMove && lastMove != null && board[lastMove.row][lastMove.col] != null) {
                drawCircle(
                    color = palette.lastMove,
                    radius = radius * 1.14f,
                    center = center(lastMove),
                    style = Stroke(width = (radius * 0.1f).coerceAtLeast(1.5f)),
                    alpha = placeProgress.value,
                )
            }
        }

        if (onIntersectionTap != null) {
            IntersectionSemantics(board, winningSet, lastMove, onIntersectionTap)
        }
    }
}

/**
 * Invisible accessibility nodes, one per intersection, placed over the canvas. They carry
 * descriptions and click actions for screen readers but do not consume touch input.
 */
@Composable
private fun IntersectionSemantics(
    board: List<List<Player?>>,
    winning: Set<BoardPosition>,
    lastMove: BoardPosition?,
    onTap: (BoardPosition) -> Unit,
) {
    val lines = board.size
    val placeLabel = stringResource(R.string.a11y_place_stone)
    val winningLabel = stringResource(R.string.a11y_winning_stone)
    val lastMoveLabel = stringResource(R.string.a11y_last_move)
    val emptyTemplate = stringResource(R.string.a11y_empty_intersection)
    val stoneTemplate = stringResource(R.string.a11y_player_stone)

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            for (r in 0 until lines) {
                for (c in 0 until lines) {
                    val pos = BoardPosition(r, c)
                    val player = board[r][c]
                    val description = buildString {
                        if (player == null) {
                            append(emptyTemplate.format(r + 1, c + 1))
                        } else {
                            append(stoneTemplate.format(player.number, r + 1, c + 1))
                            if (pos in winning) append(", ").append(winningLabel)
                            if (pos == lastMove) append(", ").append(lastMoveLabel)
                        }
                    }
                    Box(
                        Modifier.semantics {
                            contentDescription = description
                            if (player == null) {
                                onClick(label = placeLabel) { onTap(pos); true }
                            }
                        },
                    )
                }
            }
        },
    ) { measurables, constraints ->
        val side = constraints.maxWidth
        val geometry = BoardGeometry(side.toFloat(), lines)
        val cell = geometry.spacing.roundToInt().coerceAtLeast(1)
        val placeables = measurables.map { it.measure(Constraints.fixed(cell, cell)) }
        layout(side, side) {
            placeables.forEachIndexed { index, placeable ->
                val r = index / lines
                val c = index % lines
                placeable.place(
                    (geometry.x(c) - cell / 2f).roundToInt(),
                    (geometry.y(r) - cell / 2f).roundToInt(),
                )
            }
        }
    }
}

/** Small standalone stone used in panels, labels and ornaments. */
@Composable
fun StoneIcon(player: Player, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        drawStone(center, this.size.minDimension * 0.42f, player)
    }
}
