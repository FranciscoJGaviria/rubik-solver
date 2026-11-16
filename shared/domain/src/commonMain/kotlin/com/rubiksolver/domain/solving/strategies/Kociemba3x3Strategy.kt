package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy
import com.rubiksolver.domain.solving.Stage
import com.rubiksolver.domain.solving.Step

class Kociemba3x3Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.THREE
    override val method: Method = Method.FAST

    override fun solve(state: CubeState): Solution {
        val (phase1, phase2) = ThreeByThreeCoordinateSearch.solveTwoPhase(state)
        val allMoves = phase1 + phase2
        val steps =
            allMoves.map { move ->
                Step(
                    move = move,
                    explanationKey = ExplanationKey.MOVE_ONLY,
                    explanation = null,
                )
            }
        val stages =
            if (steps.isEmpty()) {
                emptyList()
            } else {
                listOf(Stage(name = "Fast Solution", steps = steps))
            }
        return Solution(size = size, method = method, stages = stages)
    }
}
