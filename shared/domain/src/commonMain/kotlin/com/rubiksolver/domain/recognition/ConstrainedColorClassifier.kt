package com.rubiksolver.domain.recognition

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition

/**
 * Classifies 2x2 or 3x3 CIELAB facelet samples into a [CubeState] subject to exact
 * sticker-count cardinality (N^2 per color) anchored on centers/scheme (FR-1.2, ADR-05).
 */
object ConstrainedColorClassifier {
    fun classify(
        size: CubeSize,
        samples: Map<StickerPosition, LabColor>,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): CubeState {
        require(samples.size == size.totalStickers) {
            "Expected ${size.totalStickers} samples, got ${samples.size}"
        }

        val anchors: Map<StickerColor, LabColor> = resolveAnchors(size, samples, scheme)
        val remainingQuota =
            StickerColor.entries
                .associateWith { size.stickersPerFace }
                .toMutableMap()
        val assigned = linkedMapOf<StickerPosition, StickerColor>()

        // On 3x3, lock center stickers first
        if (size == CubeSize.THREE) {
            for (face in Face.entries) {
                val centerPos = StickerPosition(face, 1, 1)
                val color = scheme.colorOf(face)
                assigned[centerPos] = color
                remainingQuota[color] = remainingQuota.getValue(color) - 1
            }
        }

        val unassignedPositions = samples.keys.filter { it !in assigned }

        // Sort stickers by confidence margin (difference between best and second-best color distance)
        val rankedPositions =
            unassignedPositions.sortedByDescending { pos ->
                val sample = samples.getValue(pos)
                val sortedDists = anchors.values.map(sample::distanceTo).sorted()
                sortedDists[1] - sortedDists[0]
            }

        for (pos in rankedPositions) {
            val sample = samples.getValue(pos)
            val chosenColor =
                StickerColor.entries
                    .filter { remainingQuota.getValue(it) > 0 }
                    .minBy { color -> sample.distanceTo(anchors.getValue(color)) }
            assigned[pos] = chosenColor
            remainingQuota[chosenColor] = remainingQuota.getValue(chosenColor) - 1
        }

        return CubeState(size = size, stickers = assigned)
    }

    fun nearestColor(sample: LabColor): StickerColor =
        StickerColor.entries.minBy { color ->
            sample.distanceTo(RgbColor.canonicalFor(color).toLab())
        }

    private fun resolveAnchors(
        size: CubeSize,
        samples: Map<StickerPosition, LabColor>,
        scheme: ColorScheme,
    ): Map<StickerColor, LabColor> =
        if (size == CubeSize.THREE) {
            Face.entries.associate { face ->
                val centerPos = StickerPosition(face, 1, 1)
                scheme.colorOf(face) to samples.getValue(centerPos)
            }
        } else {
            StickerColor.entries.associateWith { color ->
                RgbColor.canonicalFor(color).toLab()
            }
        }
}
