package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.Stage
import com.rubiksolver.domain.solving.Step
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SolveGuidePlaybackAcceptanceSpec :
    BehaviorSpec({
        val manageGuide = ManageSolveGuideUseCase()

        Given("a guide where the user is at step 5 (AC-3.3)") {
            val initialCube = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("R U R' U' F D L"))
            var session = manageGuide.startGuide(initialCube, Method.BEGINNER)
            repeat(5) {
                session = manageGuide.stepForward(session)
            }
            val fifthMove = session.solution.steps[4].move

            When("the user taps previous step") {
                val steppedBack = manageGuide.stepBackward(session)

                Then("the move of step 5 is animated in reverse and step 4 becomes current") {
                    steppedBack.currentStepNumber shouldBe 4
                    steppedBack.lastAnimatedMove shouldBe fifthMove.inverse()
                }
            }
        }

        Given("a new solve guide is started (AC-3.4)") {
            val initialCube = CubeState.solved(CubeSize.THREE).apply(Move.parse("R"))

            When("the guide starts") {
                val session = manageGuide.startGuide(initialCube, Method.BEGINNER)

                Then("the user is told which face to hold up and which to hold in front") {
                    session.holdingInstruction.upFace shouldBe Face.UP
                    session.holdingInstruction.upColor shouldBe StickerColor.WHITE
                    session.holdingInstruction.frontFace shouldBe Face.FRONT
                    session.holdingInstruction.frontColor shouldBe StickerColor.GREEN
                }
            }
        }

        Given("a solution with 3 stages and 40 steps (AC-3.5)") {
            val moveR = Move(Face.RIGHT)
            val makeSteps = { count: Int ->
                List(count) {
                    Step(move = moveR, explanationKey = ExplanationKey.WHITE_CROSS, explanation = "Turn R")
                }
            }
            val stages =
                listOf(
                    Stage(name = "Stage 1", steps = makeSteps(10)),
                    Stage(name = "Stage 2", steps = makeSteps(15)),
                    Stage(name = "Stage 3", steps = makeSteps(15)),
                )
            val fortyStepSolution = Solution(size = CubeSize.THREE, method = Method.BEGINNER, stages = stages)
            val baseSession = manageGuide.startFromSolution(CubeState.solved(CubeSize.THREE), fortyStepSolution)

            When("the user is at step 12 in stage 2") {
                val atStep12 = manageGuide.jumpToStep(baseSession, targetStepNumber = 12)

                Then("the guide shows stage 2 and 12 / 40") {
                    atStep12.currentStageNumber shouldBe 2
                    atStep12.currentStageName shouldBe "Stage 2"
                    atStep12.progressIndicator shouldBe "12 / 40"
                }
            }
        }

        Given("the user is lost at step 20 (AC-3.6)") {
            val initialCube = CubeState.solved(CubeSize.TWO).apply(Scramble.parse("R U R' U' F"))
            val session = manageGuide.startGuide(initialCube, Method.ORTEGA)
            val reenteredRealState = CubeState.solved(CubeSize.TWO).apply(Scramble.parse("U R' F2"))

            When("the user re-enters the current cube state") {
                val recoveredSession = manageGuide.recoverWhenLost(session, reenteredRealState)

                Then("a new solution starts from that state and solves it") {
                    recoveredSession.initialCubeState shouldBe reenteredRealState
                    recoveredSession.currentStepNumber shouldBe 0
                    reenteredRealState.apply(recoveredSession.solution.moves).isSolved() shouldBe true
                }
            }
        }

        Given("a guide approaching its final move (AC-3.7)") {
            val oneMoveScramble = CubeState.solved(CubeSize.TWO).apply(Move.parse("R"))
            val session = manageGuide.startGuide(oneMoveScramble, Method.OPTIMAL)

            When("the last step is applied") {
                val finished = (1..session.solution.totalMoves).fold(session) { acc, _ -> manageGuide.stepForward(acc) }

                Then("a celebration is shown with the method name and the number of moves") {
                    val summary = finished.completionSummary.shouldNotBeNull()
                    summary.celebrated shouldBe true
                    summary.method shouldBe Method.OPTIMAL
                    summary.totalMoves shouldBe session.solution.totalMoves
                    finished.currentCubeState.isSolved() shouldBe true
                }
            }
        }

        Given("the default animation speed is slow (AC-7.2)") {
            val initialCube = CubeState.solved(CubeSize.TWO).apply(Move.parse("U"))

            When("a guide starts with that default speed") {
                val session =
                    manageGuide.startGuide(
                        initialState = initialCube,
                        method = Method.BEGINNER,
                        defaultSpeed = AnimationSpeed.SLOW,
                    )

                Then("moves are played at slow speed") {
                    session.speed shouldBe AnimationSpeed.SLOW
                    session.speed.durationMillis shouldBe 520L
                }
            }
        }
    })
