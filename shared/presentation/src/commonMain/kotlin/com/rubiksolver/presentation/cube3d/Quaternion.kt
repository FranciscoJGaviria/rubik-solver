package com.rubiksolver.presentation.cube3d

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Quaternion(
    val w: Double,
    val x: Double,
    val y: Double,
    val z: Double,
) {
    operator fun times(other: Quaternion): Quaternion =
        Quaternion(
            w = w * other.w - x * other.x - y * other.y - z * other.z,
            x = w * other.x + x * other.w + y * other.z - z * other.y,
            y = w * other.y - x * other.z + y * other.w + z * other.x,
            z = w * other.z + x * other.y - y * other.x + z * other.w,
        )

    fun conjugate(): Quaternion = Quaternion(w, -x, -y, -z)

    fun normalized(): Quaternion {
        val norm = sqrt(w * w + x * x + y * y + z * z)
        return if (norm < EPSILON) IDENTITY else Quaternion(w / norm, x / norm, y / norm, z / norm)
    }

    fun rotate(v: Vec3): Vec3 {
        val pureV = Quaternion(0.0, v.x, v.y, v.z)
        val rotated = this * pureV * conjugate()
        return Vec3(rotated.x, rotated.y, rotated.z)
    }

    companion object {
        private const val EPSILON: Double = 0.000000001
        val IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)

        fun fromAxisAngle(
            axis: Vec3,
            radians: Double,
        ): Quaternion {
            val unit = axis.normalized()
            val half = radians / 2.0
            val sinHalf = sin(half)
            return Quaternion(
                w = cos(half),
                x = unit.x * sinHalf,
                y = unit.y * sinHalf,
                z = unit.z * sinHalf,
            ).normalized()
        }
    }
}
