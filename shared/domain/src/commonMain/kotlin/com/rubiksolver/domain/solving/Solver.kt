package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.profile.ProfileKind

class Solver(
    strategies: Set<SolvingStrategy>,
) {
    private val registry: Map<Pair<CubeSize, Method>, SolvingStrategy> =
        strategies.associateBy { it.size to it.method }

    fun availableMethods(size: CubeSize): Set<Method> =
        registry.keys
            .filter { it.first == size }
            .mapTo(linkedSetOf()) { it.second }

    fun defaultMethod(
        size: CubeSize,
        profileKind: ProfileKind,
        lastUsedMethod: Method? = null,
    ): Method {
        val available = availableMethods(size)
        return if (profileKind == ProfileKind.KID || lastUsedMethod == null || lastUsedMethod !in available) {
            Method.BEGINNER
        } else {
            lastUsedMethod
        }
    }

    fun solve(
        state: CubeState,
        method: Method,
    ): Solution {
        val strategy =
            registry[state.size to method]
                ?: throw UnsupportedMethodException(state.size, method)
        return strategy.solve(state)
    }
}
