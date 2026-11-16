package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.CubeValidator
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.solving.DefaultSolverFactory
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.Solver

class GenerateSolutionUseCase(
    private val solver: Solver = DefaultSolverFactory.create(),
) {
    operator fun invoke(
        state: CubeState,
        method: Method,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): Solution {
        val validation = CubeValidator.validate(state, scheme)
        require(validation !is ValidationResult.Invalid) {
            "Cannot solve an invalid cube state."
        }
        return solver.solve(state, method)
    }
}
