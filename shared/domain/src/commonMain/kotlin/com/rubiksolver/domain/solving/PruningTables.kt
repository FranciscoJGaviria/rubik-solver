package com.rubiksolver.domain.solving

/**
 * Domain port providing precomputed heuristic pruning distances for coordinate solvers.
 */
interface PruningTables {
    fun isReady(): Boolean
}
