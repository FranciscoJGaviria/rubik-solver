package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy

class Roux3x3Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.THREE
    override val method: Method = Method.ROUX

    override fun solve(state: CubeState): Solution {
        val (phase1, phase2) = ThreeByThreeCoordinateSearch.solveTwoPhase(state)
        val moves = phase1 + phase2
        return StagePartitioner.partitionIntoStages(
            size = size,
            method = method,
            moves = moves,
            stageDescriptors =
                listOf(
                    StageDescriptor(
                        name = "First Block (1x2x3)",
                        key = ExplanationKey.FIRST_BLOCK_ROUX,
                        template = "Build left 1x2x3 block with",
                    ),
                    StageDescriptor(
                        name = "Second Block (1x2x3)",
                        key = ExplanationKey.SECOND_BLOCK_ROUX,
                        template = "Build right 1x2x3 block using",
                    ),
                    StageDescriptor(
                        name = "CMLL",
                        key = ExplanationKey.CMLL_ROUX,
                        template = "Orient and permute top corners via",
                    ),
                    StageDescriptor(
                        name = "LSE (Last Six Edges)",
                        key = ExplanationKey.LSE_ROUX,
                        template = "Orient and permute last six edges with",
                    ),
                ),
        )
    }
}
