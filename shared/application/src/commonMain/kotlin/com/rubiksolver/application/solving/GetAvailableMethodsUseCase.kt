package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.DefaultSolverFactory
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solver

data class AvailableMethodsSelection(
    val available: Set<Method>,
    val defaultMethod: Method,
)

class GetAvailableMethodsUseCase(
    private val solver: Solver = DefaultSolverFactory.create(),
) {
    operator fun invoke(
        size: CubeSize,
        profileKind: ProfileKind = ProfileKind.ADULT,
        lastUsedMethod: Method? = null,
    ): AvailableMethodsSelection =
        AvailableMethodsSelection(
            available = solver.availableMethods(size),
            defaultMethod = solver.defaultMethod(size, profileKind, lastUsedMethod),
        )
}
