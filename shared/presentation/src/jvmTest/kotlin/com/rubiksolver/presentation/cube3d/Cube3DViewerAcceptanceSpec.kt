package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.ColorBlindSymbol
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class Cube3DViewerAcceptanceSpec :
    BehaviorSpec({
        Given("a 3D cube viewer initialized on a solved 3x3 cube (AC-4.1)") {
            val controller = CubeViewerController(CubeState.solved(CubeSize.THREE))

            When("the cube state changes by one move R and the animation finishes") {
                val move = Move(Face.RIGHT, TurnDirection.CLOCKWISE)
                controller.startMoveAnimation(move)
                controller.stepAnimation(0.5)
                controller.completeMoveAnimation()
                val projected = controller.projectScene(viewportWidth = 800.0, viewportHeight = 800.0)

                Then("the 3D cube shows the new state after the animation") {
                    controller.state.value.cubeState shouldBe CubeState.solved(CubeSize.THREE).apply(move)
                    controller.state.value.activeAnimation.shouldBeNull()
                    val upRightSticker =
                        projected.visibleQuads.first {
                            it.position == StickerPosition(Face.UP, 2, 2)
                        }
                    upRightSticker.color shouldBe StickerColor.GREEN
                }
            }
        }

        Given("the user rotated the cube to see the Back face (AC-4.2)") {
            val initialCube = CubeState.solved(CubeSize.THREE).apply(Move.parse("F"))
            val controller = CubeViewerController(initialCube)
            controller.orbit(deltaYawDegrees = 180.0, deltaPitchDegrees = -15.0)

            When("the user resets the view") {
                controller.resetView()

                Then("the default view is restored and the cube state is unchanged") {
                    controller.state.value.cameraOrbit shouldBe CameraOrbit.DEFAULT
                    controller.state.value.cubeState shouldBe initialCube
                }
            }
        }

        Given("the move U' is played on the 3D cube (AC-4.3)") {
            val controller = CubeViewerController(CubeState.solved(CubeSize.THREE))
            val moveUPrime = Move(Face.UP, TurnDirection.COUNTER_CLOCKWISE)

            When("the move animation is in progress") {
                controller.startMoveAnimation(moveUPrime)
                controller.stepAnimation(0.5)
                val projected = controller.projectScene(viewportWidth = 800.0, viewportHeight = 800.0)

                Then("the Up layer turns counter-clockwise and an arrow shows the direction") {
                    val arrow = projected.directionArrow.shouldNotBeNull()
                    arrow.face shouldBe Face.UP
                    arrow.direction shouldBe TurnDirection.COUNTER_CLOCKWISE
                    arrow.points.size shouldBe 9
                }
            }
        }

        Given("free-play mode on a solved 3x3 cube (AC-4.4)") {
            val controller = CubeViewerController(CubeState.solved(CubeSize.THREE))
            controller.setFreePlayMode(true)

            When("the user swipes the Right layer upward on the Front face") {
                val appliedMove =
                    controller.onFreePlaySwipe(
                        touchedPosition = StickerPosition(Face.FRONT, 1, 2),
                        dragX = 0.0,
                        dragY = -120.0,
                    )

                Then("the move R is applied") {
                    appliedMove shouldBe Move(Face.RIGHT, TurnDirection.CLOCKWISE)
                    controller.state.value.cubeState shouldBe
                        CubeState.solved(CubeSize.THREE).apply(Move(Face.RIGHT, TurnDirection.CLOCKWISE))
                }
            }
        }

        Given("color-blind assistance is turned on in the 3D viewer (AC-7.3)") {
            val controller = CubeViewerController(CubeState.solved(CubeSize.TWO))
            controller.setColorBlindAssistance(true)

            When("the 3D scene is projected") {
                val scene = controller.projectScene(viewportWidth = 600.0, viewportHeight = 600.0)

                Then("every visible sticker shows the symbol of its color") {
                    scene.visibleQuads.forEach { quad ->
                        quad.symbol shouldBe ColorBlindSymbol.forColor(quad.color)
                    }
                }
            }
        }
    })
