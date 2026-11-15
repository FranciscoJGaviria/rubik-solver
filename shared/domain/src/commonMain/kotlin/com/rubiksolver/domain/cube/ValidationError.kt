package com.rubiksolver.domain.cube

sealed interface ValidationError {
    val offendingStickers: Set<StickerPosition>

    data class WrongColorCount(
        val color: StickerColor,
        val expected: Int,
        val actual: Int,
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError

    data class InvalidCenters(
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError

    data class ImpossiblePiece(
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError

    data class TwistedCorner(
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError

    data class FlippedEdge(
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError

    data class PermutationParityMismatch(
        override val offendingStickers: Set<StickerPosition>,
    ) : ValidationError
}
