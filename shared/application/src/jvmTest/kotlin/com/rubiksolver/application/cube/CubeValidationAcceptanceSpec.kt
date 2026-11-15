package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.CornerSlot
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.EdgeSlot
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationError
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.profile.ProfileKind
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class CubeValidationAcceptanceSpec :
    BehaviorSpec({
        val validateCube = ValidateCubeUseCase()

        Given("a 3x3 cube state with 10 white stickers (AC-2.1)") {
            val extraWhitePos = StickerPosition(Face.FRONT, 0, 0)
            val invalidState = CubeState.solved(CubeSize.THREE).withSticker(extraWhitePos, StickerColor.WHITE)

            When("the user confirms the cube") {
                val report = validateCube(invalidState, ProfileKind.ADULT)

                Then("the cube is rejected and the user is told that white has one sticker too many") {
                    val invalid = report.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    val countErrors = invalid.errors.filterIsInstance<ValidationError.WrongColorCount>()
                    val whiteError = countErrors.first { it.color == StickerColor.WHITE }
                    whiteError.expected shouldBe 9
                    whiteError.actual shouldBe 10
                    report.messages.first() shouldContain "WHITE"
                }
            }
        }

        Given("a 3x3 cube state with two green centers and equal color counts (AC-2.2)") {
            val frontCenter = StickerPosition(Face.FRONT, 1, 1)
            val backCenter = StickerPosition(Face.BACK, 1, 1)
            val frontCorner = StickerPosition(Face.FRONT, 0, 0)
            val invalidCentersState =
                CubeState
                    .solved(CubeSize.THREE)
                    .withSticker(backCenter, StickerColor.GREEN)
                    .withSticker(frontCorner, StickerColor.BLUE)

            When("the user confirms the cube") {
                val report = validateCube(invalidCentersState, ProfileKind.ADULT)

                Then("the cube is rejected and both centers are highlighted") {
                    val invalid = report.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    val centerError = invalid.errors.filterIsInstance<ValidationError.InvalidCenters>().first()
                    centerError.offendingStickers.contains(frontCenter) shouldBe true
                    centerError.offendingStickers.contains(backCenter) shouldBe true
                }
            }
        }

        Given("a cube state with an impossible corner colored white, yellow and red (AC-2.3)") {
            // Swap a WHITE sticker on UFL with a YELLOW sticker on DLF so counts stay 9 each,
            // but URF/UFL/DLF form impossible opposite-color corners.
            val uflFront = StickerPosition(Face.FRONT, 0, 0) // normally GREEN; make it YELLOW
            val dlfDown = StickerPosition(Face.DOWN, 0, 0) // normally YELLOW; make it GREEN
            val impossiblePieceState =
                CubeState
                    .solved(CubeSize.THREE)
                    .withSticker(uflFront, StickerColor.YELLOW)
                    .withSticker(dlfDown, StickerColor.GREEN)

            When("the user confirms the cube") {
                val report = validateCube(impossiblePieceState, ProfileKind.ADULT)

                Then("the cube is rejected and that corner is highlighted") {
                    val invalid = report.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    val pieceError = invalid.errors.filterIsInstance<ValidationError.ImpossiblePiece>().first()
                    val uflPositions = CornerSlot.UFL.positions(CubeSize.THREE).toSet()
                    pieceError.offendingStickers.intersect(uflPositions).shouldNotBeEmpty()
                }
            }
        }

        Given("a solved 3x3 with one corner twisted in place (AC-2.4)") {
            val urfPositions = CornerSlot.URF.positions(CubeSize.THREE)
            val solved = CubeState.solved(CubeSize.THREE)
            val twistedState =
                solved
                    .withSticker(urfPositions[0], solved.stickerAt(urfPositions[2]))
                    .withSticker(urfPositions[1], solved.stickerAt(urfPositions[0]))
                    .withSticker(urfPositions[2], solved.stickerAt(urfPositions[1]))

            When("the user confirms the cube") {
                val report = validateCube(twistedState, ProfileKind.ADULT)

                Then("the cube is rejected and the user is told a corner may have been twisted by hand") {
                    val invalid = report.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    invalid.errors.filterIsInstance<ValidationError.TwistedCorner>().shouldNotBeEmpty()
                    report.messages.joinToString(" ") shouldContain "twisted by hand"
                }
            }
        }

        Given("a solved 3x3 with one edge flipped or two edges swapped (AC-2.4)") {
            val ufPositions = EdgeSlot.UF.positions()
            val urPositions = EdgeSlot.UR.positions()
            val solved = CubeState.solved(CubeSize.THREE)
            val flippedEdgeState =
                solved
                    .withSticker(ufPositions[0], solved.stickerAt(ufPositions[1]))
                    .withSticker(ufPositions[1], solved.stickerAt(ufPositions[0]))
            val swappedEdgesState =
                solved
                    .withSticker(ufPositions[0], solved.stickerAt(urPositions[0]))
                    .withSticker(ufPositions[1], solved.stickerAt(urPositions[1]))
                    .withSticker(urPositions[0], solved.stickerAt(ufPositions[0]))
                    .withSticker(urPositions[1], solved.stickerAt(ufPositions[1]))

            When("the user confirms those states") {
                val flippedReport = validateCube(flippedEdgeState, ProfileKind.ADULT)
                val parityReport = validateCube(swappedEdgesState, ProfileKind.ADULT)

                Then("both states are rejected for group-theory impossibility") {
                    val flippedInvalid = flippedReport.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    flippedInvalid.errors.filterIsInstance<ValidationError.FlippedEdge>().shouldNotBeEmpty()

                    val parityInvalid = parityReport.result.shouldBeInstanceOf<ValidationResult.Invalid>()
                    parityInvalid
                        .errors
                        .filterIsInstance<ValidationError.PermutationParityMismatch>()
                        .shouldNotBeEmpty()
                }
            }
        }

        Given("a Kid profile and a rejected cube state (AC-2.5)") {
            val urfPositions = CornerSlot.URF.positions(CubeSize.THREE)
            val solved = CubeState.solved(CubeSize.THREE)
            val twistedState =
                solved
                    .withSticker(urfPositions[0], solved.stickerAt(urfPositions[2]))
                    .withSticker(urfPositions[1], solved.stickerAt(urfPositions[0]))
                    .withSticker(urfPositions[2], solved.stickerAt(urfPositions[1]))

            When("the cube is validated for a Kid profile") {
                val kidReport = validateCube(twistedState, ProfileKind.KID)

                Then("the explanation uses short sentences and points at the stickers to check") {
                    kidReport.highlightedStickers.shouldNotBeEmpty()
                    kidReport.messages.first() shouldContain "Oops!"
                }
            }
        }

        Given("a solved cube state versus a valid scrambled cube state (AC-2.6)") {
            val solvedState = CubeState.solved(CubeSize.THREE)
            val scrambledState = solvedState.apply(Move.parse("R"))

            When("the user confirms the solved cube") {
                val solvedReport = validateCube(solvedState, ProfileKind.ADULT)
                val scrambledReport = validateCube(scrambledState, ProfileKind.ADULT)

                Then("the user is told the cube is already solved, while the scrambled cube is valid") {
                    solvedReport.result.shouldBeInstanceOf<ValidationResult.AlreadySolved>()
                    solvedReport.messages.first() shouldContain "already solved"
                    scrambledReport.result.shouldBeInstanceOf<ValidationResult.Valid>()
                }
            }
        }
    })
