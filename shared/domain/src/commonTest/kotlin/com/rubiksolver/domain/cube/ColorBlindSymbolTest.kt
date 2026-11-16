package com.rubiksolver.domain.cube

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ColorBlindSymbolTest {
    @Test
    fun `AC-7_3 every sticker color maps to a unique color-blind symbol`() {
        val symbols = StickerColor.entries.map(ColorBlindSymbol::forColor)
        symbols.toSet().size shouldBe StickerColor.entries.size
        ColorBlindSymbol.forColor(StickerColor.WHITE) shouldBe ColorBlindSymbol.CIRCLE
        ColorBlindSymbol.forColor(StickerColor.YELLOW) shouldBe ColorBlindSymbol.TRIANGLE
        ColorBlindSymbol.forColor(StickerColor.GREEN) shouldBe ColorBlindSymbol.SQUARE
        ColorBlindSymbol.forColor(StickerColor.BLUE) shouldBe ColorBlindSymbol.DIAMOND
        ColorBlindSymbol.forColor(StickerColor.RED) shouldBe ColorBlindSymbol.STAR
        ColorBlindSymbol.forColor(StickerColor.ORANGE) shouldBe ColorBlindSymbol.PLUS
    }
}
