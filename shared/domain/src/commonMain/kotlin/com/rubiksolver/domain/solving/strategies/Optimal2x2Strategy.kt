package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy
import com.rubiksolver.domain.solving.Stage
import com.rubiksolver.domain.solving.Step

class Optimal2x2Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.TWO
    override val method: Method = Method.OPTIMAL

    override fun solve(state: CubeState): Solution {
        val moves = TwoByTwoCoordinateSearch.solveOptimal(state)
        val steps =
            moves.map { move ->
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
                listOf(Stage(name = "Optimal 2x2 Solution", steps = steps))
            }
        return Solution(size = size, method = method, stages = stages)
    }
}
