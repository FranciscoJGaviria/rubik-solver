package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.profile.ProfileKind
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SolverCommonTest {
    private val solver = DefaultSolverFactory.create()

    @Test
    fun `AC-3_1 and AC-3_2_2 solver dispatches strategies on all targets and rejects unsupported methods`() {
        solver.defaultMethod(CubeSize.TWO, ProfileKind.ADULT, null) shouldBe Method.BEGINNER
        solver.defaultMethod(CubeSize.TWO, ProfileKind.ADULT, Method.CFOP) shouldBe Method.BEGINNER
        solver.defaultMethod(CubeSize.THREE, ProfileKind.ADULT, Method.CFOP) shouldBe Method.CFOP

        shouldThrow<UnsupportedMethodException> {
            solver.solve(CubeState.solved(CubeSize.TWO), Method.CFOP)
        }

        val solved2x2 = CubeState.solved(CubeSize.TWO)
        solver.solve(solved2x2, Method.OPTIMAL).totalMoves shouldBe 0

        val scrambled2x2 = solved2x2.apply(Scramble.parse("R U R' U' F"))
        for (method in solver.availableMethods(CubeSize.TWO)) {
            val sol = solver.solve(scrambled2x2, method)
            scrambled2x2.apply(sol.moves).isSolved() shouldBe true
        }

        val scrambled3x3 = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("R U R' F'"))
        for (method in solver.availableMethods(CubeSize.THREE)) {
            val sol = solver.solve(scrambled3x3, method)
            scrambled3x3.apply(sol.moves).isSolved() shouldBe true
        }
    }
}
