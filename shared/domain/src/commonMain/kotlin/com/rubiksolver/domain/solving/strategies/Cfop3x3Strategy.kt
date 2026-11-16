package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy

class Cfop3x3Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.THREE
    override val method: Method = Method.CFOP

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
                        name = "Cross",
                        key = ExplanationKey.CROSS_CFOP,
                        template = "Solve bottom cross edges efficiently with",
                    ),
                    StageDescriptor(
                        name = "F2L (First Two Layers)",
                        key = ExplanationKey.F2L_CFOP,
                        template = "Pair and insert corner-edge F2L slot via",
                    ),
                    StageDescriptor(
                        name = "OLL (Orient Last Layer)",
                        key = ExplanationKey.OLL_CFOP,
                        template = "Orient all last-layer stickers using",
                    ),
                    StageDescriptor(
                        name = "PLL (Permute Last Layer)",
                        key = ExplanationKey.PLL_CFOP,
                        template = "Permute last-layer pieces into solved state with",
                    ),
                ),
        )
    }
}
