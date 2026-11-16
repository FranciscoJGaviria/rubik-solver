package com.rubiksolver.domain.recognition

import com.rubiksolver.domain.cube.Face

data class NextFaceGuidance(
    val nextFace: Face,
    val upReferenceFace: Face,
    val rotationInstruction: String,
)

object ScanOrientationGuide {
    val CAPTURE_ORDER: List<Face> =
        listOf(
            Face.FRONT,
            Face.RIGHT,
            Face.BACK,
            Face.LEFT,
            Face.UP,
            Face.DOWN,
        )

    fun nextStepAfter(capturedFace: Face): NextFaceGuidance? =
        when (capturedFace) {
            Face.FRONT ->
                NextFaceGuidance(
                    nextFace = Face.RIGHT,
                    upReferenceFace = Face.UP,
                    rotationInstruction = "Rotate the cube left 90 degrees to show the Right face (keep Up on top).",
                )
            Face.RIGHT ->
                NextFaceGuidance(
                    nextFace = Face.BACK,
                    upReferenceFace = Face.UP,
                    rotationInstruction = "Rotate the cube left 90 degrees again to show the Back face.",
                )
            Face.BACK ->
                NextFaceGuidance(
                    nextFace = Face.LEFT,
                    upReferenceFace = Face.UP,
                    rotationInstruction = "Rotate the cube left 90 degrees to show the Left face.",
                )
            Face.LEFT ->
                NextFaceGuidance(
                    nextFace = Face.UP,
                    upReferenceFace = Face.BACK,
                    rotationInstruction = "Return to Front, then tilt the cube down to show the Up face.",
                )
            Face.UP ->
                NextFaceGuidance(
                    nextFace = Face.DOWN,
                    upReferenceFace = Face.FRONT,
                    rotationInstruction = "Tilt the cube up 180 degrees to show the Down face.",
                )
            Face.DOWN -> null
        }
}
