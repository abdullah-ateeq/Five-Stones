package com.example.fivestones.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    private fun p(row: Int, col: Int) = BoardPosition(row, col)

    /** Plays [moves] in order (players alternate automatically), failing on any illegal move. */
    private fun play(vararg moves: BoardPosition): FiveStonesState =
        moves.fold(FiveStonesState()) { state, move ->
            assertNotNull("Move $move should be legal", GameRules.place(state, move))
            GameRules.place(state, move)!!
        }

    /** Interleaves Player 1 and Player 2 moves: p1[0], p2[0], p1[1], p2[1], ... */
    private fun interleave(p1: List<BoardPosition>, p2: List<BoardPosition>): Array<BoardPosition> =
        buildList {
            for (i in p1.indices) {
                add(p1[i])
                if (i < p2.size) add(p2[i])
            }
        }.toTypedArray()

    // Player 2 filler moves that are spread out and never form a line.
    private val filler = listOf(p(12, 0), p(12, 3), p(12, 6), p(12, 9), p(12, 12), p(10, 0), p(10, 3))

    /** Player 1 plays [line]; Player 2 answers between moves so Player 1's last stone ends the sequence. */
    private fun playerOneLine(line: List<BoardPosition>) = play(*interleave(line, filler.take(line.size - 1)))

    @Test
    fun initialBoardIsEmpty() {
        val state = FiveStonesState()
        assertEquals(BOARD_SIZE, state.board.size)
        assertTrue(state.board.all { row -> row.size == BOARD_SIZE && row.all { it == null } })
        assertEquals(Player.PLAYER_ONE, state.currentPlayer)
        assertNull(state.winner)
        assertFalse(state.isDraw)
        assertTrue(state.moveHistory.isEmpty())
    }

    @Test
    fun legalPlacementPutsStoneAndRecordsMove() {
        val state = play(p(6, 6))
        assertEquals(Player.PLAYER_ONE, state.stoneAt(p(6, 6)))
        assertEquals(p(6, 6), state.lastMove)
        assertEquals(listOf(p(6, 6)), state.moveHistory)
    }

    @Test
    fun occupiedPositionIsRejected() {
        val state = play(p(6, 6))
        assertNull(GameRules.place(state, p(6, 6)))
    }

    @Test
    fun outsideBoardIsRejected() {
        val state = FiveStonesState()
        assertNull(GameRules.place(state, p(-1, 0)))
        assertNull(GameRules.place(state, p(0, BOARD_SIZE)))
    }

    @Test
    fun turnsAlternate() {
        val afterOne = play(p(0, 0))
        assertEquals(Player.PLAYER_TWO, afterOne.currentPlayer)
        val afterTwo = GameRules.place(afterOne, p(0, 1))!!
        assertEquals(Player.PLAYER_ONE, afterTwo.currentPlayer)
        assertEquals(Player.PLAYER_TWO, afterTwo.stoneAt(p(0, 1)))
    }

    @Test
    fun horizontalFiveWins() {
        val state = playerOneLine((2..6).map { p(4, it) })
        assertEquals(Player.PLAYER_ONE, state.winner)
        assertEquals((2..6).map { p(4, it) }, state.winningPositions)
    }

    @Test
    fun verticalFiveWins() {
        val state = playerOneLine((0..4).map { p(it, 7) })
        assertEquals(Player.PLAYER_ONE, state.winner)
        assertEquals((0..4).map { p(it, 7) }, state.winningPositions)
    }

    @Test
    fun diagonalDownRightFiveWins() {
        // Completed from the middle to verify both directions are counted.
        val line = listOf(p(1, 1), p(2, 2), p(4, 4), p(5, 5), p(3, 3))
        val state = playerOneLine(line)
        assertEquals(Player.PLAYER_ONE, state.winner)
        assertEquals((1..5).map { p(it, it) }, state.winningPositions)
    }

    @Test
    fun diagonalUpRightFiveWins() {
        val line = (0..4).map { p(8 - it, 2 + it) }
        val state = playerOneLine(line)
        assertEquals(Player.PLAYER_ONE, state.winner)
        assertEquals(5, state.winningPositions.size)
        assertTrue(state.winningPositions.containsAll(line))
    }

    @Test
    fun playerTwoCanWin() {
        val p1 = listOf(p(0, 0), p(0, 2), p(0, 4), p(0, 6), p(0, 8))
        val p2 = (3..7).map { p(6, it) }
        val state = play(*interleave(p1, p2))
        assertEquals(Player.PLAYER_TWO, state.winner)
    }

    @Test
    fun sixInARowWinsAndHighlightsWholeLine() {
        val line = listOf(p(5, 0), p(5, 1), p(5, 2), p(5, 4), p(5, 5), p(5, 3))
        val state = playerOneLine(line)
        assertEquals(Player.PLAYER_ONE, state.winner)
        assertEquals((0..5).map { p(5, it) }, state.winningPositions)
    }

    @Test
    fun disconnectedStonesDoNotWin() {
        val stones = listOf(p(3, 0), p(3, 1), p(3, 3), p(3, 4), p(3, 6))
        val state = playerOneLine(stones)
        assertNull(state.winner)
        assertTrue(state.winningPositions.isEmpty())
    }

    @Test
    fun lineBlockedByOpponentDoesNotWin() {
        val p1 = listOf(p(7, 0), p(7, 1), p(7, 2), p(7, 3), p(7, 5))
        val p2 = listOf(p(7, 4), p(12, 0), p(12, 3), p(12, 6))
        val state = play(*interleave(p1, p2))
        assertNull(state.winner)
    }

    @Test
    fun noMovesAfterWin() {
        val state = playerOneLine((2..6).map { p(4, it) })
        assertNull(GameRules.place(state, p(9, 9)))
    }

    @Test
    fun fullBoardWithoutLineIsDraw() {
        // Pattern with runs of at most two in every direction.
        fun owner(r: Int, c: Int) = if ((c + 2 * r) % 4 < 2) Player.PLAYER_ONE else Player.PLAYER_TWO
        val all = (0 until BOARD_SIZE).flatMap { r -> (0 until BOARD_SIZE).map { c -> p(r, c) } }
        var p1 = all.filter { owner(it.row, it.col) == Player.PLAYER_ONE }
        var p2 = all.filter { owner(it.row, it.col) == Player.PLAYER_TWO }
        if (p1.size < p2.size) p1 = p2.also { p2 = p1 } // Player 1 moves first, so needs the extra stone.
        assertEquals(p1.size, p2.size + 1)

        val state = play(*interleave(p1, p2))
        assertTrue(state.isDraw)
        assertNull(state.winner)
        assertEquals(TOTAL_INTERSECTIONS, state.moveCount)
        assertNull(GameRules.place(state, p(0, 0)))
    }

    @Test
    fun undoRemovesLastStoneAndRestoresTurn() {
        val state = play(p(6, 6), p(6, 7))
        val undone = GameRules.undo(state)
        assertNull(undone.stoneAt(p(6, 7)))
        assertEquals(Player.PLAYER_TWO, undone.currentPlayer)
        assertEquals(p(6, 6), undone.lastMove)
        assertEquals(listOf(p(6, 6)), undone.moveHistory)
    }

    @Test
    fun undoOfWinningMoveRestoresOpenGame() {
        val won = playerOneLine((2..6).map { p(4, it) })
        val undone = GameRules.undo(won)
        assertNull(undone.winner)
        assertTrue(undone.winningPositions.isEmpty())
        assertEquals(Player.PLAYER_ONE, undone.currentPlayer)
        assertNull(undone.stoneAt(p(4, 6)))
    }

    @Test
    fun repeatedUndoReturnsToEmptyBoard() {
        var state = play(p(0, 0), p(1, 1), p(2, 2))
        repeat(3) { state = GameRules.undo(state) }
        assertEquals(FiveStonesState(), state)
        assertSame(state, GameRules.undo(state))
    }
}
