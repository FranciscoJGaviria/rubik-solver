package com.rubiksolver.presentation.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.recognition.ConstrainedColorClassifier
import com.rubiksolver.domain.recognition.ScanOrientationGuide
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.presentation.cube3d.Cube3DCanvas
import com.rubiksolver.presentation.cube3d.CubeViewerState
import kotlinx.coroutines.delay

@Composable
fun ManualCubeInputScreen(
    controller: ManualInputController = remember { ManualInputController() },
    onOpenSolveGuide: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()
    val webcamSampler = remember { PlatformWebcamSampler() }
    var latestWebcamFrame by remember { mutableStateOf<WebcamFrameSample?>(null) }

    DisposableEffect(Unit) {
        onDispose { webcamSampler.stop() }
    }

    LaunchedEffect(state.webcamActive, state.inputMode, state.activeScanFace, state.cubeState.size) {
        while (state.webcamActive && state.inputMode == InputMode.CAMERA_SCAN) {
            val frame = webcamSampler.readFaceGrid(state.activeScanFace, state.cubeState.size)
            if (frame != null) {
                latestWebcamFrame = frame
                controller.updateWebcamState(
                    active = true,
                    samples = frame.gridRgb,
                    statusMessage = frame.statusMessage,
                )
            }
            delay(WEBCAM_POLL_INTERVAL_MS)
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopSizeAndModeSelector(state = state, controller = controller)
            Interactive3DInputCanvasCard(state = state, controller = controller)

            when (state.inputMode) {
                InputMode.PAINT_3D -> Paint3DControlsSection(state = state, controller = controller)
                InputMode.CAMERA_SCAN ->
                    CameraScanSection(
                        state = state,
                        controller = controller,
                        webcamSampler = webcamSampler,
                        latestWebcamFrame = latestWebcamFrame,
                        onClearWebcamFrame = { latestWebcamFrame = null },
                    )
                InputMode.SCRAMBLE ->
                    ScrambleInputRow(
                        scrambleText = state.scrambleText,
                        onTextChanged = controller::updateScrambleText,
                        onApplyScramble = { controller.applyScramble() },
                    )
            }

            MethodAndSolveSection(
                state = state,
                controller = controller,
                onOpenSolveGuide = onOpenSolveGuide,
            )
        }
    }
}

@Composable
private fun TopSizeAndModeSelector(
    state: ManualInputUiState,
    controller: ManualInputController,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CubeSize.entries.forEach { size ->
                FilterChip(
                    selected = state.cubeState.size == size,
                    onClick = { controller.selectSize(size) },
                    label = { Text("${size.dimension}x${size.dimension}") },
                )
            }
        }
        Text(
            text = "Colored: ${state.paintedCount} / ${state.totalStickers}",
            style = MaterialTheme.typography.titleSmall,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.inputMode == InputMode.PAINT_3D,
            onClick = { controller.selectInputMode(InputMode.PAINT_3D) },
            label = { Text("1. Paint 3D Cube") },
        )
        FilterChip(
            selected = state.inputMode == InputMode.CAMERA_SCAN,
            onClick = { controller.selectInputMode(InputMode.CAMERA_SCAN) },
            label = { Text("2. Camera Scan (6 Faces)") },
        )
        FilterChip(
            selected = state.inputMode == InputMode.SCRAMBLE,
            onClick = { controller.selectInputMode(InputMode.SCRAMBLE) },
            label = { Text("3. Scramble") },
        )
    }
}

@Composable
private fun Interactive3DInputCanvasCard(
    state: ManualInputUiState,
    controller: ManualInputController,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Tap any sticker on the 3D cube to paint it • Drag anywhere to orbit 360°",
            style = MaterialTheme.typography.bodySmall,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .background(Color(CANVAS_BG_HEX))
                    .border(1.dp, Color.DarkGray),
            contentAlignment = Alignment.Center,
        ) {
            Cube3DCanvas(
                viewerState =
                    CubeViewerState(
                        cubeState = state.cubeState,
                        cameraOrbit = state.cameraOrbit,
                        unpaintedPositions = state.unpaintedPositions,
                        highlightedPositions =
                            if (state.isFullyPainted) {
                                state.validationReport.highlightedStickers
                            } else {
                                emptySet()
                            },
                    ),
                onOrbitDrag = controller::orbit,
                onFreePlaySwipe = { _, _, _ -> },
                onStickerTap = controller::paintSticker,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Face.entries.forEach { face ->
                FilterChip(
                    selected = state.selectedFace == face,
                    onClick = { controller.selectFace(face) },
                    label = { Text("View ${face.symbol}") },
                )
            }
        }
    }
}

@Composable
private fun Paint3DControlsSection(
    state: ManualInputUiState,
    controller: ManualInputController,
) {
    val targetPerColor = state.cubeState.size.stickersPerFace
    val paintedColorCounts =
        StickerColor.entries.associateWith { color ->
            state.cubeState
                .allStickers()
                .count { (pos, stickerColor) -> pos !in state.unpaintedPositions && stickerColor == color }
        }

    Text(
        text = "Select Brush Color (target: $targetPerColor per color):",
        style = MaterialTheme.typography.labelLarge,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StickerColor.entries.forEach { color ->
            val count = paintedColorCounts[color] ?: 0
            val isSelected = state.selectedBrushColor == color
            FilterChip(
                selected = isSelected,
                onClick = { controller.selectBrushColor(color) },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .background(stickerComposeColor(color))
                                    .border(1.dp, Color.Black),
                        )
                        Text("${color.name.take(LABEL_CHAR_COUNT)} ($count/$targetPerColor)")
                    }
                },
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { controller.applyPreset(CubePresetAction.CLEAR_UNPAINTED) }) {
            Text("Clear (Uncolored)")
        }
        if (state.cubeState.size == CubeSize.THREE) {
            OutlinedButton(onClick = { controller.applyPreset(CubePresetAction.ANCHOR_CENTERS) }) {
                Text("Anchor 6 Centers")
            }
        }
        OutlinedButton(onClick = { controller.applyPreset(CubePresetAction.FILL_SOLVED) }) {
            Text("Fill Solved")
        }
    }
}

@Composable
private fun CameraScanSection(
    state: ManualInputUiState,
    controller: ManualInputController,
    webcamSampler: PlatformWebcamSampler,
    latestWebcamFrame: WebcamFrameSample?,
    onClearWebcamFrame: () -> Unit,
) {
    val capturedCount = state.scanSession.capturedFaces.size
    val totalFaces = ScanOrientationGuide.CAPTURE_ORDER.size

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Guided Camera Scanner — Captured $capturedCount / $totalFaces faces",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(text = state.webcamStatusMessage, style = MaterialTheme.typography.bodySmall)
        CameraSourceButtonsRow(
            state = state,
            controller = controller,
            webcamSampler = webcamSampler,
            onClearWebcamFrame = onClearWebcamFrame,
        )
        CameraViewfinderAndGrid(
            state = state,
            latestWebcamFrame = latestWebcamFrame,
            onCaptureFace = controller::captureCurrentScanFace,
        )
    }
}

@Composable
private fun CameraSourceButtonsRow(
    state: ManualInputUiState,
    controller: ManualInputController,
    webcamSampler: PlatformWebcamSampler,
    onClearWebcamFrame: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                if (state.webcamActive) {
                    webcamSampler.stop()
                    onClearWebcamFrame()
                    controller.updateWebcamState(active = false, statusMessage = "Mac Webcam stopped.")
                } else {
                    val started = webcamSampler.start()
                    if (started) {
                        controller.updateWebcamState(
                            active = true,
                            statusMessage = "Mac Webcam connected! Point cube face at the grid.",
                        )
                    } else {
                        controller.toggleSimulatedCamera(true)
                    }
                }
            },
        ) {
            Text(if (state.webcamActive) "Stop Mac Webcam" else "Start Mac Webcam")
        }
        FilterChip(
            selected = state.useSimulatedCamera,
            onClick = {
                if (state.webcamActive) {
                    webcamSampler.stop()
                    onClearWebcamFrame()
                }
                controller.toggleSimulatedCamera(!state.useSimulatedCamera)
            },
            label = { Text("Simulated Camera") },
        )
        OutlinedButton(onClick = { controller.applyPreset(CubePresetAction.CLEAR_UNPAINTED) }) {
            Text("Reset Scan")
        }
    }
}

@Composable
private fun CameraViewfinderAndGrid(
    state: ManualInputUiState,
    latestWebcamFrame: WebcamFrameSample?,
    onCaptureFace: () -> Unit,
) {
    val n = state.cubeState.size.dimension
    val targetFace = state.activeScanFace
    val allDone = state.scanSession.proposedCubeState != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(160.dp, 120.dp)
                    .background(Color.Black)
                    .border(1.dp, Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            WebcamFeedCanvas(
                frameSample = latestWebcamFrame,
                dimension = n,
                liveSamples = state.liveCameraSamples,
                targetFace = targetFace,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text =
                    if (allDone) {
                        "All 6 faces captured!"
                    } else {
                        "Target Face: ${targetFace.name} (${targetFace.symbol})"
                    },
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text =
                    state.scanSession.nextFaceGuidance?.rotationInstruction
                        ?: "Review & tap any sticker on the 3D cube above to adjust.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = onCaptureFace,
                enabled = !allDone,
            ) {
                Text("Capture Face ${targetFace.symbol} (${state.scanSession.capturedFaces.size + 1}/6)")
            }
        }
    }
}

@Composable
private fun WebcamFeedCanvas(
    frameSample: WebcamFrameSample?,
    dimension: Int,
    liveSamples: Map<StickerPosition, com.rubiksolver.domain.recognition.RgbColor>,
    targetFace: Face,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pixels = frameSample?.previewPixelsArgb
        if (pixels != null && frameSample.previewWidth > 0 && frameSample.previewHeight > 0) {
            val cellW = size.width / frameSample.previewWidth
            val cellH = size.height / frameSample.previewHeight
            for (py in 0 until frameSample.previewHeight step 2) {
                for (px in 0 until frameSample.previewWidth step 2) {
                    val argb = pixels[py * frameSample.previewWidth + px]
                    drawRect(
                        color = Color(argb),
                        topLeft = Offset(px * cellW, py * cellH),
                        size = Size(cellW * 2f, cellH * 2f),
                    )
                }
            }
        }

        val gridSize = size.minDimension * GRID_OVERLAY_FRACTION
        val startX = (size.width - gridSize) / 2f
        val startY = (size.height - gridSize) / 2f
        val step = gridSize / dimension

        for (r in 0 until dimension) {
            for (c in 0 until dimension) {
                val pos = StickerPosition(targetFace, r, c)
                val rgb = liveSamples[pos]
                val topLeft = Offset(startX + c * step, startY + r * step)
                if (rgb != null) {
                    val detected = ConstrainedColorClassifier.nearestColor(rgb.toLab())
                    drawRect(
                        color = stickerComposeColor(detected).copy(alpha = 0.78f),
                        topLeft = topLeft,
                        size = Size(step - 2f, step - 2f),
                    )
                }
                drawRect(
                    color = Color.White,
                    topLeft = topLeft,
                    size = Size(step - 2f, step - 2f),
                    style = Stroke(width = 2f),
                )
            }
        }
    }
}

@Composable
private fun ScrambleInputRow(
    scrambleText: String,
    onTextChanged: (String) -> Unit,
    onApplyScramble: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = scrambleText,
            onValueChange = onTextChanged,
            label = { Text("Scramble (e.g. R U R' U' F' U F)") },
            modifier = Modifier.weight(1f),
        )
        Button(onClick = onApplyScramble) {
            Text("Apply to 3D Cube")
        }
    }
}

@Composable
private fun MethodAndSolveSection(
    state: ManualInputUiState,
    controller: ManualInputController,
    onOpenSolveGuide: (() -> Unit)?,
) {
    val statusText =
        if (!state.isFullyPainted) {
            val remaining = state.totalStickers - state.paintedCount
            "Paint or scan the remaining $remaining stickers on the 3D cube to validate and solve."
        } else {
            state.validationReport.messages.joinToString("\n")
        }

    Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        state.availableMethods.forEach { method ->
            FilterChip(
                selected = state.selectedMethod == method,
                onClick = { controller.selectMethod(method) },
                label = { Text(method.name) },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { controller.generateSolution() },
            enabled = state.isFullyPainted && state.validationReport.result !is ValidationResult.Invalid,
        ) {
            Text("Generate Solution")
        }
        if (state.latestSolution != null && onOpenSolveGuide != null) {
            OutlinedButton(onClick = onOpenSolveGuide) {
                Text("Open in 3D Solve Guide →")
            }
        }
    }
    state.latestSolution?.let { solution ->
        SolutionSummaryCard(solution = solution)
    }
}

@Composable
private fun SolutionSummaryCard(solution: Solution) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Method: ${solution.method} — ${solution.totalMoves} moves",
            style = MaterialTheme.typography.titleSmall,
        )
        solution.stages.forEach { stage ->
            val notation = stage.steps.joinToString(" ") { it.move.notation }
            Text(text = "${stage.name}: $notation")
        }
    }
}

private fun stickerComposeColor(color: StickerColor): Color =
    when (color) {
        StickerColor.WHITE -> Color(WHITE_HEX)
        StickerColor.YELLOW -> Color(YELLOW_HEX)
        StickerColor.GREEN -> Color(GREEN_HEX)
        StickerColor.BLUE -> Color(BLUE_HEX)
        StickerColor.RED -> Color(RED_HEX)
        StickerColor.ORANGE -> Color(ORANGE_HEX)
    }

private const val LABEL_CHAR_COUNT: Int = 3
private const val WEBCAM_POLL_INTERVAL_MS: Long = 90L
private const val GRID_OVERLAY_FRACTION: Float = 0.72f
private const val CANVAS_BG_HEX: Long = 0xFF0F172A
private const val WHITE_HEX: Long = 0xFFF8FAFC
private const val YELLOW_HEX: Long = 0xFFFACC15
private const val GREEN_HEX: Long = 0xFF22C55E
private const val BLUE_HEX: Long = 0xFF2563EB
private const val RED_HEX: Long = 0xFFEF4444
private const val ORANGE_HEX: Long = 0xFFF97316
