package com.rubiksolver.domain.solving.strategies

internal object KociembaTables {
    private const val TWIST_STATES = 2187
    private const val FLIP_STATES = 2048
    private const val SLICE_STATES = 495
    private const val CP_STATES = 40320
    private const val UD_EDGES_STATES = 40320
    private const val E_SLICE_PERM_STATES = 24
    private const val LAST_TWIST_INDEX = 6
    private const val LAST_CORNER_INDEX = 7
    private const val LAST_FLIP_INDEX = 10
    private const val LAST_EDGE_INDEX = 11

    val twistMoveTable: Array<IntArray> by lazy {
        Array(TWIST_STATES) { twist ->
            val co = decodeTwist(twist)
            IntArray(Cubie3x3Model.allMoves.size) { moveIdx ->
                val gen = Cubie3x3Model.generators[moveIdx]
                val nextCo = IntArray(8) { t -> (co[gen.cornerSourceSlot[t]] + gen.cornerTwistDelta[t]) % 3 }
                encodeTwist(nextCo)
            }
        }
    }

    val flipMoveTable: Array<IntArray> by lazy {
        Array(FLIP_STATES) { flip ->
            val eo = decodeFlip(flip)
            IntArray(Cubie3x3Model.allMoves.size) { moveIdx ->
                val gen = Cubie3x3Model.generators[moveIdx]
                val nextEo = IntArray(12) { t -> (eo[gen.edgeSourceSlot[t]] + gen.edgeFlipDelta[t]) % 2 }
                encodeFlip(nextEo)
            }
        }
    }

    val sliceMoveTable: Array<IntArray> by lazy {
        Array(SLICE_STATES) { slice ->
            val isE = Combinatorics.decodeCombination4Of12(slice)
            IntArray(Cubie3x3Model.allMoves.size) { moveIdx ->
                val gen = Cubie3x3Model.generators[moveIdx]
                val nextIsE = BooleanArray(12) { t -> isE[gen.edgeSourceSlot[t]] }
                Combinatorics.encodeCombination4Of12(nextIsE)
            }
        }
    }

    val solvedSliceCoord: Int by lazy {
        val eSliceFlags = BooleanArray(12) { idx -> idx >= 8 }
        Combinatorics.encodeCombination4Of12(eSliceFlags)
    }

    val twistDist: IntArray by lazy { Combinatorics.buildBfsTable(TWIST_STATES, 0, twistMoveTable) }
    val flipDist: IntArray by lazy { Combinatorics.buildBfsTable(FLIP_STATES, 0, flipMoveTable) }
    val sliceDist: IntArray by lazy { Combinatorics.buildBfsTable(SLICE_STATES, solvedSliceCoord, sliceMoveTable) }

    val cpMovePhase2: Array<IntArray> by lazy {
        Array(CP_STATES) { cp ->
            val perm = Combinatorics.decodePermutation(cp, 8)
            IntArray(Cubie3x3Model.phase2MoveIndices.size) { p2Idx ->
                val gen = Cubie3x3Model.generators[Cubie3x3Model.phase2MoveIndices[p2Idx]]
                val nextPerm = IntArray(8) { t -> perm[gen.cornerSourceSlot[t]] }
                Combinatorics.encodePermutation(nextPerm)
            }
        }
    }

    val udMovePhase2: Array<IntArray> by lazy {
        Array(UD_EDGES_STATES) { ud ->
            val perm = Combinatorics.decodePermutation(ud, 8)
            IntArray(Cubie3x3Model.phase2MoveIndices.size) { p2Idx ->
                val gen = Cubie3x3Model.generators[Cubie3x3Model.phase2MoveIndices[p2Idx]]
                val nextPerm = IntArray(8) { t -> perm[gen.edgeSourceSlot[t]] }
                Combinatorics.encodePermutation(nextPerm)
            }
        }
    }

    val ePermMovePhase2: Array<IntArray> by lazy {
        Array(E_SLICE_PERM_STATES) { ep ->
            val perm = Combinatorics.decodePermutation(ep, 4)
            IntArray(Cubie3x3Model.phase2MoveIndices.size) { p2Idx ->
                val gen = Cubie3x3Model.generators[Cubie3x3Model.phase2MoveIndices[p2Idx]]
                val nextPerm = IntArray(4) { t -> perm[gen.edgeSourceSlot[t + 8] - 8] }
                Combinatorics.encodePermutation(nextPerm)
            }
        }
    }

    val cpDistPhase2: IntArray by lazy { Combinatorics.buildBfsTable(CP_STATES, 0, cpMovePhase2) }
    val udDistPhase2: IntArray by lazy { Combinatorics.buildBfsTable(UD_EDGES_STATES, 0, udMovePhase2) }
    val ePermDistPhase2: IntArray by lazy { Combinatorics.buildBfsTable(E_SLICE_PERM_STATES, 0, ePermMovePhase2) }

    fun encodeTwist(co: IntArray): Int {
        var code = 0
        for (i in 0..LAST_TWIST_INDEX) {
            code = code * 3 + co[i]
        }
        return code
    }

    private fun decodeTwist(code: Int): IntArray {
        val co = IntArray(8)
        var rem = code
        var sum = 0
        for (i in LAST_TWIST_INDEX downTo 0) {
            co[i] = rem % 3
            sum += co[i]
            rem /= 3
        }
        co[LAST_CORNER_INDEX] = (3 - (sum % 3)) % 3
        return co
    }

    fun encodeFlip(eo: IntArray): Int {
        var code = 0
        for (i in 0..LAST_FLIP_INDEX) {
            code = (code shl 1) or eo[i]
        }
        return code
    }

    private fun decodeFlip(code: Int): IntArray {
        val eo = IntArray(12)
        var rem = code
        var sum = 0
        for (i in LAST_FLIP_INDEX downTo 0) {
            eo[i] = rem and 1
            sum += eo[i]
            rem = rem shr 1
        }
        eo[LAST_EDGE_INDEX] = sum and 1
        return eo
    }

    fun encodeSlice(ep: IntArray): Int {
        val isE = BooleanArray(12) { slot -> ep[slot] >= 8 }
        return Combinatorics.encodeCombination4Of12(isE)
    }
}
