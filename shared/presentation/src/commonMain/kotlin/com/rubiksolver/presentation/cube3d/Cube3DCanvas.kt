package com.rubiksolver.presentation.cube3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.rubiksolver.domain.cube.ColorBlindSymbol
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import kotlin.math.hypot

@Composable
fun Cube3DCanvas(
    viewerState: CubeViewerState,
    onOrbitDrag: (Double, Double) -> Unit,
    onFreePlaySwipe: (StickerPosition, Double, Double) -> Unit,
    onStickerTap: ((StickerPosition) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var latestScene by remember { mutableStateOf<ProjectedCubeScene?>(null) }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .cube3dTapAndDragGestures(
                    viewerState = viewerState,
                    latestSceneProvider = { latestScene },
                    onOrbitDrag = onOrbitDrag,
                    onFreePlaySwipe = onFreePlaySwipe,
                    onStickerTap = onStickerTap,
                ),
    ) {
        val scene =
            Cube3DProjectionEngine.project(
                state = viewerState.cubeState,
                orbit = viewerState.cameraOrbit,
                animation = viewerState.activeAnimation,
                colorBlindSymbols = viewerState.colorBlindAssistance,
                viewportWidth = size.width.toDouble(),
                viewportHeight = size.height.toDouble(),
            )
        latestScene = scene

        scene.visibleQuads.forEach { quad ->
            drawStickerQuad(
                quad = quad,
                isUnpainted = quad.position in viewerState.unpaintedPositions,
                isHighlighted = quad.position in viewerState.highlightedPositions,
            )
        }
        scene.directionArrow?.let { arrow ->
            drawDirectionArrow(arrow)
        }
    }
}

private fun Modifier.cube3dTapAndDragGestures(
    viewerState: CubeViewerState,
    latestSceneProvider: () -> ProjectedCubeScene?,
    onOrbitDrag: (Double, Double) -> Unit,
    onFreePlaySwipe: (StickerPosition, Double, Double) -> Unit,
    onStickerTap: ((StickerPosition) -> Unit)?,
): Modifier =
    this
        .pointerInput(onStickerTap) {
            if (onStickerTap != null) {
                detectTapGestures { tapOffset ->
                    latestSceneProvider()?.let { scene ->
                        findHitQuad(scene, tapOffset.x.toDouble(), tapOffset.y.toDouble())?.let(onStickerTap)
                    }
                }
            }
        }.pointerInput(viewerState.freePlayMode, viewerState.cubeState) {
            var touchedSticker: StickerPosition? = null
            var accumulatedDragX = 0.0
            var accumulatedDragY = 0.0
            detectDragGestures(
                onDragStart = { startOffset ->
                    accumulatedDragX = 0.0
                    accumulatedDragY = 0.0
                    touchedSticker =
                        latestSceneProvider()?.let { scene ->
                            findHitQuad(scene, startOffset.x.toDouble(), startOffset.y.toDouble())
                        }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (viewerState.freePlayMode && touchedSticker != null) {
                        accumulatedDragX += dragAmount.x.toDouble()
                        accumulatedDragY += dragAmount.y.toDouble()
                    } else {
                        onOrbitDrag(
                            dragAmount.x.toDouble() * ORBIT_SENSITIVITY,
                            dragAmount.y.toDouble() * ORBIT_SENSITIVITY,
                        )
                    }
                },
                onDragEnd = {
                    val startPos = touchedSticker
                    if (viewerState.freePlayMode && startPos != null) {
                        onFreePlaySwipe(startPos, accumulatedDragX, accumulatedDragY)
                    }
                    touchedSticker = null
                },
            )
        }

private fun findHitQuad(
    scene: ProjectedCubeScene,
    touchX: Double,
    touchY: Double,
): StickerPosition? {
    val frontToBack = scene.visibleQuads.asReversed()
    val exactHit = frontToBack.firstOrNull { quad -> isPointInsideConvexQuad(quad.vertices, touchX, touchY) }
    if (exactHit != null) return exactHit.position
    return frontToBack
        .filter { quad -> hypot(quad.center.x - touchX, quad.center.y - touchY) <= MAX_TAP_FALLBACK_DISTANCE }
        .minByOrNull { quad -> hypot(quad.center.x - touchX, quad.center.y - touchY) }
        ?.position
}

private fun isPointInsideConvexQuad(
    vertices: List<Point2D>,
    px: Double,
    py: Double,
): Boolean {
    if (vertices.size < 3) return false
    var positiveCount = 0
    var negativeCount = 0
    for (i in vertices.indices) {
        val a = vertices[i]
        val b = vertices[(i + 1) % vertices.size]
        val cross = (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x)
        if (cross > 0.0) positiveCount++
        if (cross < 0.0) negativeCount++
    }
    return positiveCount == 0 || negativeCount == 0
}

private fun DrawScope.drawStickerQuad(
    quad: ProjectedQuad2D,
    isUnpainted: Boolean,
    isHighlighted: Boolean,
) {
    if (quad.vertices.isEmpty()) return
    val path =
        Path().apply {
            val first = quad.vertices.first()
            moveTo(first.x.toFloat(), first.y.toFloat())
            quad.vertices.drop(1).forEach { pt ->
                lineTo(pt.x.toFloat(), pt.y.toFloat())
            }
            close()
        }
    val fillColor = if (isUnpainted) Color(UNPAINTED_HEX) else paletteColor(quad.color)
    drawPath(path = path, color = fillColor, style = Fill)
    val borderColor = if (isHighlighted) Color(HIGHLIGHT_BORDER_HEX) else Color(BORDER_HEX)
    val borderWidth = if (isHighlighted) HIGHLIGHT_BORDER_WIDTH else STICKER_BORDER_WIDTH
    drawPath(path = path, color = borderColor, style = Stroke(width = borderWidth))

    if (isUnpainted) {
        drawCircle(
            color = Color(UNPAINTED_DOT_HEX),
            radius = UNPAINTED_DOT_RADIUS,
            center = Offset(quad.center.x.toFloat(), quad.center.y.toFloat()),
        )
    } else {
        quad.symbol?.let { symbol ->
            drawColorBlindSymbol(symbol, quad.center)
        }
    }
}

private fun DrawScope.drawColorBlindSymbol(
    symbol: ColorBlindSymbol,
    center: Point2D,
) {
    val cx = center.x.toFloat()
    val cy = center.y.toFloat()
    val r = SYMBOL_RADIUS_PX
    val ink = Color(SYMBOL_INK_HEX)

    when (symbol) {
        ColorBlindSymbol.CIRCLE ->
            drawCircle(
                color = ink,
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = SYMBOL_STROKE_WIDTH),
            )
        ColorBlindSymbol.TRIANGLE ->
            drawPolygonSymbol(
                ink = ink,
                points = listOf(Offset(cx, cy - r), Offset(cx + r, cy + r), Offset(cx - r, cy + r)),
            )
        ColorBlindSymbol.SQUARE ->
            drawPolygonSymbol(
                ink = ink,
                points =
                    listOf(
                        Offset(cx - r, cy - r),
                        Offset(cx + r, cy - r),
                        Offset(cx + r, cy + r),
                        Offset(cx - r, cy + r),
                    ),
            )
        ColorBlindSymbol.DIAMOND ->
            drawPolygonSymbol(
                ink = ink,
                points = listOf(Offset(cx, cy - r), Offset(cx + r, cy), Offset(cx, cy + r), Offset(cx - r, cy)),
            )
        ColorBlindSymbol.PLUS -> drawCrossSymbol(ink, cx, cy, r, includeDiagonals = false)
        ColorBlindSymbol.STAR -> drawCrossSymbol(ink, cx, cy, r, includeDiagonals = true)
    }
}

private fun DrawScope.drawPolygonSymbol(
    ink: Color,
    points: List<Offset>,
) {
    val poly =
        Path().apply {
            val first = points.first()
            moveTo(first.x, first.y)
            points.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
            close()
        }
    drawPath(path = poly, color = ink, style = Stroke(width = SYMBOL_STROKE_WIDTH))
}

private fun DrawScope.drawCrossSymbol(
    ink: Color,
    cx: Float,
    cy: Float,
    r: Float,
    includeDiagonals: Boolean,
) {
    drawLine(
        color = ink,
        start = Offset(cx - r, cy),
        end = Offset(cx + r, cy),
        strokeWidth = SYMBOL_STROKE_WIDTH,
    )
    drawLine(
        color = ink,
        start = Offset(cx, cy - r),
        end = Offset(cx, cy + r),
        strokeWidth = SYMBOL_STROKE_WIDTH,
    )
    if (includeDiagonals) {
        val d = r * DIAGONAL_FACTOR
        drawLine(
            color = ink,
            start = Offset(cx - d, cy - d),
            end = Offset(cx + d, cy + d),
            strokeWidth = SYMBOL_STROKE_WIDTH,
        )
        drawLine(
            color = ink,
            start = Offset(cx - d, cy + d),
            end = Offset(cx + d, cy - d),
            strokeWidth = SYMBOL_STROKE_WIDTH,
        )
    }
}

private fun DrawScope.drawDirectionArrow(arrow: DirectionArrow2D) {
    if (arrow.points.size < 2) return
    val path =
        Path().apply {
            val first = arrow.points.first()
            moveTo(first.x.toFloat(), first.y.toFloat())
            arrow.points.drop(1).forEach { pt ->
                lineTo(pt.x.toFloat(), pt.y.toFloat())
            }
        }
    drawPath(path = path, color = Color(ARROW_HEX), style = Stroke(width = ARROW_STROKE_WIDTH))
    val tip = arrow.points.last()
    drawCircle(
        color = Color(ARROW_HEX),
        radius = ARROW_TIP_RADIUS,
        center = Offset(tip.x.toFloat(), tip.y.toFloat()),
    )
}

private fun paletteColor(color: StickerColor): Color =
    when (color) {
        StickerColor.WHITE -> Color(WHITE_HEX)
        StickerColor.YELLOW -> Color(YELLOW_HEX)
        StickerColor.GREEN -> Color(GREEN_HEX)
        StickerColor.BLUE -> Color(BLUE_HEX)
        StickerColor.RED -> Color(RED_HEX)
        StickerColor.ORANGE -> Color(ORANGE_HEX)
    }

private const val ORBIT_SENSITIVITY: Double = 0.45
private const val MAX_TAP_FALLBACK_DISTANCE: Double = 42.0
private const val STICKER_BORDER_WIDTH: Float = 3.5f
private const val HIGHLIGHT_BORDER_WIDTH: Float = 6.5f
private const val UNPAINTED_DOT_RADIUS: Float = 4.0f
private const val SYMBOL_RADIUS_PX: Float = 10f
private const val SYMBOL_STROKE_WIDTH: Float = 2.6f
private const val DIAGONAL_FACTOR: Float = 0.7f
private const val ARROW_STROKE_WIDTH: Float = 5f
private const val ARROW_TIP_RADIUS: Float = 7f
private const val BORDER_HEX: Long = 0xFF14181F
private const val UNPAINTED_HEX: Long = 0xFF334155
private const val UNPAINTED_DOT_HEX: Long = 0xFF64748B
private const val HIGHLIGHT_BORDER_HEX: Long = 0xFFFB7185
private const val SYMBOL_INK_HEX: Long = 0xFF111827
private const val ARROW_HEX: Long = 0xFF0EA5E9
private const val WHITE_HEX: Long = 0xFFF8FAFC
private const val YELLOW_HEX: Long = 0xFFFACC15
private const val GREEN_HEX: Long = 0xFF22C55E
private const val BLUE_HEX: Long = 0xFF2563EB
private const val RED_HEX: Long = 0xFFEF4444
private const val ORANGE_HEX: Long = 0xFFF97316
