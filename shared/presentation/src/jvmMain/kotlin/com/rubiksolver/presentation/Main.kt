package com.rubiksolver.presentation

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

private const val DEFAULT_WINDOW_WIDTH_DP: Int = 520
private const val DEFAULT_WINDOW_HEIGHT_DP: Int = 920

fun main() =
    application {
        val windowState =
            rememberWindowState(
                size = DpSize(DEFAULT_WINDOW_WIDTH_DP.dp, DEFAULT_WINDOW_HEIGHT_DP.dp),
            )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Rubik Solver — Interactive 3D & Tutor",
        ) {
            App()
        }
    }
