package com.rubiksolver.domain.cube

/**
 * The 8 corner positions of a 2x2 or 3x3 cube.
 * [faces] are ordered clockwise around the corner vertex starting from [Face.UP] or [Face.DOWN].
 */
enum class CornerSlot(
    val primaryFace: Face,
    val secondFace: Face,
    val thirdFace: Face,
) {
    URF(Face.UP, Face.RIGHT, Face.FRONT),
    UFL(Face.UP, Face.FRONT, Face.LEFT),
    ULB(Face.UP, Face.LEFT, Face.BACK),
    UBR(Face.UP, Face.BACK, Face.RIGHT),
    DFR(Face.DOWN, Face.FRONT, Face.RIGHT),
    DLF(Face.DOWN, Face.LEFT, Face.FRONT),
    DBL(Face.DOWN, Face.BACK, Face.LEFT),
    DRB(Face.DOWN, Face.RIGHT, Face.BACK),
    ;

    val faces: List<Face>
        get() = listOf(primaryFace, secondFace, thirdFace)

    fun positions(size: CubeSize): List<StickerPosition> {
        val last = size.dimension - 1
        return when (this) {
            URF ->
                listOf(
                    StickerPosition(Face.UP, last, last),
                    StickerPosition(Face.RIGHT, 0, 0),
                    StickerPosition(Face.FRONT, 0, last),
                )
            UFL ->
                listOf(
                    StickerPosition(Face.UP, last, 0),
                    StickerPosition(Face.FRONT, 0, 0),
                    StickerPosition(Face.LEFT, 0, last),
                )
            ULB ->
                listOf(
                    StickerPosition(Face.UP, 0, 0),
                    StickerPosition(Face.LEFT, 0, 0),
                    StickerPosition(Face.BACK, 0, last),
                )
            UBR ->
                listOf(
                    StickerPosition(Face.UP, 0, last),
                    StickerPosition(Face.BACK, 0, 0),
                    StickerPosition(Face.RIGHT, 0, last),
                )
            DFR ->
                listOf(
                    StickerPosition(Face.DOWN, 0, last),
                    StickerPosition(Face.FRONT, last, last),
                    StickerPosition(Face.RIGHT, last, 0),
                )
            DLF ->
                listOf(
                    StickerPosition(Face.DOWN, 0, 0),
                    StickerPosition(Face.LEFT, last, last),
                    StickerPosition(Face.FRONT, last, 0),
                )
            DBL ->
                listOf(
                    StickerPosition(Face.DOWN, last, 0),
                    StickerPosition(Face.BACK, last, last),
                    StickerPosition(Face.LEFT, last, 0),
                )
            DRB ->
                listOf(
                    StickerPosition(Face.DOWN, last, last),
                    StickerPosition(Face.RIGHT, last, last),
                    StickerPosition(Face.BACK, last, 0),
                )
        }
    }
}
