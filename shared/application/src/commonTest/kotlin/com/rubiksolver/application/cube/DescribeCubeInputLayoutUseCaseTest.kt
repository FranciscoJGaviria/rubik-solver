package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.CubeSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DescribeCubeInputLayoutUseCaseTest {
    private val useCase = DescribeCubeInputLayoutUseCase()

    @Test
    fun `AC-1_1 returns 4 stickers per face when user chooses 2x2`() {
        useCase(CubeSize.TWO) shouldBe 4
    }

    @Test
    fun `AC-1_1 returns 9 stickers per face when user chooses 3x3`() {
        useCase(CubeSize.THREE) shouldBe 9
    }
}
