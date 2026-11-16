package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.profile.Handedness
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GuideDomainSeedsTest {
    @Test
    fun `AC-3_4 AC-3_7 AC-7_2 and AC-7_4 domain models hold guide metadata`() {
        val holding = HoldingInstruction.fromScheme()
        holding.upFace shouldBe Face.UP
        holding.upColor shouldBe StickerColor.WHITE
        holding.frontFace shouldBe Face.FRONT
        holding.frontColor shouldBe StickerColor.GREEN

        val summary = CompletionSummary(method = Method.BEGINNER, totalMoves = 42)
        summary.celebrated shouldBe true
        summary.method shouldBe Method.BEGINNER
        summary.totalMoves shouldBe 42

        AnimationSpeed.SLOW.durationMillis shouldBe 520L
        AnimationSpeed.NORMAL.durationMillis shouldBe 280L
        AnimationSpeed.FAST.durationMillis shouldBe 150L
        Handedness.entries shouldBe listOf(Handedness.LEFT, Handedness.RIGHT)
    }
}
