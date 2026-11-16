package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy

class Ortega2x2Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.TWO
    override val method: Method = Method.ORTEGA

    override fun solve(state: CubeState): Solution {
        val moves = TwoByTwoCoordinateSearch.solveOptimal(state)
        return StagePartitioner.partitionIntoStages(
            size = size,
            method = method,
            moves = moves,
            stageDescriptors =
                listOf(
                    StageDescriptor(
                        name = "First Face",
                        key = ExplanationKey.FIRST_LAYER_2X2,
                        template = "Build the bottom face regardless of permutation with",
                    ),
                    StageDescriptor(
                        name = "OLL (Orient Last Layer)",
                        key = ExplanationKey.OLL_2X2,
                        template = "Orient the opposite face in one look using",
                    ),
                    StageDescriptor(
                        name = "PBL (Permute Both Layers)",
                        key = ExplanationKey.PBL_2X2,
                        template = "Permute both layers simultaneously with",
                    ),
                ),
        )
    }
}
