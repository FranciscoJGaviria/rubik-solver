package com.rubiksolver.presentation.guide

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.profile.Handedness
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SolveGuideControllerTest {
    @Test
    fun `AC-7_4 and AC-3_3 to AC-3_7 guide controller supports left-handed layout playback and recovery`() {
        val scrambled = CubeState.solved(CubeSize.TWO).apply(Move.parse("R")).apply(Move.parse("U"))
        val controller =
            SolveGuideController(
                initialState = scrambled,
                initialMethod = Method.OPTIMAL,
                defaultSpeed = AnimationSpeed.SLOW,
                initialHandedness = Handedness.LEFT,
            )

        // AC-7.4 Left-handed layout places main controls on the left side
        controller.state.value.controlsPlacement shouldBe PrimaryControlsPlacement.LEFT
        controller.setHandedness(Handedness.RIGHT)
        controller.state.value.controlsPlacement shouldBe PrimaryControlsPlacement.RIGHT

        // AC-7.2 Default speed is slow
        controller.state.value.session.speed shouldBe AnimationSpeed.SLOW
        controller.setSpeed(AnimationSpeed.FAST)
        controller.state.value.session.speed shouldBe AnimationSpeed.FAST

        // Play / pause and step forward / backward
        controller.togglePlayPause()
        controller.state.value.session.isPlaying shouldBe true
        controller.togglePlayPause()
        controller.state.value.session.isPlaying shouldBe false

        controller.nextStep()
        controller.state.value.session.currentStepNumber shouldBe 1
        controller.previousStep()
        controller.state.value.session.currentStepNumber shouldBe 0

        controller.jumpToStage(0)
        controller.state.value.session.currentStepNumber shouldBe 0

        // Complete all steps -> celebration
        val total = controller.state.value.session.solution.totalMoves
        repeat(total) {
            controller.nextStep()
        }
        controller.state.value.session.completionSummary.shouldNotBeNull()

        // Recover when lost
        val lostState = CubeState.solved(CubeSize.TWO).apply(Move.parse("F"))
        controller.recoverFromLostState(lostState)
        controller.state.value.session.initialCubeState shouldBe lostState
        controller.state.value.session.currentStepNumber shouldBe 0
    }
}
