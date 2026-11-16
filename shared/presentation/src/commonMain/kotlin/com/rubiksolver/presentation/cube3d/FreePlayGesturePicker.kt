package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Resolves a touch drag on a sticker into a layer [Move] in free-play mode (FR-4.4, AC-4.4).
 */
object FreePlayGesturePicker {
    private const val MIN_SWIPE_DISTANCE_PX: Double = 16.0

    fun resolveMove(
        size: CubeSize,
        touchedPosition: StickerPosition,
        dragX: Double,
        dragY: Double,
    ): Move? {
        if (hypot(dragX, dragY) < MIN_SWIPE_DISTANCE_PX) {
            return null
        }
        val last = size.dimension - 1
        val isHorizontal = abs(dragX) >= abs(dragY)
        return if (isHorizontal) {
            resolveHorizontalSwipe(touchedPosition, last, swipeRight = dragX > 0.0)
        } else {
            resolveVerticalSwipe(touchedPosition, last, swipeDown = dragY > 0.0)
        }
    }

    private fun resolveHorizontalSwipe(
        pos: StickerPosition,
        last: Int,
        swipeRight: Boolean,
    ): Move? =
        when (pos.face) {
            Face.FRONT, Face.RIGHT, Face.BACK, Face.LEFT ->
                horizontalSideFaceMove(pos.row, last, swipeRight)
            Face.UP ->
                horizontalPolarFaceMove(
                    row = pos.row,
                    last = last,
                    topFace = Face.BACK,
                    bottomFace = Face.FRONT,
                    topRightCw = false,
                    swipeRight = swipeRight,
                )
            Face.DOWN ->
                horizontalPolarFaceMove(
                    row = pos.row,
                    last = last,
                    topFace = Face.FRONT,
                    bottomFace = Face.BACK,
                    topRightCw = true,
                    swipeRight = swipeRight,
                )
        }

    private fun horizontalSideFaceMove(
        row: Int,
        last: Int,
        swipeRight: Boolean,
    ): Move? =
        when (row) {
            0 -> Move(Face.UP, directionFor(clockwiseWhenPositive = false, positive = swipeRight))
            last -> Move(Face.DOWN, directionFor(clockwiseWhenPositive = true, positive = swipeRight))
            else -> null
        }

    private fun horizontalPolarFaceMove(
        row: Int,
        last: Int,
        topFace: Face,
        bottomFace: Face,
        topRightCw: Boolean,
        swipeRight: Boolean,
    ): Move? =
        when (row) {
            0 -> Move(topFace, directionFor(clockwiseWhenPositive = topRightCw, positive = swipeRight))
            last -> Move(bottomFace, directionFor(clockwiseWhenPositive = !topRightCw, positive = swipeRight))
            else -> null
        }

    private fun resolveVerticalSwipe(
        pos: StickerPosition,
        last: Int,
        swipeDown: Boolean,
    ): Move? =
        when (pos.face) {
            Face.FRONT, Face.UP, Face.DOWN ->
                verticalSliceMove(
                    col = pos.col,
                    last = last,
                    leftColFace = Face.LEFT,
                    rightColFace = Face.RIGHT,
                    swipeDown = swipeDown,
                )
            Face.BACK ->
                verticalSliceMove(
                    col = pos.col,
                    last = last,
                    leftColFace = Face.RIGHT,
                    rightColFace = Face.LEFT,
                    swipeDown = swipeDown,
                )
            Face.LEFT ->
                verticalSliceMove(
                    col = pos.col,
                    last = last,
                    leftColFace = Face.BACK,
                    rightColFace = Face.FRONT,
                    swipeDown = swipeDown,
                )
            Face.RIGHT ->
                verticalSliceMove(
                    col = pos.col,
                    last = last,
                    leftColFace = Face.FRONT,
                    rightColFace = Face.BACK,
                    swipeDown = swipeDown,
                )
        }

    private fun verticalSliceMove(
        col: Int,
        last: Int,
        leftColFace: Face,
        rightColFace: Face,
        swipeDown: Boolean,
    ): Move? =
        when (col) {
            0 -> Move(leftColFace, directionFor(clockwiseWhenPositive = true, positive = swipeDown))
            last -> Move(rightColFace, directionFor(clockwiseWhenPositive = false, positive = swipeDown))
            else -> null
        }

    private fun directionFor(
        clockwiseWhenPositive: Boolean,
        positive: Boolean,
    ): TurnDirection =
        if (positive == clockwiseWhenPositive) {
            TurnDirection.CLOCKWISE
        } else {
            TurnDirection.COUNTER_CLOCKWISE
        }
}
