package com.rubiksolver.domain.cube

data class Algorithm(
    val moves: List<Move>,
    val name: String? = null,
) {
    val notation: String
        get() = moves.joinToString(" ") { it.notation }

    fun inverse(): Algorithm =
        Algorithm(
            moves = moves.asReversed().map(Move::inverse),
            name = name?.let { "$it (inverse)" },
        )

    companion object {
        fun parse(
            notation: String,
            name: String? = null,
        ): Algorithm = Algorithm(moves = Move.parseSequence(notation), name = name)
    }
}
