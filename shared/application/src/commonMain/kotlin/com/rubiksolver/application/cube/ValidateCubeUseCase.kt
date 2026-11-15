package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.CubeValidator
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationError
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.profile.ProfileKind

data class CubeValidationReport(
    val result: ValidationResult,
    val messages: List<String>,
    val highlightedStickers: Set<StickerPosition>,
)

class ValidateCubeUseCase {
    operator fun invoke(
        state: CubeState,
        profileKind: ProfileKind = ProfileKind.ADULT,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): CubeValidationReport {
        val result = CubeValidator.validate(state, scheme)
        return when (result) {
            is ValidationResult.Valid ->
                CubeValidationReport(
                    result = result,
                    messages =
                        listOf(
                            if (profileKind == ProfileKind.KID) {
                                "Great! Your cube is ready to solve."
                            } else {
                                "Cube state is valid and ready to solve."
                            },
                        ),
                    highlightedStickers = emptySet(),
                )
            is ValidationResult.AlreadySolved ->
                CubeValidationReport(
                    result = result,
                    messages =
                        listOf(
                            if (profileKind == ProfileKind.KID) {
                                "Yay! Your cube is already solved!"
                            } else {
                                "The cube is already solved."
                            },
                        ),
                    highlightedStickers = emptySet(),
                )
            is ValidationResult.Invalid -> {
                val highlighted = result.errors.flatMap { it.offendingStickers }.toSet()
                val messages = result.errors.map { explain(it, profileKind) }
                CubeValidationReport(
                    result = result,
                    messages = messages,
                    highlightedStickers = highlighted,
                )
            }
        }
    }

    private fun explain(
        error: ValidationError,
        profileKind: ProfileKind,
    ): String =
        when (error) {
            is ValidationError.WrongColorCount ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Check the ${error.color} squares: we found ${error.actual}, but need ${error.expected}."
                } else {
                    "Color ${error.color} has ${error.actual} stickers instead of ${error.expected}."
                }
            is ValidationError.InvalidCenters ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Look at the middle squares shown: two center colors do not match."
                } else {
                    "Center stickers are duplicated or opposite center colors do not match the color scheme."
                }
            is ValidationError.ImpossiblePiece ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Check the highlighted piece: those colors cannot live on the same piece."
                } else {
                    "Highlighted piece has an impossible or duplicated color combination."
                }
            is ValidationError.TwistedCorner ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Check the corner squares: a corner may have been twisted by hand."
                } else {
                    "Cube is unsolvable: a corner may have been twisted by hand."
                }
            is ValidationError.FlippedEdge ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Check the edge squares: an edge piece may have been flipped."
                } else {
                    "Cube is unsolvable: an edge piece may have been flipped in place."
                }
            is ValidationError.PermutationParityMismatch ->
                if (profileKind == ProfileKind.KID) {
                    "Oops! Two pieces look swapped. Check the highlighted squares."
                } else {
                    "Cube is unsolvable: two pieces appear to be swapped (permutation parity error)."
                }
        }
}
