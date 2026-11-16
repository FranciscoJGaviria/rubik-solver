package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.cube.TurnDirection
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class SolvingStrategyContractPropertySpec :
    FunSpec({
        val solver = DefaultSolverFactory.create()
        val moveArb =
            Arb.bind(Arb.enum<Face>(), Arb.enum<TurnDirection>()) { face, dir ->
                Move(face, dir)
            }
        val scramble2x2Arb = Arb.list(moveArb, 1..9)
        val scramble3x3Arb = Arb.list(moveArb, 1..6)

        test("AC-3.2.2 and NFR-4 all 2x2 strategies solve any scrambled 2x2 and Optimal is <= 11 moves") {
            checkAll(25, scramble2x2Arb) { moves ->
                val state = CubeState.solved(CubeSize.TWO).apply(Scramble(moves))
                for (method in solver.availableMethods(CubeSize.TWO)) {
                    val solution = solver.solve(state, method)
                    state.apply(solution.moves).isSolved() shouldBe true
                    if (method == Method.OPTIMAL) {
                        solution.totalMoves shouldBeLessThanOrEqual 11
                    }
                }
            }
        }

        test("AC-3.2.2 and NFR-4 all 3x3 strategies solve scrambled 3x3 states and Fast is <= 25 moves") {
            checkAll(25, scramble3x3Arb) { moves ->
                val state = CubeState.solved(CubeSize.THREE).apply(Scramble(moves))
                for (method in solver.availableMethods(CubeSize.THREE)) {
                    val solution = solver.solve(state, method)
                    state.apply(solution.moves).isSolved() shouldBe true
                    if (method == Method.FAST) {
                        solution.totalMoves shouldBeLessThanOrEqual 25
                    }
                }
            }
        }
    })
