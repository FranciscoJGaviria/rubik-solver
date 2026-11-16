package com.rubiksolver.domain.solving.strategies

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Move

internal object ThreeByThreeCoordinateSearch {
    private const val MAX_PHASE1_DEPTH = 12
    private const val MAX_PHASE2_DEPTH = 13
    private const val MAX_TOTAL_MOVES = 25

    private data class Phase1Node(
        val cubie: Cubie3x3State,
        val twist: Int,
        val flip: Int,
        val slice: Int,
        val depth: Int,
        val lastFaceOrdinal: Int,
    )

    private data class Phase2Node(
        val cp: Int,
        val ud: Int,
        val ep: Int,
        val depth: Int,
        val lastFaceOrdinal: Int,
    )

    fun isReady(): Boolean = KociembaTables.twistDist.isNotEmpty() && KociembaTables.cpDistPhase2.isNotEmpty()

    fun solveTwoPhase(state: CubeState): Pair<List<Move>, List<Move>> {
        require(state.size == CubeSize.THREE) { "Expected 3x3 cube state" }
        var result: Pair<List<Move>, List<Move>> = emptyList<Move>() to emptyList()
        if (!state.isSolved()) {
            val initialCubie = Cubie3x3Model.fromCubeState(state)
            val root =
                Phase1Node(
                    cubie = initialCubie,
                    twist = KociembaTables.encodeTwist(initialCubie.co),
                    flip = KociembaTables.encodeFlip(initialCubie.eo),
                    slice = KociembaTables.encodeSlice(initialCubie.ep),
                    depth = 0,
                    lastFaceOrdinal = -1,
                )
            val phase1Path = IntArray(MAX_PHASE1_DEPTH)
            val phase2Path = IntArray(MAX_PHASE2_DEPTH)
            val startBound1 =
                maxOf(
                    KociembaTables.twistDist[root.twist],
                    KociembaTables.flipDist[root.flip],
                    KociembaTables.sliceDist[root.slice],
                )
            val found =
                (startBound1..MAX_PHASE1_DEPTH).firstNotNullOfOrNull { bound1 ->
                    searchPhase1(root, bound1, phase1Path, phase2Path)
                }
            if (found != null) {
                result = found
            }
        }
        return result
    }

    private fun searchPhase1(
        node: Phase1Node,
        bound: Int,
        phase1Path: IntArray,
        phase2Path: IntArray,
    ): Pair<List<Move>, List<Move>>? {
        val h =
            maxOf(
                KociembaTables.twistDist[node.twist],
                KociembaTables.flipDist[node.flip],
                KociembaTables.sliceDist[node.slice],
            )
        return if (h == 0 && node.depth == bound) {
            trySolvePhase2FromG1(node, phase1Path, phase2Path)
        } else if (node.depth + h > bound) {
            null
        } else {
            expandPhase1Children(node, bound, phase1Path, phase2Path)
        }
    }

    private fun expandPhase1Children(
        node: Phase1Node,
        bound: Int,
        phase1Path: IntArray,
        phase2Path: IntArray,
    ): Pair<List<Move>, List<Move>>? =
        Cubie3x3Model.allMoves.indices.firstNotNullOfOrNull { moveIdx ->
            val faceOrd = Cubie3x3Model.allMoves[moveIdx].face.ordinal
            if (faceOrd == node.lastFaceOrdinal) {
                null
            } else {
                phase1Path[node.depth] = moveIdx
                val child =
                    Phase1Node(
                        cubie = Cubie3x3Model.applyMove(node.cubie, moveIdx),
                        twist = KociembaTables.twistMoveTable[node.twist][moveIdx],
                        flip = KociembaTables.flipMoveTable[node.flip][moveIdx],
                        slice = KociembaTables.sliceMoveTable[node.slice][moveIdx],
                        depth = node.depth + 1,
                        lastFaceOrdinal = faceOrd,
                    )
                searchPhase1(child, bound, phase1Path, phase2Path)
            }
        }

    private fun trySolvePhase2FromG1(
        g1Node: Phase1Node,
        phase1Path: IntArray,
        phase2Path: IntArray,
    ): Pair<List<Move>, List<Move>>? {
        val root2 =
            Phase2Node(
                cp = Combinatorics.encodePermutation(g1Node.cubie.cp),
                ud = Combinatorics.encodePermutation(IntArray(8) { g1Node.cubie.ep[it] }),
                ep = Combinatorics.encodePermutation(IntArray(4) { g1Node.cubie.ep[it + 8] - 8 }),
                depth = 0,
                lastFaceOrdinal = g1Node.lastFaceOrdinal,
            )
        val startBound2 =
            maxOf(
                KociembaTables.cpDistPhase2[root2.cp],
                KociembaTables.udDistPhase2[root2.ud],
                KociembaTables.ePermDistPhase2[root2.ep],
            )
        val maxAllowedPhase2 = (MAX_TOTAL_MOVES - g1Node.depth).coerceAtMost(MAX_PHASE2_DEPTH)

        val winningBound2 =
            (startBound2..maxAllowedPhase2).firstOrNull { bound2 ->
                searchPhase2(root2, bound2, phase2Path)
            }
        return winningBound2?.let { bound2 ->
            val p1Moves = (0 until g1Node.depth).map { Cubie3x3Model.allMoves[phase1Path[it]] }
            val p2Moves =
                (0 until bound2).map { idx ->
                    val p2MoveIdx = phase2Path[idx]
                    Cubie3x3Model.allMoves[Cubie3x3Model.phase2MoveIndices[p2MoveIdx]]
                }
            p1Moves to p2Moves
        }
    }

    private fun searchPhase2(
        node: Phase2Node,
        bound: Int,
        phase2Path: IntArray,
    ): Boolean {
        val h =
            maxOf(
                KociembaTables.cpDistPhase2[node.cp],
                KociembaTables.udDistPhase2[node.ud],
                KociembaTables.ePermDistPhase2[node.ep],
            )
        return if (h == 0) {
            node.depth == bound
        } else if (node.depth + h > bound) {
            false
        } else {
            val p2Indices = Cubie3x3Model.phase2MoveIndices
            p2Indices.indices.any { p2Idx ->
                val globalMoveIdx = p2Indices[p2Idx]
                val faceOrd = Cubie3x3Model.allMoves[globalMoveIdx].face.ordinal
                faceOrd != node.lastFaceOrdinal &&
                    run {
                        phase2Path[node.depth] = p2Idx
                        val child =
                            Phase2Node(
                                cp = KociembaTables.cpMovePhase2[node.cp][p2Idx],
                                ud = KociembaTables.udMovePhase2[node.ud][p2Idx],
                                ep = KociembaTables.ePermMovePhase2[node.ep][p2Idx],
                                depth = node.depth + 1,
                                lastFaceOrdinal = faceOrd,
                            )
                        searchPhase2(child, bound, phase2Path)
                    }
            }
        }
    }
}
