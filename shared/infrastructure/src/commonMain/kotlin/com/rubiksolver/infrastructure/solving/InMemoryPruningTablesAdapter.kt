package com.rubiksolver.infrastructure.solving

import com.rubiksolver.domain.solving.DefaultSolverFactory
import com.rubiksolver.domain.solving.PruningTables

class InMemoryPruningTablesAdapter : PruningTables {
    override fun isReady(): Boolean = DefaultSolverFactory.warmUpTables()
}
