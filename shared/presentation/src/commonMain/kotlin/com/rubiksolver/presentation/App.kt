package com.rubiksolver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.presentation.cube3d.CubeViewerController
import com.rubiksolver.presentation.cube3d.CubeViewerScreen

@Composable
fun App(defaultSize: CubeSize = CubeSize.THREE) {
    MaterialTheme {
        val controller = remember(defaultSize) { CubeViewerController(CubeState.solved(defaultSize)) }
        CubeViewerScreen(controller = controller)
    }
}
