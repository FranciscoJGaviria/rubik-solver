package com.rubiksolver.presentation.input

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.recognition.RgbColor

class WebcamFrameSample(
    val gridRgb: Map<StickerPosition, RgbColor>,
    val previewWidth: Int,
    val previewHeight: Int,
    val previewPixelsArgb: IntArray?,
    val statusMessage: String,
)

expect class PlatformWebcamSampler() {
    fun start(): Boolean

    fun readFaceGrid(
        face: Face,
        size: CubeSize,
    ): WebcamFrameSample?

    fun stop()
}
