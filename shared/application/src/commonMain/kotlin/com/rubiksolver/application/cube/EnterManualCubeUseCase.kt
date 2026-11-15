package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.CubeValidator
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationResult

data class ManualCubeSubmission(
    val state: CubeState,
    val validationResult: ValidationResult,
)

class EnterManualCubeUseCase {
    operator fun invoke(
        size: CubeSize,
        paintedStickers: Map<StickerPosition, StickerColor>,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): ManualCubeSubmission {
        val state = CubeState(size = size, stickers = paintedStickers)
        val validationResult = CubeValidator.validate(state, scheme)
        return ManualCubeSubmission(state = state, validationResult = validationResult)
    }
}
