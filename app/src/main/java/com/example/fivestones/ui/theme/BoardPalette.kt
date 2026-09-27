package com.example.fivestones.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.fivestones.data.BoardTheme

data class BoardPalette(
    val surfaceLight: Color,
    val surfaceMid: Color,
    val surfaceDark: Color,
    val grain: Color,
    val line: Color,
    val starPoint: Color,
    val edgeHighlight: Color,
    val lastMove: Color,
    val winGlow: Color,
)

object BoardPalettes {
    val ClassicWood = BoardPalette(
        surfaceLight = Color(0xFFE0BC86),
        surfaceMid = Color(0xFFCFA067),
        surfaceDark = Color(0xFFB9854C),
        grain = Color(0xFF7A4E24),
        line = Color(0xD93A2816),
        starPoint = Color(0xFF3A2816),
        edgeHighlight = Color(0x66FFF1D6),
        lastMove = Color(0xFFA8401C),
        winGlow = Color(0xFFFFE3A3),
    )

    val Midnight = BoardPalette(
        surfaceLight = Color(0xFF34404D),
        surfaceMid = Color(0xFF26303B),
        surfaceDark = Color(0xFF1A2129),
        grain = Color(0x669FB2C8),
        line = Color(0x99B4C0CE),
        starPoint = Color(0xFFB4C0CE),
        edgeHighlight = Color(0x33D6E2F0),
        lastMove = Color(0xFFD9B06A),
        winGlow = Color(0xFFE8C98A),
    )

    fun of(theme: BoardTheme): BoardPalette = when (theme) {
        BoardTheme.CLASSIC_WOOD -> ClassicWood
        BoardTheme.MIDNIGHT -> Midnight
    }
}
