package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class Cube3DCommonTest {
    @Test
    fun `AC-4_1 to AC-4_4 and AC-7_3 3D controller and projection work on all targets`() {
        val controller = CubeViewerController(CubeState.solved(CubeSize.THREE))
        controller.setColorBlindAssistance(true)
        controller.setFreePlayMode(true)
        controller.setCubeSize(CubeSize.TWO)
        controller.state.value.cubeState.size shouldBe CubeSize.TWO

        controller.setCubeSize(CubeSize.THREE)
        controller.orbit(20.0, -10.0)
        controller.resetView()
        controller.state.value.cameraOrbit shouldBe CameraOrbit.DEFAULT

        val move = Move(Face.UP, TurnDirection.CLOCKWISE)
        controller.startMoveAnimation(move)
        controller.stepAnimation(0.5)
        val midScene = controller.projectScene(500.0, 500.0)
        midScene.directionArrow.shouldNotBeNull()
        controller.completeMoveAnimation()

        // Swipe down on left column of Front face applies L
        val moveL =
            controller.onFreePlaySwipe(
                touchedPosition = StickerPosition(Face.FRONT, 1, 0),
                dragX = 0.0,
                dragY = 100.0,
            )
        moveL shouldBe Move(Face.LEFT, TurnDirection.CLOCKWISE)

        // Swipe right on top row of Front face applies U'
        val moveUPrime =
            controller.onFreePlaySwipe(
                touchedPosition = StickerPosition(Face.FRONT, 0, 1),
                dragX = 100.0,
                dragY = 0.0,
            )
        moveUPrime shouldBe Move(Face.UP, TurnDirection.COUNTER_CLOCKWISE)
    }
}
