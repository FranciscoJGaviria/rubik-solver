package com.rubiksolver.presentation.cube3d

import kotlin.math.PI

data class CameraOrbit(
    val yawDegrees: Double = DEFAULT_YAW_DEGREES,
    val pitchDegrees: Double = DEFAULT_PITCH_DEGREES,
    val distance: Double = DEFAULT_DISTANCE,
) {
    fun rotateBy(
        deltaYawDegrees: Double,
        deltaPitchDegrees: Double,
    ): CameraOrbit =
        copy(
            yawDegrees = normalizeYaw(yawDegrees + deltaYawDegrees),
            pitchDegrees = (pitchDegrees + deltaPitchDegrees).coerceIn(MIN_PITCH_DEGREES, MAX_PITCH_DEGREES),
        )

    fun reset(): CameraOrbit = DEFAULT

    fun toViewQuaternion(): Quaternion {
        val yawRad = yawDegrees * PI / HALF_TURN_DEGREES
        val pitchRad = pitchDegrees * PI / HALF_TURN_DEGREES
        val qPitch = Quaternion.fromAxisAngle(Vec3.UNIT_X, pitchRad)
        val qYaw = Quaternion.fromAxisAngle(Vec3.UNIT_Y, yawRad)
        return (qPitch * qYaw).normalized()
    }

    private fun normalizeYaw(yaw: Double): Double {
        var result = yaw % FULL_TURN_DEGREES
        if (result > HALF_TURN_DEGREES) result -= FULL_TURN_DEGREES
        if (result < -HALF_TURN_DEGREES) result += FULL_TURN_DEGREES
        return result
    }

    companion object {
        const val DEFAULT_YAW_DEGREES: Double = -35.0
        const val DEFAULT_PITCH_DEGREES: Double = 25.0
        const val DEFAULT_DISTANCE: Double = 6.5
        const val MIN_PITCH_DEGREES: Double = -80.0
        const val MAX_PITCH_DEGREES: Double = 80.0
        private const val HALF_TURN_DEGREES: Double = 180.0
        private const val FULL_TURN_DEGREES: Double = 360.0

        val DEFAULT = CameraOrbit()
    }
}
