package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import com.rubiksolver.domain.cube.ValidationResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CubeInputAcceptanceSpec :
    BehaviorSpec({
        val correctSticker = CorrectStickerUseCase()
        val enterManualCube = EnterManualCubeUseCase()
        val applyScramble = ApplyScrambleUseCase()

        Given("a proposed cube state where one sticker was read as orange but is red (AC-1.3)") {
            val targetPos = StickerPosition(Face.RIGHT, 0, 0)
            val proposed = CubeState.solved(CubeSize.THREE).withSticker(targetPos, StickerColor.ORANGE)

            When("the user changes that sticker to red") {
                val corrected = correctSticker(proposed, targetPos, StickerColor.RED)

                Then("the cube state shows that sticker as red") {
                    corrected.stickerAt(targetPos) shouldBe StickerColor.RED
                    corrected.isSolved() shouldBe true
                }
            }
        }

        Given("an empty 3x3 cube painted by the user (AC-1.4)") {
            val solvedReference = CubeState.solved(CubeSize.THREE).apply(Move(Face.UP, TurnDirection.CLOCKWISE))
            val paintedStickers = solvedReference.allStickers()

            When("the user paints all 54 stickers and confirms") {
                val submission = enterManualCube(CubeSize.THREE, paintedStickers)

                Then("the cube state is submitted for validation and accepted as valid") {
                    submission.validationResult.shouldBeInstanceOf<ValidationResult.Valid>()
                    submission.state shouldBe solvedReference
                }
            }
        }

        Given("a solved 3x3 cube (AC-1.5)") {
            When("the user enters the scramble \"R U R' U'\"") {
                val scrambled = applyScramble(CubeSize.THREE, "R U R' U'")

                Then("the cube state equals a solved cube after R U R' U'") {
                    val expected =
                        CubeState
                            .solved(CubeSize.THREE)
                            .apply(Move(Face.RIGHT, TurnDirection.CLOCKWISE))
                            .apply(Move(Face.UP, TurnDirection.CLOCKWISE))
                            .apply(Move(Face.RIGHT, TurnDirection.COUNTER_CLOCKWISE))
                            .apply(Move(Face.UP, TurnDirection.COUNTER_CLOCKWISE))
                    scrambled shouldBe expected
                    scrambled.isSolved() shouldBe false
                }
            }
        }

        Given("the user defined a custom scheme with blue in front (AC-1.6)") {
            val customScheme =
                ColorScheme(
                    mapOf(
                        Face.UP to StickerColor.WHITE,
                        Face.DOWN to StickerColor.YELLOW,
                        Face.FRONT to StickerColor.BLUE,
                        Face.BACK to StickerColor.GREEN,
                        Face.LEFT to StickerColor.RED,
                        Face.RIGHT to StickerColor.ORANGE,
                    ),
                )

            When("the user enters a cube using that scheme") {
                val state = applyScramble(CubeSize.TWO, "U", customScheme)
                val solvedCustom = CubeState.solved(CubeSize.TWO, customScheme)

                Then("the custom scheme is used for input and validation") {
                    solvedCustom.stickerAt(StickerPosition(Face.FRONT, 0, 0)) shouldBe StickerColor.BLUE
                    state.stickerAt(StickerPosition(Face.FRONT, 1, 0)) shouldBe StickerColor.BLUE
                }
            }
        }
    })
