package com.example.fivestones.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fivestones.R
import com.example.fivestones.game.BoardGeometry
import com.example.fivestones.game.Player
import com.example.fivestones.ui.board.buildGrain
import com.example.fivestones.ui.board.drawBoardSurface
import com.example.fivestones.ui.board.drawGrid
import com.example.fivestones.ui.board.drawStone
import com.example.fivestones.ui.board.drawWinGlow
import com.example.fivestones.ui.theme.BoardPalettes
import com.example.fivestones.ui.theme.SplashBackground

private const val TOTAL_MILLIS = 1550
private const val LINES = 7

/** (row, col, player, appear time in ms). The final Player 1 stone completes the diagonal. */
private val Sequence = listOf(
    Triple(3 to 3, Player.PLAYER_ONE, 280),
    Triple(2 to 4, Player.PLAYER_TWO, 360),
    Triple(2 to 2, Player.PLAYER_ONE, 440),
    Triple(4 to 2, Player.PLAYER_TWO, 520),
    Triple(4 to 4, Player.PLAYER_ONE, 600),
    Triple(1 to 3, Player.PLAYER_TWO, 680),
    Triple(1 to 1, Player.PLAYER_ONE, 760),
    Triple(5 to 3, Player.PLAYER_TWO, 840),
    Triple(5 to 5, Player.PLAYER_ONE, 960),
)
private val WinningLine = listOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5)

private const val GRID_FADE_END = 300f
private const val STONE_POP = 130f
private const val GLOW_START = 1090f
private const val TITLE_START = 1000f

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val time = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        time.animateTo(TOTAL_MILLIS.toFloat(), tween(TOTAL_MILLIS, easing = LinearEasing))
        finish()
    }
    val palette = BoardPalettes.ClassicWood

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            modifier = Modifier
                .size(188.dp)
                .graphicsLayer { alpha = (time.value / GRID_FADE_END).coerceIn(0f, 1f) }
                .shadow(16.dp, RoundedCornerShape(10.dp))
                .drawWithCache {
                    val grain = buildGrain(size.width)
                    val geometry = BoardGeometry(size.width, LINES)
                    onDrawBehind {
                        drawBoardSurface(palette, grain, 10.dp.toPx())
                        drawGrid(geometry, palette, 1.dp.toPx())
                    }
                },
        ) {
            val t = time.value
            val geometry = BoardGeometry(size.width, LINES)
            val radius = geometry.stoneRadius
            fun center(p: Pair<Int, Int>) = Offset(geometry.x(p.second), geometry.y(p.first))

            val glow = ((t - GLOW_START) / 260f).coerceIn(0f, 1f)
            if (glow > 0f) {
                drawWinGlow(WinningLine.map(::center), radius, palette.winGlow, glow)
            }
            Sequence.forEach { (pos, player, appearAt) ->
                val p = ((t - appearAt) / STONE_POP).coerceIn(0f, 1f)
                if (p > 0f) {
                    drawStone(center(pos), radius * (0.8f + 0.2f * p), player, alpha = p, lift = 1f - p)
                }
            }
            if (glow > 0f) {
                drawLine(
                    color = palette.winGlow,
                    start = center(WinningLine.first()),
                    end = center(WinningLine.first()) + (center(WinningLine.last()) - center(WinningLine.first())) * glow,
                    strokeWidth = radius * 0.14f,
                    cap = StrokeCap.Round,
                    alpha = 0.75f,
                )
            }
        }
        Spacer(Modifier.height(36.dp))
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.displayMedium,
            color = SplashTitle,
            modifier = Modifier.graphicsLayer {
                val p = ((time.value - TITLE_START) / 380f).coerceIn(0f, 1f)
                alpha = p
                translationY = (1f - p) * 12.dp.toPx()
            },
        )
    }
}

private val SplashTitle = androidx.compose.ui.graphics.Color(0xFFE9E4DA)
