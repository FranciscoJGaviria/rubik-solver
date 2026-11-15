package com.rubiksolver.domain.cube

enum class CubeSize(val dimension: Int) {
    TWO(dimension = 2),
    THREE(dimension = 3),
    ;

    val stickersPerFace: Int
        get() = dimension * dimension

    val totalStickers: Int
        get() = stickersPerFace * FACES_COUNT

    companion object {
        const val FACES_COUNT: Int = 6
    }
}
