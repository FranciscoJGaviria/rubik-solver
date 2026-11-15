package com.rubiksolver.domain.cube

internal object CubeTopology {
    fun rotateClockwise(
        stickers: Map<StickerPosition, StickerColor>,
        size: CubeSize,
        face: Face,
    ): Map<StickerPosition, StickerColor> {
        val n = size.dimension
        val last = n - 1
        val next = stickers.toMutableMap()

        // 1. Rotate the target face matrix 90 degrees clockwise
        for (r in 0 until n) {
            for (c in 0 until n) {
                next[StickerPosition(face, c, last - r)] =
                    stickers.getValue(StickerPosition(face, r, c))
            }
        }

        // 2. Cycle the 4 adjacent face strips (s0 -> s1 -> s2 -> s3 -> s0)
        val strips = adjacentStrips(face, n, last)
        for (i in 0 until n) {
            val s0 = strips[0][i]
            val s1 = strips[1][i]
            val s2 = strips[2][i]
            val s3 = strips[3][i]
            next[s1] = stickers.getValue(s0)
            next[s2] = stickers.getValue(s1)
            next[s3] = stickers.getValue(s2)
            next[s0] = stickers.getValue(s3)
        }
        return next
    }

    private fun adjacentStrips(
        face: Face,
        n: Int,
        last: Int,
    ): List<List<StickerPosition>> =
        when (face) {
            Face.UP ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.BACK, 0, i) },
                    (0 until n).map { i -> StickerPosition(Face.RIGHT, 0, i) },
                    (0 until n).map { i -> StickerPosition(Face.FRONT, 0, i) },
                    (0 until n).map { i -> StickerPosition(Face.LEFT, 0, i) },
                )
            Face.DOWN ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.FRONT, last, i) },
                    (0 until n).map { i -> StickerPosition(Face.RIGHT, last, i) },
                    (0 until n).map { i -> StickerPosition(Face.BACK, last, i) },
                    (0 until n).map { i -> StickerPosition(Face.LEFT, last, i) },
                )
            Face.FRONT ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.UP, last, i) },
                    (0 until n).map { i -> StickerPosition(Face.RIGHT, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.DOWN, 0, last - i) },
                    (0 until n).map { i -> StickerPosition(Face.LEFT, last - i, last) },
                )
            Face.BACK ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.UP, 0, last - i) },
                    (0 until n).map { i -> StickerPosition(Face.LEFT, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.DOWN, last, i) },
                    (0 until n).map { i -> StickerPosition(Face.RIGHT, last - i, last) },
                )
            Face.LEFT ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.UP, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.FRONT, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.DOWN, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.BACK, last - i, last) },
                )
            Face.RIGHT ->
                listOf(
                    (0 until n).map { i -> StickerPosition(Face.UP, last - i, last) },
                    (0 until n).map { i -> StickerPosition(Face.BACK, i, 0) },
                    (0 until n).map { i -> StickerPosition(Face.DOWN, last - i, last) },
                    (0 until n).map { i -> StickerPosition(Face.FRONT, last - i, last) },
                )
        }
}
