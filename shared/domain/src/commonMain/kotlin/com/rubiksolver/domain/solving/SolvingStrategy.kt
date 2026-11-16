package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState

interface SolvingStrategy {
    val size: CubeSize
    val method: Method

    fun solve(state: CubeState): Solution
}
