package com.rubiksolver.domain.cube

/**
 * Distinct geometric symbols per sticker color for color-blind assistance (FR-7.3, AC-7.3).
 */
enum class ColorBlindSymbol {
    CIRCLE,
    TRIANGLE,
    SQUARE,
    DIAMOND,
    STAR,
    PLUS,
    ;

    companion object {
        fun forColor(color: StickerColor): ColorBlindSymbol =
            when (color) {
                StickerColor.WHITE -> CIRCLE
                StickerColor.YELLOW -> TRIANGLE
                StickerColor.GREEN -> SQUARE
                StickerColor.BLUE -> DIAMOND
                StickerColor.RED -> STAR
                StickerColor.ORANGE -> PLUS
            }
    }
}
