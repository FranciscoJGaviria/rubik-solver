package com.rubiksolver.domain.cube

data class Scramble(
    val moves: List<Move>,
) {
    val notation: String
        get() = moves.joinToString(" ") { it.notation }

    fun inverse(): Scramble = Scramble(moves = moves.asReversed().map(Move::inverse))

    companion object {
        fun parse(notation: String): Scramble = Scramble(moves = Move.parseSequence(notation))
    }
}
