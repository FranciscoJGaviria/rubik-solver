package com.rubiksolver.presentation.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.solving.Solution

@Composable
fun ManualCubeInputScreen(
    controller: ManualInputController = remember { ManualInputController() },
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SizeAndFaceSelector(state = state, controller = controller)
            ColorBrushRow(
                selectedColor = state.selectedBrushColor,
                onSelectColor = controller::selectBrushColor,
            )
            FaceStickerGrid(
                state = state,
                onPaintSticker = controller::paintSticker,
            )
            ScrambleInputRow(
                scrambleText = state.scrambleText,
                onTextChanged = controller::updateScrambleText,
                onApplyScramble = { controller.applyScramble() },
            )
            MethodAndSolveSection(state = state, controller = controller)
            state.latestSolution?.let { solution ->
                SolutionSummaryCard(solution = solution)
            }
        }
    }
}

@Composable
private fun SizeAndFaceSelector(
    state: ManualInputUiState,
    controller: ManualInputController,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CubeSize.entries.forEach { size ->
            FilterChip(
                selected = state.cubeState.size == size,
                onClick = { controller.selectSize(size) },
                label = { Text("${size.dimension}x${size.dimension}") },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Face.entries.forEach { face ->
            FilterChip(
                selected = state.selectedFace == face,
                onClick = { controller.selectFace(face) },
                label = { Text(face.symbol.toString()) },
            )
        }
    }
}

@Composable
private fun ColorBrushRow(
    selectedColor: StickerColor,
    onSelectColor: (StickerColor) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StickerColor.entries.forEach { color ->
            FilterChip(
                selected = selectedColor == color,
                onClick = { onSelectColor(color) },
                label = { Text(color.name.take(LABEL_CHAR_COUNT)) },
            )
        }
    }
}

@Composable
private fun FaceStickerGrid(
    state: ManualInputUiState,
    onPaintSticker: (StickerPosition) -> Unit,
) {
    val n = state.cubeState.size.dimension
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (r in 0 until n) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until n) {
                    val pos = StickerPosition(state.selectedFace, r, c)
                    val color = state.cubeState.stickerAt(pos)
                    val isHighlighted = pos in state.validationReport.highlightedStickers
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .background(stickerComposeColor(color))
                                .border(
                                    width = if (isHighlighted) 3.dp else 1.dp,
                                    color = if (isHighlighted) Color.Red else Color.DarkGray,
                                ).clickable { onPaintSticker(pos) },
                    )
                }
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
            label = { Text("Scramble (e.g. R U R' U')") },
            modifier = Modifier.weight(1f),
        )
        Button(onClick = onApplyScramble) {
            Text("Apply")
        }
    }
}

@Composable
private fun MethodAndSolveSection(
    state: ManualInputUiState,
    controller: ManualInputController,
) {
    Text(text = state.validationReport.messages.joinToString("\n"))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        state.availableMethods.forEach { method ->
            FilterChip(
                selected = state.selectedMethod == method,
                onClick = { controller.selectMethod(method) },
                label = { Text(method.name) },
            )
        }
    }
    Button(
        onClick = { controller.generateSolution() },
        enabled = state.validationReport.result !is ValidationResult.Invalid,
    ) {
        Text("Generate Solution")
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
private const val WHITE_HEX: Long = 0xFFF8FAFC
private const val YELLOW_HEX: Long = 0xFFFACC15
private const val GREEN_HEX: Long = 0xFF22C55E
private const val BLUE_HEX: Long = 0xFF2563EB
private const val RED_HEX: Long = 0xFFEF4444
private const val ORANGE_HEX: Long = 0xFFF97316
