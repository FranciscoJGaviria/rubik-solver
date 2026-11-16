package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerPosition

data class Step(
    val move: Move,
    val explanationKey: ExplanationKey,
    val explanation: String? = null,
    val highlightedPositions: Set<StickerPosition> = emptySet(),
)
