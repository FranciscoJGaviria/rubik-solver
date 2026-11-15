package com.rubiksolver.domain.cube

enum class TurnDirection(val suffix: String, val quarterTurnsClockwise: Int) {
    CLOCKWISE("", 1),
    COUNTER_CLOCKWISE("'", 3),
    DOUBLE("2", 2),
    ;

    fun inverse(): TurnDirection =
        when (this) {
            CLOCKWISE -> COUNTER_CLOCKWISE
            COUNTER_CLOCKWISE -> CLOCKWISE
            DOUBLE -> DOUBLE
        }
}
