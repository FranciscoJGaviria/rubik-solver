package com.rubiksolver.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.presentation.cube3d.CubeViewerController
import com.rubiksolver.presentation.cube3d.CubeViewerScreen
import com.rubiksolver.presentation.input.ManualCubeInputScreen
import com.rubiksolver.presentation.input.ManualInputController

@Composable
fun App(defaultSize: CubeSize = CubeSize.THREE) {
    MaterialTheme {
        var selectedTab by remember { mutableStateOf(0) }
        val viewerController = remember(defaultSize) { CubeViewerController(CubeState.solved(defaultSize)) }
        val inputController = remember(defaultSize) { ManualInputController(initialSize = defaultSize) }

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("3D Viewer") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Input & Solve") },
                )
            }
            if (selectedTab == 0) {
                CubeViewerScreen(controller = viewerController, modifier = Modifier.weight(1f))
            } else {
                ManualCubeInputScreen(controller = inputController, modifier = Modifier.weight(1f))
            }
        }
    }
}
