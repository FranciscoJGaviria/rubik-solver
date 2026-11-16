package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CornerSlot
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.EdgeSlot
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection

internal data class Cubie3x3State(
    val cp: IntArray,
    val co: IntArray,
    val ep: IntArray,
    val eo: IntArray,
)

internal object Cubie3x3Model {
    val allMoves: List<Move> =
        Face.entries.flatMap { face ->
            TurnDirection.entries.map { dir -> Move(face, dir) }
        }

    val phase2MoveIndices: IntArray =
        allMoves
            .mapIndexedNotNull { idx, move ->
                val isUdMove = move.face == Face.UP || move.face == Face.DOWN
                val isHalfTurn = move.direction == TurnDirection.DOUBLE
                if (isUdMove || isHalfTurn) idx else null
            }.toIntArray()

    data class MoveGenerator3x3(
        val cornerSourceSlot: IntArray,
        val cornerTwistDelta: IntArray,
        val edgeSourceSlot: IntArray,
        val edgeFlipDelta: IntArray,
    )

    val generators: List<MoveGenerator3x3> by lazy {
        val solved = CubeState.solved(CubeSize.THREE)
        allMoves.map { move ->
            val after = solved.apply(move)
            val extracted = fromCubeState(after)
            MoveGenerator3x3(
                cornerSourceSlot = extracted.cp,
                cornerTwistDelta = extracted.co,
                edgeSourceSlot = extracted.ep,
                edgeFlipDelta = extracted.eo,
            )
        }
    }

    fun fromCubeState(state: CubeState): Cubie3x3State {
        val scheme = resolveCenterScheme(state)
        val cp = IntArray(8)
        val co = IntArray(8)
        for ((slotIdx, slot) in CornerSlot.entries.withIndex()) {
            val observed = slot.positions(CubeSize.THREE).map(state::stickerAt)
            val (pieceIdx, twist) = matchCorner(observed, scheme)
            cp[slotIdx] = pieceIdx
            co[slotIdx] = twist
        }

        val ep = IntArray(12)
        val eo = IntArray(12)
        for ((slotIdx, slot) in EdgeSlot.entries.withIndex()) {
            val observed = slot.positions().map(state::stickerAt)
            val (pieceIdx, flip) = matchEdge(observed, scheme)
            ep[slotIdx] = pieceIdx
            eo[slotIdx] = flip
        }

        return Cubie3x3State(cp = cp, co = co, ep = ep, eo = eo)
    }

    fun applyMove(
        state: Cubie3x3State,
        moveIdx: Int,
    ): Cubie3x3State {
        val gen = generators[moveIdx]
        val nextCp = IntArray(8) { t -> state.cp[gen.cornerSourceSlot[t]] }
        val nextCo = IntArray(8) { t -> (state.co[gen.cornerSourceSlot[t]] + gen.cornerTwistDelta[t]) % 3 }
        val nextEp = IntArray(12) { t -> state.ep[gen.edgeSourceSlot[t]] }
        val nextEo = IntArray(12) { t -> (state.eo[gen.edgeSourceSlot[t]] + gen.edgeFlipDelta[t]) % 2 }
        return Cubie3x3State(nextCp, nextCo, nextEp, nextEo)
    }

    private fun resolveCenterScheme(state: CubeState): ColorScheme =
        ColorScheme(
            Face.entries.associateWith { face ->
                state.stickerAt(StickerPosition(face, 1, 1))
            },
        )

    private fun matchCorner(
        observed: List<StickerColor>,
        scheme: ColorScheme,
    ): Pair<Int, Int> =
        CornerSlot.entries.withIndex().firstNotNullOf { (pieceIdx, candidate) ->
            val expected = candidate.faces.map(scheme::colorOf)
            (0..2)
                .firstOrNull { twist ->
                    observed[twist] == expected[0] &&
                        observed[(twist + 1) % 3] == expected[1] &&
                        observed[(twist + 2) % 3] == expected[2]
                }?.let { twist -> pieceIdx to twist }
        }

    private fun matchEdge(
        observed: List<StickerColor>,
        scheme: ColorScheme,
    ): Pair<Int, Int> =
        EdgeSlot.entries.withIndex().firstNotNullOf { (pieceIdx, candidate) ->
            val expected0 = scheme.colorOf(candidate.primaryFace)
            val expected1 = scheme.colorOf(candidate.secondaryFace)
            when {
                observed[0] == expected0 && observed[1] == expected1 -> pieceIdx to 0
                observed[0] == expected1 && observed[1] == expected0 -> pieceIdx to 1
                else -> null
            }
        }
}
