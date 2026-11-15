package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Scramble

class ApplyScrambleUseCase {
    operator fun invoke(
        size: CubeSize,
        scrambleNotation: String,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): CubeState {
        val scramble = Scramble.parse(scrambleNotation)
        return CubeState.solved(size = size, scheme = scheme).apply(scramble)
    }
}
