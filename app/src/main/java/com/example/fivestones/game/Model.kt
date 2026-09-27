package com.example.fivestones.game

const val BOARD_SIZE = 13
const val WIN_LENGTH = 5
const val TOTAL_INTERSECTIONS = BOARD_SIZE * BOARD_SIZE

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO;

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    /** 1-based number used in labels such as "Player 1". */
    val number: Int
        get() = ordinal + 1
}

data class BoardPosition(
    val row: Int,
    val col: Int,
) {
    fun isInside(size: Int = BOARD_SIZE): Boolean = row in 0 until size && col in 0 until size
}

data class FiveStonesState(
    val board: List<List<Player?>> = emptyBoard(),
    val currentPlayer: Player = Player.PLAYER_ONE,
    val lastMove: BoardPosition? = null,
    val winningPositions: List<BoardPosition> = emptyList(),
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val moveHistory: List<BoardPosition> = emptyList(),
    val isAnimating: Boolean = false,
) {
    val isGameOver: Boolean get() = winner != null || isDraw
    val moveCount: Int get() = moveHistory.size
    val canUndo: Boolean get() = moveHistory.isNotEmpty() && !isAnimating && !isGameOver

    fun stoneAt(position: BoardPosition): Player? = board[position.row][position.col]

    fun stonesPlacedBy(player: Player): Int =
        if (player == Player.PLAYER_ONE) (moveCount + 1) / 2 else moveCount / 2
}

fun emptyBoard(size: Int = BOARD_SIZE): List<List<Player?>> = List(size) { List(size) { null } }
