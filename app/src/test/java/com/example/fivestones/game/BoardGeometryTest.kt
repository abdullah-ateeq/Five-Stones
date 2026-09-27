package com.example.fivestones.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoardGeometryTest {

    private val geometry = BoardGeometry(sizePx = 1000f)

    @Test
    fun exactIntersectionMapsToItself() {
        assertEquals(BoardPosition(0, 0), geometry.positionAt(geometry.x(0), geometry.y(0)))
        assertEquals(BoardPosition(6, 8), geometry.positionAt(geometry.x(8), geometry.y(6)))
        assertEquals(BoardPosition(12, 12), geometry.positionAt(geometry.x(12), geometry.y(12)))
    }

    @Test
    fun nearbyTapSnapsToNearestIntersection() {
        val s = geometry.spacing
        assertEquals(BoardPosition(3, 4), geometry.positionAt(geometry.x(4) + s * 0.4f, geometry.y(3) - s * 0.3f))
        assertEquals(BoardPosition(3, 5), geometry.positionAt(geometry.x(4) + s * 0.6f, geometry.y(3)))
    }

    @Test
    fun edgeMarginTapStillMapsToEdgeIntersection() {
        assertEquals(BoardPosition(0, 0), geometry.positionAt(geometry.x(0) - geometry.spacing * 0.45f, geometry.y(0)))
    }

    @Test
    fun farOutsideTapIsRejected() {
        assertNull(geometry.positionAt(2f, 2f))
        assertNull(geometry.positionAt(geometry.x(12) + geometry.spacing * 0.8f, geometry.y(6)))
    }

    @Test
    fun edgeStonesFitInsideBoard() {
        val left = geometry.x(0) - geometry.stoneRadius
        val right = geometry.x(12) + geometry.stoneRadius
        assert(left > 0f && right < geometry.sizePx)
    }
}
