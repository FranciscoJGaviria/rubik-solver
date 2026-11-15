package com.rubiksolver.domain.cube

object CubeValidator {
    fun validate(
        state: CubeState,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): ValidationResult {
        val initialErrors = mutableListOf<ValidationError>()
        initialErrors.addAll(checkColorCounts(state))

        val effectiveScheme =
            if (state.size == CubeSize.THREE) {
                val centerOutcome = checkAndResolveCenters(state, scheme)
                if (centerOutcome.error != null) {
                    initialErrors.add(centerOutcome.error)
                }
                centerOutcome.resolvedScheme
            } else {
                scheme
            }

        return if (initialErrors.isNotEmpty()) {
            ValidationResult.Invalid(initialErrors)
        } else {
            validatePiecesAndGroupInvariants(state, effectiveScheme)
        }
    }

    private fun validatePiecesAndGroupInvariants(
        state: CubeState,
        effectiveScheme: ColorScheme,
    ): ValidationResult {
        val cornerAnalysis = analyzeCorners(state, effectiveScheme)
        val edgeAnalysis =
            if (state.size == CubeSize.THREE) {
                analyzeEdges(state, effectiveScheme)
            } else {
                null
            }

        val impossiblePositions = cornerAnalysis.impossibleStickers + edgeAnalysis?.impossibleStickers.orEmpty()
        val errors =
            if (impossiblePositions.isNotEmpty()) {
                listOf(ValidationError.ImpossiblePiece(impossiblePositions))
            } else {
                collectGroupTheoryErrors(state.size, cornerAnalysis, edgeAnalysis)
            }

        return when {
            errors.isNotEmpty() -> ValidationResult.Invalid(errors)
            state.isSolved() -> ValidationResult.AlreadySolved(state)
            else -> ValidationResult.Valid(state)
        }
    }

    private fun collectGroupTheoryErrors(
        size: CubeSize,
        cornerAnalysis: PieceAnalysis,
        edgeAnalysis: PieceAnalysis?,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (cornerAnalysis.orientationSum % 3 != 0) {
            val allCornerStickers = CornerSlot.entries.flatMap { it.positions(size) }.toSet()
            errors.add(ValidationError.TwistedCorner(allCornerStickers))
        }
        if (edgeAnalysis != null) {
            if (edgeAnalysis.orientationSum % 2 != 0) {
                val allEdgeStickers = EdgeSlot.entries.flatMap { it.positions() }.toSet()
                errors.add(ValidationError.FlippedEdge(allEdgeStickers))
            }
            if (permutationParity(cornerAnalysis.permutation) != permutationParity(edgeAnalysis.permutation)) {
                val parityStickers =
                    CornerSlot.entries
                        .take(2)
                        .flatMap { it.positions(size) }
                        .plus(EdgeSlot.entries.take(2).flatMap { it.positions() })
                        .toSet()
                errors.add(ValidationError.PermutationParityMismatch(parityStickers))
            }
        }
        return errors
    }

    private fun checkColorCounts(state: CubeState): List<ValidationError.WrongColorCount> {
        val expected = state.size.stickersPerFace
        val byColor = state.allStickers().entries.groupBy({ it.value }, { it.key })
        return StickerColor.entries.mapNotNull { color ->
            val positions = byColor[color].orEmpty().toSet()
            if (positions.size != expected) {
                ValidationError.WrongColorCount(
                    color = color,
                    expected = expected,
                    actual = positions.size,
                    offendingStickers = positions,
                )
            } else {
                null
            }
        }
    }

    private data class CenterCheckOutcome(
        val resolvedScheme: ColorScheme,
        val error: ValidationError.InvalidCenters? = null,
    )

    private fun checkAndResolveCenters(
        state: CubeState,
        scheme: ColorScheme,
    ): CenterCheckOutcome {
        val centerMap =
            Face.entries.associateWith { face ->
                StickerPosition(face, 1, 1) to state.stickerAt(StickerPosition(face, 1, 1))
            }
        val offending = mutableSetOf<StickerPosition>()

        val byColor = centerMap.values.groupBy({ it.second }, { it.first })
        byColor.values.filter { it.size > 1 }.forEach { dupPositions ->
            offending.addAll(dupPositions)
        }

        for (face in listOf(Face.UP, Face.FRONT, Face.LEFT)) {
            val (posA, colA) = centerMap.getValue(face)
            val (posB, colB) = centerMap.getValue(face.opposite)
            if (!scheme.areOppositeColors(colA, colB)) {
                offending.add(posA)
                offending.add(posB)
            }
        }

        return if (offending.isNotEmpty()) {
            CenterCheckOutcome(
                resolvedScheme = scheme,
                error = ValidationError.InvalidCenters(offending),
            )
        } else {
            val resolvedColors = centerMap.mapValues { it.value.second }
            CenterCheckOutcome(resolvedScheme = ColorScheme(resolvedColors))
        }
    }

    private data class PieceAnalysis(
        val permutation: IntArray,
        val orientationSum: Int,
        val impossibleStickers: Set<StickerPosition>,
    )

    private fun analyzeCorners(
        state: CubeState,
        scheme: ColorScheme,
    ): PieceAnalysis {
        val slots = CornerSlot.entries
        val permutation = IntArray(slots.size)
        val seenPieces = mutableMapOf<CornerSlot, List<StickerPosition>>()
        val impossible = mutableSetOf<StickerPosition>()
        var orientationSum = 0

        for ((slotIdx, slot) in slots.withIndex()) {
            val positions = slot.positions(state.size)
            val observedColors = positions.map(state::stickerAt)
            val match = findMatchingCorner(observedColors, scheme)

            if (match == null) {
                impossible.addAll(positions)
            } else {
                val (targetPiece, twist) = match
                val previousPositions = seenPieces[targetPiece]
                if (previousPositions != null) {
                    impossible.addAll(previousPositions)
                    impossible.addAll(positions)
                } else {
                    seenPieces[targetPiece] = positions
                    permutation[slotIdx] = targetPiece.ordinal
                    orientationSum += twist
                }
            }
        }

        return PieceAnalysis(
            permutation = permutation,
            orientationSum = orientationSum,
            impossibleStickers = impossible,
        )
    }

    private fun findMatchingCorner(
        observedColors: List<StickerColor>,
        scheme: ColorScheme,
    ): Pair<CornerSlot, Int>? =
        CornerSlot.entries.firstNotNullOfOrNull { candidate ->
            val expected = candidate.faces.map(scheme::colorOf)
            (0..2)
                .firstOrNull { twist ->
                    observedColors[twist] == expected[0] &&
                        observedColors[(twist + 1) % 3] == expected[1] &&
                        observedColors[(twist + 2) % 3] == expected[2]
                }?.let { twist -> candidate to twist }
        }

    private fun analyzeEdges(
        state: CubeState,
        scheme: ColorScheme,
    ): PieceAnalysis {
        val slots = EdgeSlot.entries
        val permutation = IntArray(slots.size)
        val seenPieces = mutableMapOf<EdgeSlot, List<StickerPosition>>()
        val impossible = mutableSetOf<StickerPosition>()
        var orientationSum = 0

        for ((slotIdx, slot) in slots.withIndex()) {
            val positions = slot.positions()
            val observedColors = positions.map(state::stickerAt)
            val match = findMatchingEdge(observedColors, scheme)

            if (match == null) {
                impossible.addAll(positions)
            } else {
                val (targetPiece, flip) = match
                val previousPositions = seenPieces[targetPiece]
                if (previousPositions != null) {
                    impossible.addAll(previousPositions)
                    impossible.addAll(positions)
                } else {
                    seenPieces[targetPiece] = positions
                    permutation[slotIdx] = targetPiece.ordinal
                    orientationSum += flip
                }
            }
        }

        return PieceAnalysis(
            permutation = permutation,
            orientationSum = orientationSum,
            impossibleStickers = impossible,
        )
    }

    private fun findMatchingEdge(
        observedColors: List<StickerColor>,
        scheme: ColorScheme,
    ): Pair<EdgeSlot, Int>? =
        EdgeSlot.entries.firstNotNullOfOrNull { candidate ->
            val expected0 = scheme.colorOf(candidate.primaryFace)
            val expected1 = scheme.colorOf(candidate.secondaryFace)
            when {
                observedColors[0] == expected0 && observedColors[1] == expected1 -> candidate to 0
                observedColors[0] == expected1 && observedColors[1] == expected0 -> candidate to 1
                else -> null
            }
        }

    private fun permutationParity(permutation: IntArray): Int {
        var inversions = 0
        for (i in permutation.indices) {
            for (j in i + 1 until permutation.size) {
                if (permutation[i] > permutation[j]) {
                    inversions++
                }
            }
        }
        return inversions % 2
    }
}
