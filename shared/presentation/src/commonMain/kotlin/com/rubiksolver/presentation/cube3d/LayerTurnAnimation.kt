package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import kotlin.math.PI

data class LayerTurnAnimation(
    val move: Move,
    val progress: Double = 0.0,
) {
    init {
        require(progress in 0.0..1.0) {
            "Animation progress must be in 0.0..1.0, got $progress"
        }
    }

    fun withProgress(newProgress: Double): LayerTurnAnimation = copy(progress = newProgress.coerceIn(0.0, 1.0))

    fun easedProgress(): Double {
        val t = progress
        return if (t < HALF_PROGRESS) {
            EASE_FACTOR * t * t * t
        } else {
            val f = -EASE_SLOPE * t + EASE_SLOPE
            1.0 - (f * f * f) / EASE_SLOPE
        }
    }

    fun currentRotationQuaternion(): Quaternion {
        val normal = faceOutwardNormal(move.face)
        val targetAngle =
            when (move.direction) {
                TurnDirection.CLOCKWISE -> -PI / EASE_SLOPE
                TurnDirection.COUNTER_CLOCKWISE -> PI / EASE_SLOPE
                TurnDirection.DOUBLE -> -PI
            }
        return Quaternion.fromAxisAngle(normal, targetAngle * easedProgress())
    }

    fun affectsSticker(
        position: StickerPosition,
        size: CubeSize,
    ): Boolean {
        val last = size.dimension - 1
        return if (position.face == move.face) {
            true
        } else if (position.face == move.face.opposite) {
            false
        } else {
            isAdjacentStickerInSlice(move.face, position, last)
        }
    }

    private fun isAdjacentStickerInSlice(
        turningFace: Face,
        pos: StickerPosition,
        last: Int,
    ): Boolean =
        when (turningFace) {
            Face.UP -> pos.row == 0
            Face.DOWN -> pos.row == last
            Face.FRONT -> isInFrontSlice(pos, last)
            Face.BACK -> isInBackSlice(pos, last)
            Face.LEFT -> isInLeftSlice(pos, last)
            Face.RIGHT -> isInRightSlice(pos, last)
        }

    private fun isInFrontSlice(
        pos: StickerPosition,
        last: Int,
    ): Boolean =
        when (pos.face) {
            Face.UP -> pos.row == last
            Face.DOWN -> pos.row == 0
            Face.LEFT -> pos.col == last
            Face.RIGHT -> pos.col == 0
            else -> false
        }

    private fun isInBackSlice(
        pos: StickerPosition,
        last: Int,
    ): Boolean =
        when (pos.face) {
            Face.UP -> pos.row == 0
            Face.DOWN -> pos.row == last
            Face.LEFT -> pos.col == 0
            Face.RIGHT -> pos.col == last
            else -> false
        }

    private fun isInLeftSlice(
        pos: StickerPosition,
        last: Int,
    ): Boolean =
        when (pos.face) {
            Face.UP, Face.FRONT, Face.DOWN -> pos.col == 0
            Face.BACK -> pos.col == last
            else -> false
        }

    private fun isInRightSlice(
        pos: StickerPosition,
        last: Int,
    ): Boolean =
        when (pos.face) {
            Face.UP, Face.FRONT, Face.DOWN -> pos.col == last
            Face.BACK -> pos.col == 0
            else -> false
        }

    companion object {
        private const val HALF_PROGRESS: Double = 0.5
        private const val EASE_SLOPE: Double = 2.0
        private const val EASE_FACTOR: Double = 4.0

        fun faceOutwardNormal(face: Face): Vec3 =
            when (face) {
                Face.UP -> Vec3(0.0, 1.0, 0.0)
                Face.DOWN -> Vec3(0.0, -1.0, 0.0)
                Face.FRONT -> Vec3(0.0, 0.0, 1.0)
                Face.BACK -> Vec3(0.0, 0.0, -1.0)
                Face.LEFT -> Vec3(-1.0, 0.0, 0.0)
                Face.RIGHT -> Vec3(1.0, 0.0, 0.0)
            }
    }
}
