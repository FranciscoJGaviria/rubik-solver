package com.rubiksolver.application.recognition

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.recognition.ConstrainedColorClassifier
import com.rubiksolver.domain.recognition.LabColor
import com.rubiksolver.domain.recognition.NextFaceGuidance
import com.rubiksolver.domain.recognition.RgbColor
import com.rubiksolver.domain.recognition.ScanOrientationGuide

data class GuidedCubeScanSession(
    val size: CubeSize,
    val capturedFaces: Set<Face> = emptySet(),
    val collectedSamples: Map<StickerPosition, LabColor> = emptyMap(),
    val nextFaceGuidance: NextFaceGuidance? =
        NextFaceGuidance(
            nextFace = ScanOrientationGuide.CAPTURE_ORDER.first(),
            upReferenceFace = Face.UP,
            rotationInstruction = "Hold the Front face toward the camera with Up on top.",
        ),
    val proposedCubeState: CubeState? = null,
)

class GuidedScanUseCase {
    fun startSession(size: CubeSize): GuidedCubeScanSession = GuidedCubeScanSession(size = size)

    fun captureFace(
        session: GuidedCubeScanSession,
        face: Face,
        faceRgbSamples: Map<StickerPosition, RgbColor>,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): GuidedCubeScanSession {
        val converted = faceRgbSamples.mapValues { (_, rgb) -> rgb.toLab() }
        val mergedSamples = session.collectedSamples + converted
        val updatedFaces = session.capturedFaces + face
        val nextGuidance = ScanOrientationGuide.nextStepAfter(face)
        val allCaptured = updatedFaces.containsAll(ScanOrientationGuide.CAPTURE_ORDER)
        val proposed =
            if (allCaptured && mergedSamples.size == session.size.totalStickers) {
                ConstrainedColorClassifier.classify(session.size, mergedSamples, scheme)
            } else {
                null
            }
        return session.copy(
            capturedFaces = updatedFaces,
            collectedSamples = mergedSamples,
            nextFaceGuidance = nextGuidance,
            proposedCubeState = proposed,
        )
    }
}
