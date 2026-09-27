package com.example.fivestones.game

import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Maps between board intersections and pixel coordinates for a square board of [sizePx].
 * A margin of [marginRatio] × spacing is kept around the outer lines so edge stones are not clipped.
 */
class BoardGeometry(
    val sizePx: Float,
    val lines: Int = BOARD_SIZE,
    marginRatio: Float = 0.85f,
) {
    val spacing: Float = sizePx / (lines - 1 + 2 * marginRatio)
    val origin: Float = spacing * marginRatio
    val stoneRadius: Float = spacing * 0.46f

    fun x(col: Int): Float = origin + col * spacing
    fun y(row: Int): Float = origin + row * spacing

    /**
     * Nearest intersection to a touch point, or null when the touch is further than
     * [tolerance] × spacing from any intersection (e.g. deep in the board margin).
     */
    fun positionAt(px: Float, py: Float, tolerance: Float = 0.72f): BoardPosition? {
        val col = ((px - origin) / spacing).roundToInt()
        val row = ((py - origin) / spacing).roundToInt()
        if (row !in 0 until lines || col !in 0 until lines) return null
        val dx = px - x(col)
        val dy = py - y(row)
        return if (sqrt(dx * dx + dy * dy) <= spacing * tolerance) BoardPosition(row, col) else null
    }
}
