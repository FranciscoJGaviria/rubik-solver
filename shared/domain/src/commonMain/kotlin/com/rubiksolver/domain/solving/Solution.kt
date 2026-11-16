package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Move

data class Solution(
    val size: CubeSize,
    val method: Method,
    val stages: List<Stage>,
) {
    val steps: List<Step>
        get() = stages.flatMap(Stage::steps)

    val moves: List<Move>
        get() = steps.map(Step::move)

    val totalMoves: Int
        get() = steps.size
}
