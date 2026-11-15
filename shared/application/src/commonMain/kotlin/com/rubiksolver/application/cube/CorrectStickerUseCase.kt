package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition

class CorrectStickerUseCase {
    operator fun invoke(
        state: CubeState,
        position: StickerPosition,
        newColor: StickerColor,
    ): CubeState = state.withSticker(position, newColor)
}
