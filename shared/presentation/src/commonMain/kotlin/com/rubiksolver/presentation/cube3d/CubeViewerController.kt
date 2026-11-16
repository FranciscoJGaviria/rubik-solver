package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Move
import com.rubiksolver.domain.cube.StickerPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CubeViewerState(
    val cubeState: CubeState,
    val cameraOrbit: CameraOrbit = CameraOrbit.DEFAULT,
    val activeAnimation: LayerTurnAnimation? = null,
    val freePlayMode: Boolean = false,
    val colorBlindAssistance: Boolean = false,
)

class CubeViewerController(
    initialCubeState: CubeState = CubeState.solved(CubeSize.THREE),
) {
    private val _state = MutableStateFlow(CubeViewerState(cubeState = initialCubeState))
    val state: StateFlow<CubeViewerState> = _state.asStateFlow()

    fun setCubeSize(size: CubeSize) {
        _state.update { current ->
            current.copy(
                cubeState = CubeState.solved(size),
                activeAnimation = null,
            )
        }
    }

    fun setCubeState(newCubeState: CubeState) {
        _state.update { current ->
            current.copy(cubeState = newCubeState, activeAnimation = null)
        }
    }

    fun orbit(
        deltaYawDegrees: Double,
        deltaPitchDegrees: Double,
    ) {
        _state.update { current ->
            current.copy(
                cameraOrbit = current.cameraOrbit.rotateBy(deltaYawDegrees, deltaPitchDegrees),
            )
        }
    }

    fun resetView() {
        _state.update { current ->
            current.copy(cameraOrbit = current.cameraOrbit.reset())
        }
    }

    fun setFreePlayMode(enabled: Boolean) {
        _state.update { current ->
            current.copy(freePlayMode = enabled)
        }
    }

    fun setColorBlindAssistance(enabled: Boolean) {
        _state.update { current ->
            current.copy(colorBlindAssistance = enabled)
        }
    }

    fun startMoveAnimation(move: Move) {
        _state.update { current ->
            val baseCube =
                current.activeAnimation?.let { prev ->
                    current.cubeState.apply(prev.move)
                } ?: current.cubeState
            current.copy(
                cubeState = baseCube,
                activeAnimation = LayerTurnAnimation(move = move, progress = 0.0),
            )
        }
    }

    fun stepAnimation(progress: Double) {
        _state.update { current ->
            val anim = current.activeAnimation ?: return@update current
            current.copy(activeAnimation = anim.withProgress(progress))
        }
    }

    fun completeMoveAnimation() {
        _state.update { current ->
            val anim = current.activeAnimation ?: return@update current
            current.copy(
                cubeState = current.cubeState.apply(anim.move),
                activeAnimation = null,
            )
        }
    }

    fun onFreePlaySwipe(
        touchedPosition: StickerPosition,
        dragX: Double,
        dragY: Double,
    ): Move? {
        val current = _state.value
        val resolvedMove =
            if (current.freePlayMode) {
                FreePlayGesturePicker.resolveMove(
                    size = current.cubeState.size,
                    touchedPosition = touchedPosition,
                    dragX = dragX,
                    dragY = dragY,
                )
            } else {
                null
            }

        if (resolvedMove != null) {
            _state.update { stateNow ->
                stateNow.copy(
                    cubeState = stateNow.cubeState.apply(resolvedMove),
                    activeAnimation = null,
                )
            }
        }
        return resolvedMove
    }

    fun projectScene(
        viewportWidth: Double,
        viewportHeight: Double,
    ): ProjectedCubeScene {
        val current = _state.value
        return Cube3DProjectionEngine.project(
            state = current.cubeState,
            orbit = current.cameraOrbit,
            animation = current.activeAnimation,
            colorBlindSymbols = current.colorBlindAssistance,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
        )
    }
}
