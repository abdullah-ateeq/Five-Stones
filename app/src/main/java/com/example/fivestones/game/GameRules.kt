package com.example.fivestones.game

/**
 * Pure five-in-a-row rules. Every function returns a new state and never mutates its input,
 * which keeps the rules independent from the UI and trivially testable.
 */
object GameRules {

    /** The four axes, each checked in both directions. */
    private val axes = listOf(
        0 to 1,  // horizontal
        1 to 0,  // vertical
        1 to 1,  // diagonal \
        1 to -1, // diagonal /
    )

    fun canPlace(state: FiveStonesState, position: BoardPosition): Boolean =
        !state.isGameOver && position.isInside() && state.stoneAt(position) == null

    /**
     * Places the current player's stone. Returns null when the move is illegal
     * (game over, outside the board, or occupied intersection).
     */
    fun place(state: FiveStonesState, position: BoardPosition): FiveStonesState? {
        if (!canPlace(state, position)) return null

        val player = state.currentPlayer
        val board = state.board.mapIndexed { r, row ->
            if (r == position.row) row.mapIndexed { c, cell -> if (c == position.col) player else cell } else row
        }
        val history = state.moveHistory + position
        val winningLine = findWinningLine(board, position, player)

        return when {
            winningLine != null -> state.copy(
                board = board,
                lastMove = position,
                moveHistory = history,
                winner = player,
                winningPositions = winningLine,
            )
            history.size == TOTAL_INTERSECTIONS -> state.copy(
                board = board,
                lastMove = position,
                moveHistory = history,
                isDraw = true,
            )
            else -> state.copy(
                board = board,
                lastMove = position,
                moveHistory = history,
                currentPlayer = player.opponent,
            )
        }
    }

    /**
     * Removes the most recent stone and restores turn, winner, winning path, draw state and
     * last move by replaying the remaining history. Returns the input when there is nothing to undo.
     */
    fun undo(state: FiveStonesState): FiveStonesState {
        if (state.moveHistory.isEmpty()) return state
        return replay(state.moveHistory.dropLast(1))
    }

    fun replay(moves: List<BoardPosition>): FiveStonesState =
        moves.fold(FiveStonesState()) { acc, move ->
            requireNotNull(place(acc, move)) { "Illegal move in history: $move" }
        }

    /**
     * Returns the full connected line through [position] (ordered end to end) when it contains
     * at least [WIN_LENGTH] stones of [player]; otherwise null. Lines longer than five are
     * returned in their entirety.
     */
    fun findWinningLine(
        board: List<List<Player?>>,
        position: BoardPosition,
        player: Player,
    ): List<BoardPosition>? {
        for ((dr, dc) in axes) {
            val backward = collect(board, position, -dr, -dc, player)
            val forward = collect(board, position, dr, dc, player)
            val line = backward.asReversed() + position + forward
            if (line.size >= WIN_LENGTH) return line
        }
        return null
    }

    private fun collect(
        board: List<List<Player?>>,
        from: BoardPosition,
        dr: Int,
        dc: Int,
        player: Player,
    ): List<BoardPosition> {
        val size = board.size
        val result = mutableListOf<BoardPosition>()
        var r = from.row + dr
        var c = from.col + dc
        while (r in 0 until size && c in 0 until size && board[r][c] == player) {
            result += BoardPosition(r, c)
            r += dr
            c += dc
        }
        return result
    }
}
