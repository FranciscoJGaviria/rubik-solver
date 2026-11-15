package com.rubiksolver.domain.cube

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class CubeGroupInvariantsPropertySpec :
    FunSpec({
        val moveArb: Arb<Move> =
            Arb.bind(Arb.enum<Face>(), Arb.enum<TurnDirection>()) { face, dir ->
                Move(face, dir)
            }
        val moveSeqArb: Arb<List<Move>> = Arb.list(moveArb, 1..25)

        test("applying any move 4 times returns the original 2x2 and 3x3 cube state") {
            checkAll(Arb.enum<CubeSize>(), moveArb) { size, move ->
                val initial = CubeState.solved(size).apply(Move(Face.UP)).apply(Move(Face.RIGHT))
                val afterFour = initial.apply(listOf(move, move, move, move))
                afterFour shouldBe initial
            }
        }

        test("applying any move sequence followed by its inverse restores the initial state (2x2 and 3x3)") {
            checkAll(Arb.enum<CubeSize>(), moveSeqArb) { size, moves ->
                val algorithm = Algorithm(moves)
                val initial = CubeState.solved(size)
                val roundTrip = initial.apply(algorithm).apply(algorithm.inverse())
                roundTrip shouldBe initial
            }
        }

        test("repeating Sexy Move (R U R' U') 6 times restores a solved 2x2 and 3x3 cube") {
            val sexyMove = Algorithm.parse("R U R' U'")
            for (size in CubeSize.entries) {
                var state = CubeState.solved(size)
                repeat(6) {
                    state = state.apply(sexyMove)
                }
                state.isSolved() shouldBe true
            }
        }

        test("any state reachable by legal moves from solved is accepted by CubeValidator (2x2 and 3x3)") {
            checkAll(Arb.enum<CubeSize>(), moveSeqArb) { size, moves ->
                val state = CubeState.solved(size).apply(Scramble(moves))
                val result = CubeValidator.validate(state)
                if (state.isSolved()) {
                    result.shouldBeInstanceOf<ValidationResult.AlreadySolved>()
                } else {
                    result.shouldBeInstanceOf<ValidationResult.Valid>()
                }
            }
        }
    })
