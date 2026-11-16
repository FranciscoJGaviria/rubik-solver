package com.rubiksolver.presentation.input

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.recognition.RgbColor
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import kotlin.math.min

actual class PlatformWebcamSampler actual constructor() {
    private var capture: VideoCapture? = null
    private var openCvLoaded: Boolean = false

    @Suppress("TooGenericExceptionCaught")
    actual fun start(): Boolean =
        try {
            if (!openCvLoaded) {
                OpenCV.loadLocally()
                openCvLoaded = true
            }
            val vc = VideoCapture(0)
            if (vc.isOpened) {
                capture = vc
                true
            } else {
                vc.release()
                false
            }
        } catch (_: Throwable) {
            false
        }

    @Suppress("TooGenericExceptionCaught")
    actual fun readFaceGrid(
        face: Face,
        size: CubeSize,
    ): WebcamFrameSample? =
        try {
            val vc = capture
            if (vc == null || !vc.isOpened) {
                null
            } else {
                val frame = Mat()
                val ok = vc.read(frame)
                val sample =
                    if (!ok || frame.empty()) {
                        null
                    } else {
                        extractSampleFromMat(frame, face, size)
                    }
                frame.release()
                sample
            }
        } catch (_: Throwable) {
            null
        }

    @Suppress("TooGenericExceptionCaught")
    actual fun stop() {
        try {
            capture?.release()
        } catch (_: Throwable) {
            // Ignore release errors on shutdown
        } finally {
            capture = null
        }
    }

    private fun extractSampleFromMat(
        frame: Mat,
        face: Face,
        size: CubeSize,
    ): WebcamFrameSample {
        val width = frame.cols()
        val height = frame.rows()
        val boxSpan = (min(width, height) * ROI_BOX_FRACTION).toInt().coerceAtLeast(MIN_BOX_PX)
        val startX = (width - boxSpan) / 2
        val startY = (height - boxSpan) / 2
        val n = size.dimension
        val cellSpan = boxSpan / n

        val gridMap = linkedMapOf<StickerPosition, RgbColor>()
        for (r in 0 until n) {
            for (c in 0 until n) {
                val mirroredCol = n - 1 - c
                val cx = startX + mirroredCol * cellSpan + cellSpan / 2
                val cy = startY + r * cellSpan + cellSpan / 2
                gridMap[StickerPosition(face, r, c)] = samplePatchRgb(frame, cx, cy)
            }
        }

        val previewPixels = buildDownsampledPreview(frame, width, height)
        return WebcamFrameSample(
            gridRgb = gridMap,
            previewWidth = PREVIEW_WIDTH,
            previewHeight = PREVIEW_HEIGHT,
            previewPixelsArgb = previewPixels,
            statusMessage = "Live Mac Webcam (${width}x$height) — Align $face face inside grid",
        )
    }

    private fun samplePatchRgb(
        frame: Mat,
        centerX: Int,
        centerY: Int,
    ): RgbColor {
        var sumR = 0
        var sumG = 0
        var sumB = 0
        var count = 0
        val bgr = ByteArray(CHANNELS_BGR)
        for (dy in -PATCH_RADIUS..PATCH_RADIUS step PATCH_STEP) {
            for (dx in -PATCH_RADIUS..PATCH_RADIUS step PATCH_STEP) {
                val x = (centerX + dx).coerceIn(0, frame.cols() - 1)
                val y = (centerY + dy).coerceIn(0, frame.rows() - 1)
                frame.get(y, x, bgr)
                sumB += bgr[0].toInt() and BYTE_MASK
                sumG += bgr[1].toInt() and BYTE_MASK
                sumR += bgr[2].toInt() and BYTE_MASK
                count++
            }
        }
        return RgbColor(
            r = (sumR / count).coerceIn(0, MAX_CHANNEL),
            g = (sumG / count).coerceIn(0, MAX_CHANNEL),
            b = (sumB / count).coerceIn(0, MAX_CHANNEL),
        )
    }

    private fun buildDownsampledPreview(
        frame: Mat,
        width: Int,
        height: Int,
    ): IntArray {
        val pixels = IntArray(PREVIEW_WIDTH * PREVIEW_HEIGHT)
        val bgr = ByteArray(CHANNELS_BGR)
        for (py in 0 until PREVIEW_HEIGHT) {
            val sy = (py * height / PREVIEW_HEIGHT).coerceIn(0, height - 1)
            for (px in 0 until PREVIEW_WIDTH) {
                val sx = ((PREVIEW_WIDTH - 1 - px) * width / PREVIEW_WIDTH).coerceIn(0, width - 1)
                frame.get(sy, sx, bgr)
                val b = bgr[0].toInt() and BYTE_MASK
                val g = bgr[1].toInt() and BYTE_MASK
                val r = bgr[2].toInt() and BYTE_MASK
                pixels[py * PREVIEW_WIDTH + px] =
                    (ALPHA_OPAQUE shl SHIFT_ALPHA) or (r shl SHIFT_RED) or (g shl SHIFT_GREEN) or b
            }
        }
        return pixels
    }

    private companion object {
        private const val ROI_BOX_FRACTION: Double = 0.56
        private const val MIN_BOX_PX: Int = 60
        private const val PATCH_RADIUS: Int = 8
        private const val PATCH_STEP: Int = 4
        private const val CHANNELS_BGR: Int = 3
        private const val BYTE_MASK: Int = 0xFF
        private const val MAX_CHANNEL: Int = 255
        private const val PREVIEW_WIDTH: Int = 160
        private const val PREVIEW_HEIGHT: Int = 120
        private const val ALPHA_OPAQUE: Int = 0xFF
        private const val SHIFT_ALPHA: Int = 24
        private const val SHIFT_RED: Int = 16
        private const val SHIFT_GREEN: Int = 8
    }
}
