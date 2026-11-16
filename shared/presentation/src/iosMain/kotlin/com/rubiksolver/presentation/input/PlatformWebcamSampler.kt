package com.rubiksolver.presentation.input

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face

actual class PlatformWebcamSampler actual constructor() {
    actual fun start(): Boolean = false

    actual fun readFaceGrid(
        face: Face,
        size: CubeSize,
    ): WebcamFrameSample? = null

    actual fun stop() = Unit
}
