package com.rubiksolver.presentation.input

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ManualInputControllerTest {
    @Test
    fun `AC-1_4 AC-1_5 AC-3_1 and AC-3_2_2 manual input controller paints scrambles validates and solves`() {
        val controller = ManualInputController()

        controller.selectSize(CubeSize.TWO)
        controller.state.value.cubeState.size shouldBe CubeSize.TWO

        controller.selectBrushColor(StickerColor.RED)
        val pos = StickerPosition(Face.FRONT, 0, 0)
        controller.paintSticker(pos)
        controller.state.value.cubeState.stickerAt(pos) shouldBe StickerColor.RED

        controller.applyScramble("R U R'")
        controller.selectMethod(Method.ORTEGA)
        val solution = controller.generateSolution().shouldNotBeNull()
        solution.method shouldBe Method.ORTEGA
        controller.state.value.cubeState.apply(solution.moves).isSolved() shouldBe true
    }
}
