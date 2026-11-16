package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.solving.CompletionSummary
import com.rubiksolver.domain.solving.HoldingInstruction
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.Step

data class SolveGuideSession(
    val initialCubeState: CubeState,
    val currentCubeState: CubeState,
    val solution: Solution,
    val holdingInstruction: HoldingInstruction,
    val currentStepNumber: Int,
    val currentStageNumber: Int,
    val currentStageName: String,
    val lastAnimatedMove: Move? = null,
    val isPlaying: Boolean = false,
    val speed: AnimationSpeed = AnimationSpeed.NORMAL,
    val completionSummary: CompletionSummary? = null,
) {
    val progressIndicator: String
        get() = "$currentStepNumber / ${solution.totalMoves}"

    val currentStep: Step?
        get() =
            if (currentStepNumber in 1..solution.totalMoves) {
                solution.steps[currentStepNumber - 1]
            } else {
                solution.steps.firstOrNull()
            }
}
