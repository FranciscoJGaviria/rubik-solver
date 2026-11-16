package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor

data class HoldingInstruction(
    val upFace: Face,
    val upColor: StickerColor,
    val frontFace: Face,
    val frontColor: StickerColor,
) {
    companion object {
        fun fromScheme(scheme: ColorScheme = ColorScheme.STANDARD): HoldingInstruction =
            HoldingInstruction(
                upFace = Face.UP,
                upColor = scheme.colorOf(Face.UP),
                frontFace = Face.FRONT,
                frontColor = scheme.colorOf(Face.FRONT),
            )
    }
}
