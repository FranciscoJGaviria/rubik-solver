package com.rubiksolver.presentation.input

import com.rubiksolver.application.cube.ApplyScrambleUseCase
import com.rubiksolver.application.cube.CorrectStickerUseCase
import com.rubiksolver.application.cube.CubeValidationReport
import com.rubiksolver.application.cube.ValidateCubeUseCase
import com.rubiksolver.application.solving.GenerateSolutionUseCase
import com.rubiksolver.application.solving.GetAvailableMethodsUseCase
import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.cube.StickerPosition
import com.rubiksolver.domain.cube.ValidationResult
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ManualInputUiState(
    val cubeState: CubeState,
    val selectedFace: Face = Face.FRONT,
    val selectedBrushColor: StickerColor = StickerColor.WHITE,
    val scrambleText: String = "",
    val availableMethods: Set<Method>,
    val selectedMethod: Method,
    val validationReport: CubeValidationReport,
    val latestSolution: Solution? = null,
)

class ManualInputController(
    initialSize: CubeSize = CubeSize.THREE,
    private val profileKind: ProfileKind = ProfileKind.ADULT,
    private val colorScheme: ColorScheme = ColorScheme.STANDARD,
) {
    private val correctStickerUseCase = CorrectStickerUseCase()
    private val applyScrambleUseCase = ApplyScrambleUseCase()
    private val validateCubeUseCase = ValidateCubeUseCase()
    private val getAvailableMethodsUseCase = GetAvailableMethodsUseCase()
    private val generateSolutionUseCase = GenerateSolutionUseCase()

    private val _state: MutableStateFlow<ManualInputUiState>
    val state: StateFlow<ManualInputUiState>

    init {
        val initialCube = CubeState.solved(initialSize, colorScheme)
        val methodsSelection = getAvailableMethodsUseCase(initialSize, profileKind)
        val report = validateCubeUseCase(initialCube, profileKind, colorScheme)
        _state =
            MutableStateFlow(
                ManualInputUiState(
                    cubeState = initialCube,
                    availableMethods = methodsSelection.available,
                    selectedMethod = methodsSelection.defaultMethod,
                    validationReport = report,
                ),
            )
        state = _state.asStateFlow()
    }

    fun selectSize(size: CubeSize) {
        val solved = CubeState.solved(size, colorScheme)
        val methodsSelection =
            getAvailableMethodsUseCase(
                size = size,
                profileKind = profileKind,
                lastUsedMethod = _state.value.selectedMethod,
            )
        val report = validateCubeUseCase(solved, profileKind, colorScheme)
        _state.update { current ->
            current.copy(
                cubeState = solved,
                availableMethods = methodsSelection.available,
                selectedMethod = methodsSelection.defaultMethod,
                validationReport = report,
                latestSolution = null,
            )
        }
    }

    fun selectFace(face: Face) {
        _state.update { it.copy(selectedFace = face) }
    }

    fun selectBrushColor(color: StickerColor) {
        _state.update { it.copy(selectedBrushColor = color) }
    }

    fun paintSticker(position: StickerPosition) {
        _state.update { current ->
            val updatedCube = correctStickerUseCase(current.cubeState, position, current.selectedBrushColor)
            val report = validateCubeUseCase(updatedCube, profileKind, colorScheme)
            current.copy(
                cubeState = updatedCube,
                validationReport = report,
                latestSolution = null,
            )
        }
    }

    fun updateScrambleText(text: String) {
        _state.update { it.copy(scrambleText = text) }
    }

    fun applyScramble(notation: String = _state.value.scrambleText) {
        _state.update { current ->
            val scrambled = applyScrambleUseCase(current.cubeState.size, notation, colorScheme)
            val report = validateCubeUseCase(scrambled, profileKind, colorScheme)
            current.copy(
                cubeState = scrambled,
                scrambleText = notation,
                validationReport = report,
                latestSolution = null,
            )
        }
    }

    fun selectMethod(method: Method) {
        _state.update { current ->
            if (method in current.availableMethods) {
                current.copy(selectedMethod = method, latestSolution = null)
            } else {
                current
            }
        }
    }

    fun generateSolution(): Solution? {
        val current = _state.value
        val valid = current.validationReport.result !is ValidationResult.Invalid
        val solution =
            if (valid) {
                generateSolutionUseCase(current.cubeState, current.selectedMethod, colorScheme)
            } else {
                null
            }
        _state.update { it.copy(latestSolution = solution) }
        return solution
    }
}
