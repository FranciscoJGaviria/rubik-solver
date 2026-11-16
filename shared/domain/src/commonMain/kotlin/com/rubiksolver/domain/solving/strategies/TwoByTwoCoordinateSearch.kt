package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CornerSlot
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.TurnDirection
import kotlin.math.max

internal object TwoByTwoCoordinateSearch {
    private const val CO_STATES = 729
    private const val CP_STATES = 5040
    private const val MAX_OPTIMAL_DEPTH = 11
    private const val MOVABLE_CORNERS_COUNT = 7
    private const val LAST_INDEPENDENT_TWIST_INDEX = 5

    private val movableSlots: List<CornerSlot> =
        listOf(
            CornerSlot.URF,
            CornerSlot.UFL,
            CornerSlot.ULB,
            CornerSlot.UBR,
            CornerSlot.DFR,
            CornerSlot.DLF,
            CornerSlot.DRB,
        )

    val allowedMoves: List<Move> =
        listOf(Face.UP, Face.RIGHT, Face.FRONT).flatMap { face ->
            TurnDirection.entries.map { dir -> Move(face, dir) }
        }

    private data class MoveGenerator(
        val sourceSlotForTarget: IntArray,
        val twistDeltaForTarget: IntArray,
    )

    private val moveGenerators: List<MoveGenerator> by lazy {
        val solved = CubeState.solved(CubeSize.TWO)
        allowedMoves.map { move ->
            val after = solved.apply(move)
            val src = IntArray(MOVABLE_CORNERS_COUNT)
            val delta = IntArray(MOVABLE_CORNERS_COUNT)
            for ((slotIdx, slot) in movableSlots.withIndex()) {
                val observed = slot.positions(CubeSize.TWO).map(after::stickerAt)
                val (pieceIdx, twist) = matchMovableCorner(observed, ColorScheme.STANDARD)
                src[slotIdx] = pieceIdx
                delta[slotIdx] = twist
            }
            MoveGenerator(src, delta)
        }
    }

    private val coMoveTable: Array<IntArray> by lazy { buildCoMoveTable() }
    private val cpMoveTable: Array<IntArray> by lazy { buildCpMoveTable() }
    private val coDistTable: IntArray by lazy { Combinatorics.buildBfsTable(CO_STATES, 0, coMoveTable) }
    private val cpDistTable: IntArray by lazy { Combinatorics.buildBfsTable(CP_STATES, 0, cpMoveTable) }

    fun isReady(): Boolean = coDistTable.isNotEmpty() && cpDistTable.isNotEmpty()

    fun solveOptimal(state: CubeState): List<Move> {
        require(state.size == CubeSize.TWO) { "Expected 2x2 cube state" }
        var result: List<Move> = emptyList()
        if (!state.isSolved()) {
            val scheme = resolveSchemeFromDbl(state)
            val (initialCo, initialCp) = extractCoordinates(state, scheme)
            val path = IntArray(MAX_OPTIMAL_DEPTH)
            val startBound = max(coDistTable[initialCo], cpDistTable[initialCp])
            val winningBound =
                (startBound..MAX_OPTIMAL_DEPTH).firstOrNull { bound ->
                    idaSearch(initialCo, initialCp, 0, bound, -1, path)
                }
            if (winningBound != null) {
                result = (0 until winningBound).map { idx -> allowedMoves[path[idx]] }
            }
        }
        return result
    }

    private fun idaSearch(
        co: Int,
        cp: Int,
        depth: Int,
        bound: Int,
        lastFaceOrdinal: Int,
        path: IntArray,
    ): Boolean {
        val h = max(coDistTable[co], cpDistTable[cp])
        return if (h == 0) {
            depth == bound
        } else if (depth + h > bound) {
            false
        } else {
            allowedMoves.indices.any { moveIdx ->
                val faceOrd = allowedMoves[moveIdx].face.ordinal
                faceOrd != lastFaceOrdinal &&
                    run {
                        path[depth] = moveIdx
                        idaSearch(
                            co = coMoveTable[co][moveIdx],
                            cp = cpMoveTable[cp][moveIdx],
                            depth = depth + 1,
                            bound = bound,
                            lastFaceOrdinal = faceOrd,
                            path = path,
                        )
                    }
            }
        }
    }

    private fun resolveSchemeFromDbl(state: CubeState): ColorScheme {
        val dblPositions = CornerSlot.DBL.positions(CubeSize.TWO)
        val downColor = state.stickerAt(dblPositions[0])
        val backColor = state.stickerAt(dblPositions[1])
        val leftColor = state.stickerAt(dblPositions[2])

        val observedTriples = movableSlots.map { slot -> slot.positions(CubeSize.TWO).map(state::stickerAt) }
        val ulbTriple =
            observedTriples.first { triple ->
                triple.contains(backColor) && triple.contains(leftColor) && !triple.contains(downColor)
            }
        val upColor = ulbTriple.first { it != backColor && it != leftColor }

        val dlfTriple =
            observedTriples.first { triple ->
                triple.contains(downColor) && triple.contains(leftColor) && !triple.contains(backColor)
            }
        val frontColor = dlfTriple.first { it != downColor && it != leftColor }

        val usedColors = setOf(upColor, downColor, frontColor, backColor, leftColor)
        val rightColor = StickerColor.entries.first { it !in usedColors }

        return ColorScheme(
            mapOf(
                Face.UP to upColor,
                Face.DOWN to downColor,
                Face.FRONT to frontColor,
                Face.BACK to backColor,
                Face.LEFT to leftColor,
                Face.RIGHT to rightColor,
            ),
        )
    }

    private fun extractCoordinates(
        state: CubeState,
        scheme: ColorScheme,
    ): Pair<Int, Int> {
        val perm = IntArray(MOVABLE_CORNERS_COUNT)
        val twists = IntArray(MOVABLE_CORNERS_COUNT)

        for ((slotIdx, slot) in movableSlots.withIndex()) {
            val observed = slot.positions(CubeSize.TWO).map(state::stickerAt)
            val (pieceIdx, twist) = matchMovableCorner(observed, scheme)
            perm[slotIdx] = pieceIdx
            twists[slotIdx] = twist
        }

        return encodeTwists(twists) to Combinatorics.encodePermutation(perm)
    }

    private fun matchMovableCorner(
        observed: List<StickerColor>,
        scheme: ColorScheme,
    ): Pair<Int, Int> =
        movableSlots.withIndex().firstNotNullOf { (pieceIdx, candidate) ->
            val expected = candidate.faces.map(scheme::colorOf)
            (0..2)
                .firstOrNull { twist ->
                    observed[twist] == expected[0] &&
                        observed[(twist + 1) % 3] == expected[1] &&
                        observed[(twist + 2) % 3] == expected[2]
                }?.let { twist -> pieceIdx to twist }
        }

    private fun encodeTwists(twists: IntArray): Int {
        var code = 0
        for (i in 0..LAST_INDEPENDENT_TWIST_INDEX) {
            code = code * 3 + twists[i]
        }
        return code
    }

    private fun decodeTwists(code: Int): IntArray {
        val twists = IntArray(MOVABLE_CORNERS_COUNT)
        var rem = code
        var sum = 0
        for (i in LAST_INDEPENDENT_TWIST_INDEX downTo 0) {
            twists[i] = rem % 3
            sum += twists[i]
            rem /= 3
        }
        twists[MOVABLE_CORNERS_COUNT - 1] = (3 - (sum % 3)) % 3
        return twists
    }

    private fun buildCoMoveTable(): Array<IntArray> =
        Array(CO_STATES) { co ->
            val twists = decodeTwists(co)
            IntArray(allowedMoves.size) { moveIdx ->
                val gen = moveGenerators[moveIdx]
                val nextTwists =
                    IntArray(MOVABLE_CORNERS_COUNT) { targetSlot ->
                        val srcSlot = gen.sourceSlotForTarget[targetSlot]
                        (twists[srcSlot] + gen.twistDeltaForTarget[targetSlot]) % 3
                    }
                encodeTwists(nextTwists)
            }
        }

    private fun buildCpMoveTable(): Array<IntArray> =
        Array(CP_STATES) { cp ->
            val perm = Combinatorics.decodePermutation(cp, MOVABLE_CORNERS_COUNT)
            IntArray(allowedMoves.size) { moveIdx ->
                val gen = moveGenerators[moveIdx]
                val nextPerm =
                    IntArray(MOVABLE_CORNERS_COUNT) { targetSlot ->
                        perm[gen.sourceSlotForTarget[targetSlot]]
                    }
                Combinatorics.encodePermutation(nextPerm)
            }
        }
}
