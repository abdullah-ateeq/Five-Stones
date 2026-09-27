package com.example.fivestones.ui.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.fivestones.game.BoardGeometry
import com.example.fivestones.game.BoardPosition
import com.example.fivestones.game.Player
import com.example.fivestones.ui.theme.BoardPalette
import kotlin.random.Random

/** One procedurally generated grain stroke across the board surface. */
class GrainStroke(val path: Path, val width: Float, val alpha: Float)

/** Deterministic, gently wavy grain so the board looks identical on every frame and launch. */
fun buildGrain(size: Float, seed: Int = 17): List<GrainStroke> {
    val random = Random(seed)
    val count = 46
    return List(count) { i ->
        val baseY = size * (i + random.nextFloat()) / count
        val amplitude = size * (0.004f + random.nextFloat() * 0.012f)
        val segments = 6
        val path = Path().apply {
            moveTo(-size * 0.05f, baseY)
            for (s in 1..segments) {
                val x = size * 1.1f * s / segments - size * 0.05f
                val prevX = size * 1.1f * (s - 1) / segments - size * 0.05f
                val dir = if ((s + i) % 2 == 0) 1f else -1f
                cubicTo(
                    prevX + (x - prevX) * 0.33f, baseY + amplitude * dir,
                    prevX + (x - prevX) * 0.66f, baseY - amplitude * dir * 0.6f,
                    x, baseY + amplitude * 0.2f * dir,
                )
            }
        }
        GrainStroke(
            path = path,
            width = size * (0.0015f + random.nextFloat() * 0.004f),
            alpha = 0.05f + random.nextFloat() * 0.11f,
        )
    }
}

fun DrawScope.drawBoardSurface(palette: BoardPalette, grain: List<GrainStroke>, cornerRadius: Float) {
    val corner = CornerRadius(cornerRadius, cornerRadius)
    drawRoundRect(
        brush = Brush.linearGradient(
            0f to palette.surfaceLight,
            0.55f to palette.surfaceMid,
            1f to palette.surfaceDark,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        cornerRadius = corner,
    )
    val clip = Path().apply {
        addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, 0f, size.width, size.height, corner))
    }
    clipPath(clip) {
        grain.forEach { stroke ->
            drawPath(
                path = stroke.path,
                color = palette.grain,
                alpha = stroke.alpha * palette.grain.alpha,
                style = Stroke(width = stroke.width, cap = StrokeCap.Round),
            )
        }
        // Soft directional light from the upper left, falling off to the lower right.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.25f, size.height * 0.2f),
                radius = size.width * 0.9f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                0.6f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.16f),
                center = center,
                radius = size.width * 0.75f,
            ),
        )
    }
    // Bevelled edge: light on the top-left, darker on the bottom-right.
    val stroke = size.width * 0.006f
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(palette.edgeHighlight, Color.Transparent, Color.Black.copy(alpha = 0.25f)),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = corner,
        style = Stroke(width = stroke),
    )
}

fun starPoints(lines: Int): List<BoardPosition> = when (lines) {
    13 -> listOf(3 to 3, 3 to 9, 9 to 3, 9 to 9, 6 to 6)
    else -> if (lines % 2 == 1) listOf(lines / 2 to lines / 2) else emptyList()
}.map { (r, c) -> BoardPosition(r, c) }

fun DrawScope.drawGrid(geometry: BoardGeometry, palette: BoardPalette, lineWidth: Float, alpha: Float = 1f) {
    val first = geometry.x(0)
    val last = geometry.x(geometry.lines - 1)
    for (i in 0 until geometry.lines) {
        val outer = i == 0 || i == geometry.lines - 1
        val w = if (outer) lineWidth * 1.7f else lineWidth
        val p = geometry.x(i)
        drawLine(palette.line, Offset(first, p), Offset(last, p), strokeWidth = w, alpha = alpha)
        drawLine(palette.line, Offset(p, first), Offset(p, last), strokeWidth = w, alpha = alpha)
    }
    starPoints(geometry.lines).forEach { pos ->
        drawCircle(
            color = palette.starPoint,
            radius = geometry.spacing * 0.085f,
            center = Offset(geometry.x(pos.col), geometry.y(pos.row)),
            alpha = alpha,
        )
    }
}

private val ObsidianBody = listOf(
    0f to Color(0xFF62666D),
    0.3f to Color(0xFF2E3034),
    0.72f to Color(0xFF141517),
    1f to Color(0xFF060607),
)

private val PearlBody = listOf(
    0f to Color(0xFFFFFFFF),
    0.35f to Color(0xFFF7F2E8),
    0.78f to Color(0xFFE3DBCB),
    1f to Color(0xFFBDBAB3),
)

/**
 * Draws a polished, lit stone: landing shadow, graded body, bevelled rim, specular highlight,
 * and a subtle engraved marking (ring for Player 1, dot for Player 2) so players are
 * distinguishable without relying on colour alone.
 */
fun DrawScope.drawStone(
    center: Offset,
    radius: Float,
    player: Player,
    alpha: Float = 1f,
    lift: Float = 0f,
) {
    if (alpha <= 0f || radius <= 0f) return
    val isOne = player == Player.PLAYER_ONE

    // Shadow: grows slightly and softens while the stone is still "in the air".
    val shadowOffset = Offset(radius * (0.10f + lift * 0.12f), radius * (0.16f + lift * 0.2f))
    val shadowRadius = radius * (1.18f + lift * 0.15f)
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.Black.copy(alpha = 0.50f * (1f - lift * 0.5f)),
            0.72f to Color.Black.copy(alpha = 0.18f * (1f - lift * 0.5f)),
            1f to Color.Transparent,
            center = center + shadowOffset,
            radius = shadowRadius,
        ),
        radius = shadowRadius,
        center = center + shadowOffset,
        alpha = alpha,
    )

    // Body.
    val stops = if (isOne) ObsidianBody else PearlBody
    drawCircle(
        brush = Brush.radialGradient(
            *stops.toTypedArray(),
            center = center + Offset(-radius * 0.38f, -radius * 0.42f),
            radius = radius * 1.65f,
        ),
        radius = radius,
        center = center,
        alpha = alpha,
    )

    // Bevel rim.
    val rimWidth = radius * 0.07f
    drawCircle(
        brush = Brush.linearGradient(
            colors = if (isOne) {
                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent, Color.Black.copy(alpha = 0.5f))
            } else {
                listOf(Color.White.copy(alpha = 0.9f), Color.Transparent, Color(0xFF858C94).copy(alpha = 0.75f))
            },
            start = center - Offset(radius, radius),
            end = center + Offset(radius, radius),
        ),
        radius = radius - rimWidth / 2,
        center = center,
        style = Stroke(width = rimWidth),
        alpha = alpha,
    )

    // Engraved marking.
    if (isOne) {
        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = radius * 0.5f,
            center = center,
            style = Stroke(width = radius * 0.06f),
            alpha = alpha,
        )
    } else {
        drawCircle(
            color = Color(0xFF6E6A62).copy(alpha = 0.22f),
            radius = radius * 0.13f,
            center = center + Offset(radius * 0.01f, radius * 0.02f),
            alpha = alpha,
        )
    }

    // Specular highlight.
    val hlCenter = center + Offset(-radius * 0.36f, -radius * 0.42f)
    rotate(degrees = -38f, pivot = hlCenter) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = if (isOne) 0.42f else 0.95f), Color.Transparent),
                center = hlCenter,
                radius = radius * 0.34f,
            ),
            topLeft = hlCenter - Offset(radius * 0.34f, radius * 0.2f),
            size = Size(radius * 0.68f, radius * 0.4f),
            alpha = alpha,
        )
    }
}

/** Soft winning glow drawn beneath the stones of a winning line. */
fun DrawScope.drawWinGlow(centers: List<Offset>, radius: Float, color: Color, intensity: Float) {
    if (centers.size < 2) return
    drawLine(
        color = color,
        start = centers.first(),
        end = centers.last(),
        strokeWidth = radius * 1.1f,
        cap = StrokeCap.Round,
        alpha = 0.18f + 0.12f * intensity,
    )
    centers.forEach { c ->
        drawCircle(
            brush = Brush.radialGradient(
                0.55f to color.copy(alpha = 0.55f + 0.25f * intensity),
                1f to Color.Transparent,
                center = c,
                radius = radius * 1.55f,
            ),
            radius = radius * 1.55f,
            center = c,
        )
    }
}
