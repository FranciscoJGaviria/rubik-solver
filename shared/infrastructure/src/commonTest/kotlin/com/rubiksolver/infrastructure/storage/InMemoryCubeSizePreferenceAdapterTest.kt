package com.rubiksolver.infrastructure.storage

import com.rubiksolver.domain.cube.CubeSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryCubeSizePreferenceAdapterTest {
    @Test
    fun `default cube size is 3x3`() {
        InMemoryCubeSizePreferenceAdapter().defaultSize() shouldBe CubeSize.THREE
    }
}
