package com.rubiksolver.application.recognition

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.recognition.RgbColor
import com.rubiksolver.domain.recognition.ScanOrientationGuide
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

class CameraScanAcceptanceSpec :
    BehaviorSpec({
        val scanUseCase = GuidedScanUseCase()

        Given("the user chose size 3x3 and opened the scanner (AC-1.2.1)") {
            val realCube = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("R U R' U' F"))
            var session = scanUseCase.startSession(CubeSize.THREE)

            When("the user captures all requested faces") {
                for (face in ScanOrientationGuide.CAPTURE_ORDER) {
                    val samples =
                        realCube
                            .allStickers()
                            .filterKeys { it.face == face }
                            .mapValues { (_, color) -> RgbColor.canonicalFor(color) }
                    session = scanUseCase.captureFace(session, face, samples)
                }

                Then("a complete cube state is proposed for review") {
                    val proposed = session.proposedCubeState.shouldNotBeNull()
                    proposed shouldBe realCube
                }
            }
        }

        Given("the user captured the Front face in the scanner (AC-1.2.2)") {
            val solved = CubeState.solved(CubeSize.THREE)
            val session = scanUseCase.startSession(CubeSize.THREE)
            val frontSamples =
                solved
                    .allStickers()
                    .filterKeys { it.face == Face.FRONT }
                    .mapValues { (_, color) -> RgbColor.canonicalFor(color) }

            When("the Front face capture is recorded") {
                val updated = scanUseCase.captureFace(session, Face.FRONT, frontSamples)

                Then("the app tells the user which face to show next and how to rotate the cube") {
                    val guidance = updated.nextFaceGuidance.shouldNotBeNull()
                    guidance.nextFace shouldBe Face.RIGHT
                    guidance.upReferenceFace shouldBe Face.UP
                    guidance.rotationInstruction.shouldNotBeBlank()
                }
            }
        }
    })
