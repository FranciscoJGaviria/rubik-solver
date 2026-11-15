package com.rubiksolver.domain.cube

data class CubeState(
    val size: CubeSize,
    private val stickers: Map<StickerPosition, StickerColor>,
) {
    init {
        require(stickers.size == size.totalStickers) {
            "Expected ${size.totalStickers} stickers for ${size.dimension}x${size.dimension}, got ${stickers.size}"
        }
        stickers.keys.forEach { it.validateFor(size) }
    }

    fun stickerAt(position: StickerPosition): StickerColor {
        position.validateFor(size)
        return stickers.getValue(position)
    }

    fun allStickers(): Map<StickerPosition, StickerColor> = stickers

    fun withSticker(
        position: StickerPosition,
        color: StickerColor,
    ): CubeState {
        position.validateFor(size)
        val updated = stickers.toMutableMap()
        updated[position] = color
        return copy(stickers = updated)
    }

    fun apply(move: Move): CubeState {
        var current = stickers
        repeat(move.direction.quarterTurnsClockwise) {
            current = CubeTopology.rotateClockwise(current, size, move.face)
        }
        return copy(stickers = current)
    }

    fun apply(moves: Iterable<Move>): CubeState = moves.fold(this) { acc, move -> acc.apply(move) }

    fun apply(scramble: Scramble): CubeState = apply(scramble.moves)

    fun apply(algorithm: Algorithm): CubeState = apply(algorithm.moves)

    fun isSolved(): Boolean =
        Face.entries.all { face ->
            val firstColor = stickers.getValue(StickerPosition(face, 0, 0))
            for (r in 0 until size.dimension) {
                for (c in 0 until size.dimension) {
                    if (stickers.getValue(StickerPosition(face, r, c)) != firstColor) {
                        return@all false
                    }
                }
            }
            true
        }

    companion object {
        fun solved(
            size: CubeSize,
            scheme: ColorScheme = ColorScheme.STANDARD,
        ): CubeState {
            val map = linkedMapOf<StickerPosition, StickerColor>()
            for (face in Face.entries) {
                val color = scheme.colorOf(face)
                for (r in 0 until size.dimension) {
                    for (c in 0 until size.dimension) {
                        map[StickerPosition(face, r, c)] = color
                    }
                }
            }
            return CubeState(size = size, stickers = map)
        }
    }
}
