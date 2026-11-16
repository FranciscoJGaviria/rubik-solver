package com.rubiksolver.presentation.guide

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.profile.Handedness
import com.rubiksolver.presentation.cube3d.Cube3DCanvas
import com.rubiksolver.presentation.cube3d.CubeViewerController
import kotlinx.coroutines.delay

@Composable
fun SolveGuideScreen(
    controller: SolveGuideController = remember { SolveGuideController() },
    onRequestReenterCube: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by controller.state.collectAsState()
    val session = uiState.session
    val viewerController = remember { CubeViewerController(session.currentCubeState) }
    val viewerState by viewerController.state.collectAsState()

    LaunchedEffect(session.currentCubeState, session.lastAnimatedMove) {
        viewerController.setCubeState(session.currentCubeState)
    }

    LaunchedEffect(session.isPlaying, session.currentStepNumber, session.speed) {
        if (session.isPlaying) {
            delay(session.speed.durationMillis)
            controller.nextStep()
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GuideHeaderSection(uiState = uiState, controller = controller)

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.controlsPlacement == PrimaryControlsPlacement.LEFT) {
                    GuidePlaybackControlsColumn(
                        uiState = uiState,
                        controller = controller,
                        onRequestReenterCube = onRequestReenterCube,
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    Cube3DCanvas(
                        viewerState = viewerState,
                        onOrbitDrag = viewerController::orbit,
                        onFreePlaySwipe = { _, _, _ -> },
                    )
                }
                if (uiState.controlsPlacement == PrimaryControlsPlacement.RIGHT) {
                    GuidePlaybackControlsColumn(
                        uiState = uiState,
                        controller = controller,
                        onRequestReenterCube = onRequestReenterCube,
                    )
                }
            }

            GuideStepFooter(uiState = uiState)
        }
    }
}

@Composable
private fun GuideHeaderSection(
    uiState: SolveGuideUiState,
    controller: SolveGuideController,
) {
    val session = uiState.session
    val holding = session.holdingInstruction
    val upText = "${holding.upColor} (${holding.upFace.symbol}) UP"
    val frontText = "${holding.frontColor} (${holding.frontFace.symbol}) FRONT"
    Text(
        text = "Hold $upText and $frontText",
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "Stage ${session.currentStageNumber}: ${session.currentStageName} — Step ${session.progressIndicator}",
        style = MaterialTheme.typography.titleMedium,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimationSpeed.entries.forEach { spd ->
            FilterChip(
                selected = session.speed == spd,
                onClick = { controller.setSpeed(spd) },
                label = { Text(spd.name) },
            )
        }
        Handedness.entries.forEach { hand ->
            FilterChip(
                selected = uiState.handedness == hand,
                onClick = { controller.setHandedness(hand) },
                label = { Text("${hand.name} Hand") },
            )
        }
    }
}

@Composable
private fun GuidePlaybackControlsColumn(
    uiState: SolveGuideUiState,
    controller: SolveGuideController,
    onRequestReenterCube: () -> Unit,
) {
    val session = uiState.session
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = controller::togglePlayPause) {
            Text(if (session.isPlaying) "Pause" else "Play")
        }
        Button(
            onClick = controller::nextStep,
            enabled = session.currentStepNumber < session.solution.totalMoves,
        ) {
            Text("Next")
        }
        OutlinedButton(
            onClick = controller::previousStep,
            enabled = session.currentStepNumber > 0,
        ) {
            Text("Prev")
        }
        session.solution.stages.forEachIndexed { idx, stage ->
            FilterChip(
                selected = session.currentStageNumber == idx + 1,
                onClick = { controller.jumpToStage(idx) },
                label = { Text(stage.name.take(STAGE_LABEL_LIMIT)) },
            )
        }
        OutlinedButton(onClick = onRequestReenterCube) {
            Text("Lost? Re-enter")
        }
    }
}

@Composable
private fun GuideStepFooter(uiState: SolveGuideUiState) {
    val session = uiState.session
    session.completionSummary?.let { summary ->
        Text(
            text = "Solved! Method: ${summary.method} in ${summary.totalMoves} moves!",
            style = MaterialTheme.typography.titleMedium,
        )
        return
    }
    session.currentStep?.let { step ->
        val explanationText = step.explanation ?: "Perform move ${step.move.notation}"
        Text(
            text = "Move: ${step.move.notation} — $explanationText",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private const val STAGE_LABEL_LIMIT: Int = 12
