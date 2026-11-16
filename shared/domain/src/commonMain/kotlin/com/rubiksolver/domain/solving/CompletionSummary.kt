package com.rubiksolver.domain.solving

data class CompletionSummary(
    val method: Method,
    val totalMoves: Int,
    val celebrated: Boolean = true,
)
