package com.rubiksolver.domain.cube

enum class Face(val symbol: Char) {
    UP('U'),
    DOWN('D'),
    FRONT('F'),
    BACK('B'),
    LEFT('L'),
    RIGHT('R'),
    ;

    val opposite: Face
        get() =
            when (this) {
                UP -> DOWN
                DOWN -> UP
                FRONT -> BACK
                BACK -> FRONT
                LEFT -> RIGHT
                RIGHT -> LEFT
            }

    companion object {
        fun fromSymbol(symbol: Char): Face =
            entries.firstOrNull { it.symbol == symbol }
                ?: throw IllegalArgumentException("Unknown face symbol: $symbol")
    }
}
