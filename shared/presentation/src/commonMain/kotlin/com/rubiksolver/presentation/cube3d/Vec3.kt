package com.rubiksolver.presentation.cube3d

import kotlin.math.sqrt

data class Vec3(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Double): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vec3): Double = x * other.x + y * other.y + z * other.z

    fun cross(other: Vec3): Vec3 =
        Vec3(
            x = y * other.z - z * other.y,
            y = z * other.x - x * other.z,
            z = x * other.y - y * other.x,
        )

    fun length(): Double = sqrt(dot(this))

    fun normalized(): Vec3 {
        val len = length()
        return if (len < EPSILON) ZERO else Vec3(x / len, y / len, z / len)
    }

    companion object {
        private const val EPSILON: Double = 0.000000001
        val ZERO = Vec3(0.0, 0.0, 0.0)
        val UNIT_X = Vec3(1.0, 0.0, 0.0)
        val UNIT_Y = Vec3(0.0, 1.0, 0.0)
        val UNIT_Z = Vec3(0.0, 0.0, 1.0)
    }
}
