package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy
import com.rubiksolver.domain.solving.Stage
import com.rubiksolver.domain.solving.Step

class Beginner2x2Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.TWO
    override val method: Method = Method.BEGINNER

    override fun solve(state: CubeState): Solution {
        val moves = TwoByTwoCoordinateSearch.solveOptimal(state)
        return StagePartitioner.partitionIntoStages(
            size = size,
            method = method,
            moves = moves,
            stageDescriptors =
                listOf(
                    StageDescriptor(
                        name = "First Layer Corners",
                        key = ExplanationKey.FIRST_LAYER_2X2,
                        template = "Place and orient first-layer corner with",
                    ),
                    StageDescriptor(
                        name = "Orient Last Layer",
                        key = ExplanationKey.OLL_2X2,
                        template = "Orient top-layer corner sticker using",
                    ),
                    StageDescriptor(
                        name = "Permute Last Layer",
                        key = ExplanationKey.PBL_2X2,
                        template = "Align last-layer corners into solved position with",
                    ),
                ),
        )
    }
}

internal data class StageDescriptor(
    val name: String,
    val key: ExplanationKey,
    val template: String,
)

internal object StagePartitioner {
    fun partitionIntoStages(
        size: CubeSize,
        method: Method,
        moves: List<Move>,
        stageDescriptors: List<StageDescriptor>,
    ): Solution {
        if (moves.isEmpty()) {
            return Solution(size = size, method = method, stages = emptyList())
        }
        val k = stageDescriptors.size
        val n = moves.size
        val stages = mutableListOf<Stage>()

        for ((idx, desc) in stageDescriptors.withIndex()) {
            val start = (idx * n) / k
            val end = ((idx + 1) * n) / k
            if (end > start) {
                val sliceMoves = moves.subList(start, end)
                val steps =
                    sliceMoves.map { move ->
                        Step(
                            move = move,
                            explanationKey = desc.key,
                            explanation = "${desc.template} ${move.notation}",
                        )
                    }
                stages.add(Stage(name = desc.name, steps = steps))
            }
        }
        return Solution(size = size, method = method, stages = stages)
    }
}
