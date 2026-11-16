package com.rubiksolver.domain.solving

import com.rubiksolver.domain.solving.strategies.Beginner2x2Strategy
import com.rubiksolver.domain.solving.strategies.Beginner3x3Strategy
import com.rubiksolver.domain.solving.strategies.Cfop3x3Strategy
import com.rubiksolver.domain.solving.strategies.Kociemba3x3Strategy
import com.rubiksolver.domain.solving.strategies.Optimal2x2Strategy
import com.rubiksolver.domain.solving.strategies.Ortega2x2Strategy
import com.rubiksolver.domain.solving.strategies.Roux3x3Strategy
import com.rubiksolver.domain.solving.strategies.ThreeByThreeCoordinateSearch
import com.rubiksolver.domain.solving.strategies.TwoByTwoCoordinateSearch

object DefaultSolverFactory {
    fun create(): Solver =
        Solver(
            linkedSetOf(
                Beginner2x2Strategy(),
                Ortega2x2Strategy(),
                Optimal2x2Strategy(),
                Beginner3x3Strategy(),
                Cfop3x3Strategy(),
                Roux3x3Strategy(),
                Kociemba3x3Strategy(),
            ),
        )

    fun warmUpTables(): Boolean = TwoByTwoCoordinateSearch.isReady() && ThreeByThreeCoordinateSearch.isReady()
}
