package com.rubiksolver.domain.cube

data class Move(
    val face: Face,
    val direction: TurnDirection = TurnDirection.CLOCKWISE,
) {
    val notation: String
        get() = "${face.symbol}${direction.suffix}"

    fun inverse(): Move = Move(face = face, direction = direction.inverse())

    override fun toString(): String = notation

    companion object {
        fun parse(token: String): Move {
            val trimmed = token.trim()
            require(trimmed.length in 1..2) {
                "Invalid move token: '$token'"
            }
            val face = Face.fromSymbol(trimmed[0])
            val direction =
                when (trimmed.drop(1)) {
                    "" -> TurnDirection.CLOCKWISE
                    "'" -> TurnDirection.COUNTER_CLOCKWISE
                    "2" -> TurnDirection.DOUBLE
                    else -> throw IllegalArgumentException("Invalid move direction in '$token'")
                }
            return Move(face, direction)
        }

        fun parseSequence(notation: String): List<Move> {
            if (notation.isBlank()) return emptyList()
            return notation
                .trim()
                .split(Regex("\\s+"))
                .map(::parse)
        }
    }
}
