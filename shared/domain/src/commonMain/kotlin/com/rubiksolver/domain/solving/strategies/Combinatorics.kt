package com.rubiksolver.domain.solving.strategies

internal object Combinatorics {
    private const val FACTORIAL_TABLE_SIZE = 13
    private const val MAX_FACTORIAL_INDEX = 12
    private const val LAST_EDGE_INDEX = 11
    private const val E_SLICE_COUNT = 4
    private const val UNVISITED = -1

    private val factorials =
        IntArray(FACTORIAL_TABLE_SIZE).apply {
            this[0] = 1
            for (i in 1..MAX_FACTORIAL_INDEX) {
                this[i] = this[i - 1] * i
            }
        }

    fun encodePermutation(perm: IntArray): Int {
        val n = perm.size
        var code = 0
        for (i in 0 until n - 1) {
            var smallerAfter = 0
            for (j in i + 1 until n) {
                if (perm[j] < perm[i]) smallerAfter++
            }
            code += smallerAfter * factorials[n - 1 - i]
        }
        return code
    }

    fun decodePermutation(
        code: Int,
        n: Int,
    ): IntArray {
        val available = (0 until n).toMutableList()
        val result = IntArray(n)
        var remainder = code
        for (i in 0 until n) {
            val fact = factorials[n - 1 - i]
            val idx = remainder / fact
            remainder %= fact
            result[i] = available.removeAt(idx)
        }
        return result
    }

    fun binomial(
        n: Int,
        k: Int,
    ): Int {
        if (k < 0 || k > n) return 0
        return factorials[n] / (factorials[k] * factorials[n - k])
    }

    fun encodeCombination4Of12(isSelected: BooleanArray): Int {
        var idx = 0
        var r = E_SLICE_COUNT
        for (i in LAST_EDGE_INDEX downTo 0) {
            if (isSelected[i]) {
                idx += binomial(i, r)
                r--
            }
        }
        return idx
    }

    fun decodeCombination4Of12(code: Int): BooleanArray {
        val result = BooleanArray(MAX_FACTORIAL_INDEX)
        var rem = code
        var r = E_SLICE_COUNT
        for (i in LAST_EDGE_INDEX downTo 0) {
            val c = binomial(i, r)
            if (r > 0 && rem >= c) {
                result[i] = true
                rem -= c
                r--
            }
        }
        return result
    }

    fun buildBfsTable(
        numStates: Int,
        goalState: Int,
        moveTable: Array<IntArray>,
    ): IntArray {
        val dist = IntArray(numStates) { UNVISITED }
        val queue = IntArray(numStates)
        var head = 0
        var tail = 0
        dist[goalState] = 0
        queue[tail++] = goalState
        while (head < tail) {
            val curr = queue[head++]
            val nextDist = dist[curr] + 1
            for (next in moveTable[curr]) {
                if (dist[next] == UNVISITED) {
                    dist[next] = nextDist
                    queue[tail++] = next
                }
            }
        }
        return dist
    }
}
