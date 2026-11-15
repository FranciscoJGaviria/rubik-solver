package com.rubiksolver.domain

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.learning.HintLevel
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.recognition.FaceScanDescriptor
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BoundedContextSeedsTest {
    @Test
    fun `solving methods match ubiquitous language catalog`() {
        Method.entries.shouldContainExactly(
            Method.BEGINNER,
            Method.ORTEGA,
            Method.CFOP,
            Method.ROUX,
            Method.FAST,
            Method.OPTIMAL,
        )
    }

    @Test
    fun `hint levels ordered from full animation down to none`() {
        HintLevel.entries.shouldContainExactly(
            HintLevel.FULL_ANIMATION,
            HintLevel.NEXT_MOVE,
            HintLevel.NOTATION_ONLY,
            HintLevel.STAGE_NAME,
            HintLevel.NONE,
        )
    }

    @Test
    fun `profile kinds and face scan descriptor preserve immutability`() {
        ProfileKind.entries.shouldContainExactly(ProfileKind.KID, ProfileKind.ADULT)
        val descriptor = FaceScanDescriptor(cubeSize = CubeSize.THREE)
        descriptor.cubeSize shouldBe CubeSize.THREE
    }
}
