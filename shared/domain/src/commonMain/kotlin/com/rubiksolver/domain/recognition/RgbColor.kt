package com.rubiksolver.domain.recognition

import com.rubiksolver.domain.cube.StickerColor
import kotlin.math.pow
import kotlin.math.sqrt

data class LabColor(
    val l: Double,
    val a: Double,
    val b: Double,
) {
    /**
     * Weighted CIELAB perceptual distance giving lower weight to luminance [l]
     * for shadow and lighting resilience (ADR-05).
     */
    fun distanceTo(other: LabColor): Double {
        val dl = (l - other.l) * LUMINANCE_WEIGHT
        val da = a - other.a
        val db = b - other.b
        return sqrt(dl * dl + da * da + db * db)
    }

    companion object {
        private const val LUMINANCE_WEIGHT: Double = 0.45
    }
}

data class RgbColor(
    val r: Int,
    val g: Int,
    val b: Int,
) {
    init {
        require(r in 0..MAX_CHANNEL && g in 0..MAX_CHANNEL && b in 0..MAX_CHANNEL) {
            "RGB channels must be in 0..255"
        }
    }

    fun toLab(): LabColor {
        val rl = pivotRgb(r / MAX_CHANNEL_DOUBLE)
        val gl = pivotRgb(g / MAX_CHANNEL_DOUBLE)
        val bl = pivotRgb(b / MAX_CHANNEL_DOUBLE)

        val x = (rl * X_R + gl * X_G + bl * X_B) / REF_X
        val y = (rl * Y_R + gl * Y_G + bl * Y_B) / REF_Y
        val z = (rl * Z_R + gl * Z_G + bl * Z_B) / REF_Z

        val fx = pivotXyz(x)
        val fy = pivotXyz(y)
        val fz = pivotXyz(z)

        return LabColor(
            l = L_SCALE * fy - L_OFFSET,
            a = A_SCALE * (fx - fy),
            b = B_SCALE * (fy - fz),
        )
    }

    private fun pivotRgb(channel: Double): Double =
        if (channel > SRGB_THRESHOLD) {
            ((channel + SRGB_OFFSET) / SRGB_DIVISOR).pow(SRGB_GAMMA)
        } else {
            channel / SRGB_LINEAR_SLOPE
        }

    private fun pivotXyz(value: Double): Double =
        if (value > XYZ_EPSILON) {
            value.pow(ONE_THIRD)
        } else {
            (XYZ_KAPPA * value + L_OFFSET) / L_SCALE
        }

    companion object {
        private const val MAX_CHANNEL: Int = 255
        private const val MAX_CHANNEL_DOUBLE: Double = 255.0
        private const val SRGB_THRESHOLD: Double = 0.04045
        private const val SRGB_OFFSET: Double = 0.055
        private const val SRGB_DIVISOR: Double = 1.055
        private const val SRGB_GAMMA: Double = 2.4
        private const val SRGB_LINEAR_SLOPE: Double = 12.92
        private const val X_R: Double = 0.4124
        private const val X_G: Double = 0.3576
        private const val X_B: Double = 0.1805
        private const val Y_R: Double = 0.2126
        private const val Y_G: Double = 0.7152
        private const val Y_B: Double = 0.0722
        private const val Z_R: Double = 0.0193
        private const val Z_G: Double = 0.1192
        private const val Z_B: Double = 0.9505
        private const val REF_X: Double = 0.95047
        private const val REF_Y: Double = 1.00000
        private const val REF_Z: Double = 1.08883
        private const val XYZ_EPSILON: Double = 0.008856
        private const val XYZ_KAPPA: Double = 903.3
        private const val ONE_THIRD: Double = 0.333333333
        private const val L_SCALE: Double = 116.0
        private const val L_OFFSET: Double = 16.0
        private const val A_SCALE: Double = 500.0
        private const val B_SCALE: Double = 200.0

        private const val WHITE_R = 248
        private const val WHITE_G = 250
        private const val WHITE_B = 252
        private const val YELLOW_R = 250
        private const val YELLOW_G = 204
        private const val YELLOW_B = 21
        private const val GREEN_R = 34
        private const val GREEN_G = 197
        private const val GREEN_B = 94
        private const val BLUE_R = 37
        private const val BLUE_G = 99
        private const val BLUE_B = 235
        private const val RED_R = 239
        private const val RED_G = 68
        private const val RED_B = 68
        private const val ORANGE_R = 249
        private const val ORANGE_G = 115
        private const val ORANGE_B = 22

        fun canonicalFor(color: StickerColor): RgbColor =
            when (color) {
                StickerColor.WHITE -> RgbColor(WHITE_R, WHITE_G, WHITE_B)
                StickerColor.YELLOW -> RgbColor(YELLOW_R, YELLOW_G, YELLOW_B)
                StickerColor.GREEN -> RgbColor(GREEN_R, GREEN_G, GREEN_B)
                StickerColor.BLUE -> RgbColor(BLUE_R, BLUE_G, BLUE_B)
                StickerColor.RED -> RgbColor(RED_R, RED_G, RED_B)
                StickerColor.ORANGE -> RgbColor(ORANGE_R, ORANGE_G, ORANGE_B)
            }
    }
}
