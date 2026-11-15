package com.rubiksolver.domain.cube

/**
 * The 12 edge positions of a 3x3 cube.
 * [primaryFace] is [Face.UP]/[Face.DOWN] for U/D edges, and [Face.FRONT]/[Face.BACK] for E-slice edges.
 */
enum class EdgeSlot(
    val primaryFace: Face,
    val secondaryFace: Face,
) {
    UR(Face.UP, Face.RIGHT),
    UF(Face.UP, Face.FRONT),
    UL(Face.UP, Face.LEFT),
    UB(Face.UP, Face.BACK),
    DR(Face.DOWN, Face.RIGHT),
    DF(Face.DOWN, Face.FRONT),
    DL(Face.DOWN, Face.LEFT),
    DB(Face.DOWN, Face.BACK),
    FR(Face.FRONT, Face.RIGHT),
    FL(Face.FRONT, Face.LEFT),
    BL(Face.BACK, Face.LEFT),
    BR(Face.BACK, Face.RIGHT),
    ;

    val faces: List<Face>
        get() = listOf(primaryFace, secondaryFace)

    fun positions(): List<StickerPosition> =
        when (this) {
            UR -> listOf(StickerPosition(Face.UP, 1, 2), StickerPosition(Face.RIGHT, 0, 1))
            UF -> listOf(StickerPosition(Face.UP, 2, 1), StickerPosition(Face.FRONT, 0, 1))
            UL -> listOf(StickerPosition(Face.UP, 1, 0), StickerPosition(Face.LEFT, 0, 1))
            UB -> listOf(StickerPosition(Face.UP, 0, 1), StickerPosition(Face.BACK, 0, 1))
            DR -> listOf(StickerPosition(Face.DOWN, 1, 2), StickerPosition(Face.RIGHT, 2, 1))
            DF -> listOf(StickerPosition(Face.DOWN, 0, 1), StickerPosition(Face.FRONT, 2, 1))
            DL -> listOf(StickerPosition(Face.DOWN, 1, 0), StickerPosition(Face.LEFT, 2, 1))
            DB -> listOf(StickerPosition(Face.DOWN, 2, 1), StickerPosition(Face.BACK, 2, 1))
            FR -> listOf(StickerPosition(Face.FRONT, 1, 2), StickerPosition(Face.RIGHT, 1, 0))
            FL -> listOf(StickerPosition(Face.FRONT, 1, 0), StickerPosition(Face.LEFT, 1, 2))
            BL -> listOf(StickerPosition(Face.BACK, 1, 2), StickerPosition(Face.LEFT, 1, 0))
            BR -> listOf(StickerPosition(Face.BACK, 1, 0), StickerPosition(Face.RIGHT, 1, 2))
        }
}
