package com.rubiksolver.domain.cube

data class StickerPosition(
    val face: Face,
    val row: Int,
    val col: Int,
) {
    init {
        require(row >= 0 && col >= 0) {
            "Sticker row and col must be non-negative: ($row, $col)"
        }
    }

    fun validateFor(size: CubeSize) {
        require(row in 0 until size.dimension && col in 0 until size.dimension) {
            "Position ($face, $row, $col) is out of bounds for size ${size.dimension}x${size.dimension}"
        }
    }
}
