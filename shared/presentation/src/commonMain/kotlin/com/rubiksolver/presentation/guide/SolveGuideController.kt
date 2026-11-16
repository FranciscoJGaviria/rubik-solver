package com.rubiksolver.presentation.guide

import com.rubiksolver.application.solving.ManageSolveGuideUseCase
import com.rubiksolver.application.solving.SolveGuideSession
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.profile.Handedness
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PrimaryControlsPlacement {
    LEFT,
    RIGHT,
}

data class SolveGuideUiState(
    val session: SolveGuideSession,
    val handedness: Handedness = Handedness.RIGHT,
) {
    val controlsPlacement: PrimaryControlsPlacement
        get() =
            if (handedness == Handedness.LEFT) {
                PrimaryControlsPlacement.LEFT
            } else {
                PrimaryControlsPlacement.RIGHT
            }
}

class SolveGuideController(
    initialState: CubeState = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("R U R' U'")),
    initialMethod: Method = Method.BEGINNER,
    defaultSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    initialHandedness: Handedness = Handedness.RIGHT,
) {
    private val manageGuideUseCase = ManageSolveGuideUseCase()

    private val _state: MutableStateFlow<SolveGuideUiState>
    val state: StateFlow<SolveGuideUiState>

    init {
        val initialSession =
            manageGuideUseCase.startGuide(
                initialState = initialState,
                method = initialMethod,
                defaultSpeed = defaultSpeed,
            )
        _state =
            MutableStateFlow(
                SolveGuideUiState(
                    session = initialSession,
                    handedness = initialHandedness,
                ),
            )
        state = _state.asStateFlow()
    }

    fun loadSolution(
        initialState: CubeState,
        solution: Solution,
    ) {
        _state.update { current ->
            val newSession =
                manageGuideUseCase.startFromSolution(
                    initialState = initialState,
                    solution = solution,
                    defaultSpeed = current.session.speed,
                )
            current.copy(session = newSession)
        }
    }

    fun nextStep() {
        _state.update { current ->
            current.copy(session = manageGuideUseCase.stepForward(current.session))
        }
    }

    fun previousStep() {
        _state.update { current ->
            current.copy(session = manageGuideUseCase.stepBackward(current.session))
        }
    }

    fun togglePlayPause() {
        _state.update { current ->
            current.copy(
                session = manageGuideUseCase.setPlaying(current.session, !current.session.isPlaying),
            )
        }
    }

    fun jumpToStage(stageIndex: Int) {
        _state.update { current ->
            current.copy(session = manageGuideUseCase.jumpToStage(current.session, stageIndex))
        }
    }

    fun setSpeed(speed: AnimationSpeed) {
        _state.update { current ->
            current.copy(session = manageGuideUseCase.setSpeed(current.session, speed))
        }
    }

    fun setHandedness(handedness: Handedness) {
        _state.update { current ->
            current.copy(handedness = handedness)
        }
    }

    fun recoverFromLostState(reenteredState: CubeState) {
        _state.update { current ->
            current.copy(
                session = manageGuideUseCase.recoverWhenLost(current.session, reenteredState),
            )
        }
    }
}
