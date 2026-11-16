package com.rubiksolver.presentation.input

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ManualInputControllerTest {
    @Test
    fun `AC-1_4 AC-1_5 AC-3_1 and AC-3_2_2 manual input controller starts unpainted paints scans and solves`() {
        val controller = ManualInputController()

        // Starts unpainted on 3x3 (0 / 54 colored)
        controller.state.value.isFullyPainted shouldBe false
        controller.state.value.paintedCount shouldBe 0
        controller.generateSolution().shouldBeNull()

        controller.selectSize(CubeSize.TWO)
        controller.state.value.cubeState.size shouldBe CubeSize.TWO
        controller.state.value.unpaintedPositions.size shouldBe 24

        controller.selectBrushColor(StickerColor.RED)
        val pos = StickerPosition(Face.FRONT, 0, 0)
        controller.paintSticker(pos)
        controller.state.value.cubeState.stickerAt(pos) shouldBe StickerColor.RED
        controller.state.value.paintedCount shouldBe 1

        // Guided 6-face camera scan populates all 6 faces
        controller.selectInputMode(InputMode.CAMERA_SCAN)
        controller.toggleSimulatedCamera(true)
        repeat(6) {
            controller.captureCurrentScanFace()
        }
        controller.state.value.isFullyPainted shouldBe true

        // Scramble also marks all stickers painted and solves
        controller.applyScramble("R U R'")
        controller.state.value.isFullyPainted shouldBe true
        controller.selectMethod(Method.ORTEGA)
        val solution = controller.generateSolution().shouldNotBeNull()
        solution.method shouldBe Method.ORTEGA
        controller.state.value.cubeState.apply(solution.moves).isSolved() shouldBe true
    }
}
