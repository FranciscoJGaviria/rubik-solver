package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.ColorBlindSymbol
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.TurnDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class Point2D(
    val x: Double,
    val y: Double,
)

data class ProjectedQuad2D(
    val position: StickerPosition,
    val color: StickerColor,
    val symbol: ColorBlindSymbol?,
    val vertices: List<Point2D>,
    val center: Point2D,
    val depthZ: Double,
)

data class DirectionArrow2D(
    val face: Face,
    val direction: TurnDirection,
    val points: List<Point2D>,
)

data class ProjectedCubeScene(
    val visibleQuads: List<ProjectedQuad2D>,
    val directionArrow: DirectionArrow2D?,
)

object Cube3DProjectionEngine {
    private const val STICKER_INSET_RATIO: Double = 0.08
    private const val FOV_SCALE_FACTOR: Double = 1.85
    private const val ARROW_RADIUS: Double = 0.62
    private const val ARROW_ALTITUDE: Double = 1.06
    private const val ARROW_ARC_POINTS: Int = 9

    private data class ProjectionContext(
        val state: CubeState,
        val orbit: CameraOrbit,
        val animation: LayerTurnAnimation?,
        val colorBlindSymbols: Boolean,
        val viewQuat: Quaternion,
        val sliceQuat: Quaternion,
        val scale: Double,
        val centerX: Double,
        val centerY: Double,
    )

    fun project(
        state: CubeState,
        orbit: CameraOrbit,
        animation: LayerTurnAnimation?,
        colorBlindSymbols: Boolean,
        viewportWidth: Double,
        viewportHeight: Double,
    ): ProjectedCubeScene {
        val ctx =
            ProjectionContext(
                state = state,
                orbit = orbit,
                animation = animation,
                colorBlindSymbols = colorBlindSymbols,
                viewQuat = orbit.toViewQuaternion(),
                sliceQuat = animation?.currentRotationQuaternion() ?: Quaternion.IDENTITY,
                scale = min(viewportWidth, viewportHeight) * FOV_SCALE_FACTOR,
                centerX = viewportWidth / 2.0,
                centerY = viewportHeight / 2.0,
            )

        val projectedQuads =
            state
                .allStickers()
                .keys
                .mapNotNull { pos -> projectSingleSticker(pos, ctx) }
                .sortedByDescending { it.depthZ }

        val arrow = animation?.let { anim -> projectDirectionArrow(anim, ctx) }

        return ProjectedCubeScene(
            visibleQuads = projectedQuads,
            directionArrow = arrow,
        )
    }

    private fun projectSingleSticker(
        pos: StickerPosition,
        ctx: ProjectionContext,
    ): ProjectedQuad2D? {
        val n = ctx.state.size.dimension
        val baseNormal = LayerTurnAnimation.faceOutwardNormal(pos.face)
        val isTurning = ctx.animation?.affectsSticker(pos, ctx.state.size) == true
        val modelVerts = buildStickerVertices(pos.face, pos.row, pos.col, n)

        val worldVerts = if (isTurning) modelVerts.map(ctx.sliceQuat::rotate) else modelVerts
        val worldNormal = if (isTurning) ctx.sliceQuat.rotate(baseNormal) else baseNormal

        val viewVerts = worldVerts.map(ctx.viewQuat::rotate)
        val viewNormal = ctx.viewQuat.rotate(worldNormal)
        val centroid =
            Vec3(
                x = viewVerts.sumOf { it.x } / viewVerts.size,
                y = viewVerts.sumOf { it.y } / viewVerts.size,
                z = viewVerts.sumOf { it.z } / viewVerts.size,
            )

        val toCamera = Vec3(0.0, 0.0, ctx.orbit.distance) - centroid
        return if (viewNormal.dot(toCamera) > 0.0) {
            val screenPoints =
                viewVerts.map { v ->
                    projectPoint(v, ctx.orbit.distance, ctx.scale, ctx.centerX, ctx.centerY)
                }
            val center2d = projectPoint(centroid, ctx.orbit.distance, ctx.scale, ctx.centerX, ctx.centerY)
            val color = ctx.state.stickerAt(pos)
            val symbol = if (ctx.colorBlindSymbols) ColorBlindSymbol.forColor(color) else null
            ProjectedQuad2D(
                position = pos,
                color = color,
                symbol = symbol,
                vertices = screenPoints,
                center = center2d,
                depthZ = ctx.orbit.distance - centroid.z,
            )
        } else {
            null
        }
    }

    private fun buildStickerVertices(
        face: Face,
        row: Int,
        col: Int,
        dimension: Int,
    ): List<Vec3> {
        val cellSize = 2.0 / dimension
        val inset = cellSize * STICKER_INSET_RATIO
        val u0 = -1.0 + col * cellSize + inset
        val u1 = -1.0 + (col + 1) * cellSize - inset
        val v0 = 1.0 - row * cellSize - inset
        val v1 = 1.0 - (row + 1) * cellSize + inset

        val cornersUV = listOf(u0 to v0, u1 to v0, u1 to v1, u0 to v1)
        return cornersUV.map { (u, v) -> mapFaceUVTo3D(face, u, v) }
    }

    private fun mapFaceUVTo3D(
        face: Face,
        u: Double,
        v: Double,
    ): Vec3 =
        when (face) {
            Face.FRONT -> Vec3(u, v, 1.0)
            Face.BACK -> Vec3(-u, v, -1.0)
            Face.UP -> Vec3(u, 1.0, -v)
            Face.DOWN -> Vec3(u, -1.0, v)
            Face.LEFT -> Vec3(-1.0, v, u)
            Face.RIGHT -> Vec3(1.0, v, -u)
        }

    private fun projectDirectionArrow(
        animation: LayerTurnAnimation,
        ctx: ProjectionContext,
    ): DirectionArrow2D {
        val sign = if (animation.move.direction == TurnDirection.COUNTER_CLOCKWISE) -1.0 else 1.0
        val startAngle = -PI / 3.0
        val sweep = sign * (2.0 * PI / 3.0)

        val points3d =
            (0 until ARROW_ARC_POINTS).map { idx ->
                val t = idx.toDouble() / (ARROW_ARC_POINTS - 1)
                val theta = startAngle + sweep * t
                val u = ARROW_RADIUS * cos(theta)
                val v = ARROW_RADIUS * sin(theta)
                val onFace = mapFaceUVTo3D(animation.move.face, u, v)
                val normal = LayerTurnAnimation.faceOutwardNormal(animation.move.face)
                onFace + normal * (ARROW_ALTITUDE - 1.0)
            }

        val points2d =
            points3d.map { p ->
                val viewP = ctx.viewQuat.rotate(p)
                projectPoint(viewP, ctx.orbit.distance, ctx.scale, ctx.centerX, ctx.centerY)
            }

        return DirectionArrow2D(
            face = animation.move.face,
            direction = animation.move.direction,
            points = points2d,
        )
    }

    private fun projectPoint(
        viewPoint: Vec3,
        cameraDistance: Double,
        scale: Double,
        centerX: Double,
        centerY: Double,
    ): Point2D {
        val zDist = (cameraDistance - viewPoint.z).coerceAtLeast(1.0)
        val sx = centerX + (viewPoint.x / zDist) * scale
        val sy = centerY - (viewPoint.y / zDist) * scale
        return Point2D(sx, sy)
    }
}
