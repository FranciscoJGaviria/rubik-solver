package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.SolvingStrategy

class Beginner3x3Strategy : SolvingStrategy {
    override val size: CubeSize = CubeSize.THREE
    override val method: Method = Method.BEGINNER

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
                        name = "White Cross",
                        key = ExplanationKey.WHITE_CROSS,
                        template = "Align white edge with its side center using",
                    ),
                    StageDescriptor(
                        name = "White Corners",
                        key = ExplanationKey.WHITE_CORNERS,
                        template = "Insert first-layer corner into slot with",
                    ),
                    StageDescriptor(
                        name = "Middle Layer Edges",
                        key = ExplanationKey.MIDDLE_EDGES,
                        template = "Place middle-layer edge without disturbing white face via",
                    ),
                    StageDescriptor(
                        name = "Yellow Cross & Edges",
                        key = ExplanationKey.YELLOW_CROSS,
                        template = "Orient and align last-layer yellow edges with",
                    ),
                    StageDescriptor(
                        name = "Yellow Corners",
                        key = ExplanationKey.YELLOW_CORNERS_ORIENT,
                        template = "Position and orient final corners using",
                    ),
                ),
        )
    }
}
