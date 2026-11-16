package com.rubiksolver.presentation.cube3d

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.Move

@Composable
fun CubeViewerScreen(
    controller: CubeViewerController = remember { CubeViewerController() },
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()

    LaunchedEffect(state.activeAnimation?.move) {
        val active = state.activeAnimation ?: return@LaunchedEffect
        val startNanos = withFrameNanos { it }
        var currentProgress = active.progress
        while (currentProgress < 1.0) {
            val now = withFrameNanos { it }
            val elapsedMs = (now - startNanos) / NANOS_PER_MILLISECOND
            currentProgress = (elapsedMs / MOVE_ANIMATION_DURATION_MS).coerceIn(0.0, 1.0)
            controller.stepAnimation(currentProgress)
        }
        controller.completeMoveAnimation()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ViewerTopControls(state = state, controller = controller)

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Cube3DCanvas(
                    viewerState = state,
                    onOrbitDrag = controller::orbit,
                    onFreePlaySwipe = controller::onFreePlaySwipe,
                )
            }

            QuickMoveButtonsRow(onMoveSelected = controller::startMoveAnimation)
        }
    }
}

@Composable
private fun ViewerTopControls(
    state: CubeViewerState,
    controller: CubeViewerController,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Rubik Solver 3D (${state.cubeState.size.dimension}x${state.cubeState.size.dimension})",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.cubeState.size == CubeSize.TWO,
                onClick = { controller.setCubeSize(CubeSize.TWO) },
                label = { Text("2x2") },
            )
            FilterChip(
                selected = state.cubeState.size == CubeSize.THREE,
                onClick = { controller.setCubeSize(CubeSize.THREE) },
                label = { Text("3x3") },
            )
            FilterChip(
                selected = state.freePlayMode,
                onClick = { controller.setFreePlayMode(!state.freePlayMode) },
                label = { Text("Free Play") },
            )
            FilterChip(
                selected = state.colorBlindAssistance,
                onClick = { controller.setColorBlindAssistance(!state.colorBlindAssistance) },
                label = { Text("Symbols") },
            )
            OutlinedButton(onClick = controller::resetView) {
                Text("Reset View")
            }
        }
    }
}

@Composable
private fun QuickMoveButtonsRow(onMoveSelected: (Move) -> Unit) {
    val quickMoves = listOf("U", "U'", "R", "R'", "F", "F'", "L", "D")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        quickMoves.forEach { token ->
            Button(
                onClick = { onMoveSelected(Move.parse(token)) },
                modifier = Modifier.weight(1f),
            ) {
                Text(token)
            }
        }
    }
}

private const val MOVE_ANIMATION_DURATION_MS: Double = 280.0
private const val NANOS_PER_MILLISECOND: Double = 1_000_000.0
