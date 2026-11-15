package com.rubiksolver.domain.cube

import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Acceptance criteria: AC-1.1 — Size determines the input layout.
 */
class CubeSizeTest {
    @Test
    fun `AC-1_1 choosing size 2x2 provides 4 stickers per face and 24 in total`() {
        val size = CubeSize.TWO

        size.dimension shouldBe 2
        size.stickersPerFace shouldBe 4
        size.totalStickers shouldBe 24
    }

    @Test
    fun `AC-1_1 choosing size 3x3 provides 9 stickers per face and 54 in total`() {
        val size = CubeSize.THREE

        size.dimension shouldBe 3
        size.stickersPerFace shouldBe 9
        size.totalStickers shouldBe 54
    }
}
