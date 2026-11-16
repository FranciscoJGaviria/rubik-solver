package com.rubiksolver.presentation.input

import com.rubiksolver.application.cube.ApplyScrambleUseCase
import com.rubiksolver.application.cube.CorrectStickerUseCase
import com.rubiksolver.application.cube.CubeValidationReport
import com.rubiksolver.application.cube.ValidateCubeUseCase
import com.rubiksolver.application.recognition.GuidedCubeScanSession
import com.rubiksolver.application.recognition.GuidedScanUseCase
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
import com.rubiksolver.domain.recognition.ConstrainedColorClassifier
import com.rubiksolver.domain.recognition.RgbColor
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.presentation.cube3d.CameraOrbit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class InputMode {
    PAINT_3D,
    CAMERA_SCAN,
    SCRAMBLE,
}

enum class CubePresetAction {
    CLEAR_UNPAINTED,
    ANCHOR_CENTERS,
    FILL_SOLVED,
}

data class ManualInputUiState(
    val inputMode: InputMode = InputMode.PAINT_3D,
    val cubeState: CubeState,
    val unpaintedPositions: Set<StickerPosition>,
    val cameraOrbit: CameraOrbit = CameraOrbit.DEFAULT,
    val selectedFace: Face = Face.FRONT,
    val selectedBrushColor: StickerColor = StickerColor.WHITE,
    val scrambleText: String = ManualInputController.DEFAULT_SAMPLE_SCRAMBLE,
    val availableMethods: Set<Method>,
    val selectedMethod: Method,
    val validationReport: CubeValidationReport,
    val latestSolution: Solution? = null,
    val scanSession: GuidedCubeScanSession,
    val liveCameraSamples: Map<StickerPosition, RgbColor> = emptyMap(),
    val useSimulatedCamera: Boolean = false,
    val webcamActive: Boolean = false,
    val webcamStatusMessage: String = "Click 'Start Mac Webcam' or 'Simulated Camera' to scan 6 faces",
) {
    val totalStickers: Int
        get() = cubeState.size.totalStickers

    val paintedCount: Int
        get() = totalStickers - unpaintedPositions.size

    val isFullyPainted: Boolean
        get() = unpaintedPositions.isEmpty()

    val activeScanFace: Face
        get() = scanSession.nextFaceGuidance?.nextFace ?: Face.DOWN
}

class ManualInputController(
    initialSize: CubeSize = CubeSize.THREE,
    private val profileKind: ProfileKind = ProfileKind.ADULT,
    private val colorScheme: ColorScheme = ColorScheme.STANDARD,
    private val startUnpainted: Boolean = true,
) {
    private val correctStickerUseCase = CorrectStickerUseCase()
    private val applyScrambleUseCase = ApplyScrambleUseCase()
    private val validateCubeUseCase = ValidateCubeUseCase()
    private val getAvailableMethodsUseCase = GetAvailableMethodsUseCase()
    private val generateSolutionUseCase = GenerateSolutionUseCase()
    private val guidedScanUseCase = GuidedScanUseCase()

    private val simulatedReferenceCube: Map<CubeSize, CubeState> =
        mapOf(
            CubeSize.TWO to applyScrambleUseCase(CubeSize.TWO, "R U R' F2", colorScheme),
            CubeSize.THREE to applyScrambleUseCase(CubeSize.THREE, DEFAULT_SAMPLE_SCRAMBLE, colorScheme),
        )

    private val _state: MutableStateFlow<ManualInputUiState>
    val state: StateFlow<ManualInputUiState>

    init {
        val initialCube = CubeState.solved(initialSize, colorScheme)
        val initialUnpainted = if (startUnpainted) initialCube.allStickers().keys.toSet() else emptySet()
        val methodsSelection = getAvailableMethodsUseCase(initialSize, profileKind)
        val report = validateCubeUseCase(initialCube, profileKind, colorScheme)
        val scanSession = guidedScanUseCase.startSession(initialSize)
        _state =
            MutableStateFlow(
                ManualInputUiState(
                    cubeState = initialCube,
                    unpaintedPositions = initialUnpainted,
                    availableMethods = methodsSelection.available,
                    selectedMethod = methodsSelection.defaultMethod,
                    validationReport = report,
                    scanSession = scanSession,
                ),
            )
        state = _state.asStateFlow()
    }

    fun selectInputMode(mode: InputMode) {
        _state.update { current ->
            val nextOrbit =
                if (mode == InputMode.CAMERA_SCAN) {
                    orbitForFace(current.activeScanFace)
                } else {
                    current.cameraOrbit
                }
            current.copy(inputMode = mode, cameraOrbit = nextOrbit)
        }
    }

    fun selectSize(size: CubeSize) {
        val solved = CubeState.solved(size, colorScheme)
        val unpainted = if (startUnpainted) solved.allStickers().keys.toSet() else emptySet()
        val methodsSelection =
            getAvailableMethodsUseCase(
                size = size,
                profileKind = profileKind,
                lastUsedMethod = _state.value.selectedMethod,
            )
        val report = validateCubeUseCase(solved, profileKind, colorScheme)
        val scanSession = guidedScanUseCase.startSession(size)
        _state.update { current ->
            current.copy(
                cubeState = solved,
                unpaintedPositions = unpainted,
                availableMethods = methodsSelection.available,
                selectedMethod = methodsSelection.defaultMethod,
                validationReport = report,
                latestSolution = null,
                scanSession = scanSession,
                liveCameraSamples = emptyMap(),
            )
        }
    }

    fun orbit(
        deltaYawDegrees: Double,
        deltaPitchDegrees: Double,
    ) {
        _state.update { current ->
            current.copy(cameraOrbit = current.cameraOrbit.rotateBy(deltaYawDegrees, deltaPitchDegrees))
        }
    }

    fun selectFace(face: Face) {
        _state.update { current ->
            current.copy(
                selectedFace = face,
                cameraOrbit = orbitForFace(face),
            )
        }
    }

    fun selectBrushColor(color: StickerColor) {
        _state.update { it.copy(selectedBrushColor = color) }
    }

    fun paintSticker(position: StickerPosition) {
        _state.update { current ->
            val updatedCube = correctStickerUseCase(current.cubeState, position, current.selectedBrushColor)
            val updatedUnpainted = current.unpaintedPositions - position
            val report = validateCubeUseCase(updatedCube, profileKind, colorScheme)
            current.copy(
                cubeState = updatedCube,
                unpaintedPositions = updatedUnpainted,
                validationReport = report,
                latestSolution = null,
            )
        }
    }

    fun applyPreset(action: CubePresetAction) {
        _state.update { current ->
            val solved = CubeState.solved(current.cubeState.size, colorScheme)
            when (action) {
                CubePresetAction.CLEAR_UNPAINTED ->
                    current.copy(
                        cubeState = solved,
                        unpaintedPositions = solved.allStickers().keys.toSet(),
                        latestSolution = null,
                        scanSession = guidedScanUseCase.startSession(solved.size),
                        liveCameraSamples = emptyMap(),
                    )
                CubePresetAction.ANCHOR_CENTERS -> {
                    if (current.cubeState.size != CubeSize.THREE) return@update current
                    val centers = Face.entries.map { face -> StickerPosition(face, 1, 1) }.toSet()
                    var updated = current.cubeState
                    centers.forEach { pos ->
                        updated = correctStickerUseCase(updated, pos, colorScheme.colorOf(pos.face))
                    }
                    current.copy(
                        cubeState = updated,
                        unpaintedPositions = current.unpaintedPositions - centers,
                        validationReport = validateCubeUseCase(updated, profileKind, colorScheme),
                        latestSolution = null,
                    )
                }
                CubePresetAction.FILL_SOLVED ->
                    current.copy(
                        cubeState = solved,
                        unpaintedPositions = emptySet(),
                        validationReport = validateCubeUseCase(solved, profileKind, colorScheme),
                        latestSolution = null,
                    )
            }
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
                unpaintedPositions = emptySet(),
                scrambleText = notation,
                validationReport = report,
                latestSolution = null,
            )
        }
    }

    fun updateWebcamState(
        active: Boolean = _state.value.webcamActive,
        samples: Map<StickerPosition, RgbColor> = _state.value.liveCameraSamples,
        statusMessage: String,
    ) {
        _state.update { current ->
            current.copy(
                webcamActive = active,
                useSimulatedCamera = if (active) false else current.useSimulatedCamera,
                liveCameraSamples = samples,
                webcamStatusMessage = statusMessage,
            )
        }
    }

    fun toggleSimulatedCamera(enabled: Boolean) {
        _state.update { current ->
            val face = current.activeScanFace
            val samples =
                if (enabled) {
                    buildSimulatedFaceSamples(simulatedReferenceCube.getValue(current.cubeState.size), face)
                } else {
                    emptyMap()
                }
            current.copy(
                useSimulatedCamera = enabled,
                webcamActive = if (enabled) false else current.webcamActive,
                liveCameraSamples = samples,
                webcamStatusMessage =
                    if (enabled) {
                        "Simulated Camera active — Showing noisy RGB for face $face"
                    } else {
                        "Camera idle"
                    },
            )
        }
    }

    fun captureCurrentScanFace() {
        _state.update { current ->
            val face = current.activeScanFace
            val refCube = simulatedReferenceCube.getValue(current.cubeState.size)
            val samples = current.liveCameraSamples.ifEmpty { buildSimulatedFaceSamples(refCube, face) }
            val nextSession =
                guidedScanUseCase.captureFace(
                    session = current.scanSession,
                    face = face,
                    faceRgbSamples = samples,
                    scheme = colorScheme,
                )

            val (updatedCube, updatedUnpainted) =
                applyCapturedFaceToCube(current, nextSession, samples, correctStickerUseCase)
            val nextFace = nextSession.nextFaceGuidance?.nextFace
            val nextSimSamples =
                if (current.useSimulatedCamera && nextFace != null) {
                    buildSimulatedFaceSamples(refCube, nextFace)
                } else {
                    emptyMap()
                }
            val statusMsg =
                if (nextSession.proposedCubeState != null) {
                    "All 6 faces scanned! Review the 3D cube (tap any sticker to correct) or click Generate Solution."
                } else {
                    nextSession.nextFaceGuidance?.rotationInstruction ?: "Face $face captured."
                }

            current.copy(
                cubeState = updatedCube,
                unpaintedPositions = updatedUnpainted,
                scanSession = nextSession,
                liveCameraSamples = nextSimSamples,
                cameraOrbit = nextFace?.let(::orbitForFace) ?: CameraOrbit.DEFAULT,
                validationReport = validateCubeUseCase(updatedCube, profileKind, colorScheme),
                webcamStatusMessage = statusMsg,
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
        val valid = current.isFullyPainted && current.validationReport.result !is ValidationResult.Invalid
        val solution =
            if (valid) {
                generateSolutionUseCase(current.cubeState, current.selectedMethod, colorScheme)
            } else {
                null
            }
        _state.update { it.copy(latestSolution = solution) }
        return solution
    }

    companion object {
        internal const val DEFAULT_SAMPLE_SCRAMBLE: String = "R U R' U' F' U F"
    }
}

private const val MAX_RGB: Int = 255
private const val NOISE_MODULO: Int = 7
private const val NOISE_CENTER: Int = 3
private const val ROW_FACTOR: Int = 3
private const val COL_FACTOR: Int = 5
private const val YAW_FRONT: Double = -22.0
private const val YAW_RIGHT: Double = -112.0
private const val YAW_BACK: Double = 158.0
private const val YAW_LEFT: Double = 68.0
private const val YAW_VERTICAL: Double = -25.0
private const val PITCH_SIDE: Double = 18.0
private const val PITCH_UP: Double = 68.0
private const val PITCH_DOWN: Double = -68.0

private fun applyCapturedFaceToCube(
    current: ManualInputUiState,
    nextSession: GuidedCubeScanSession,
    samples: Map<StickerPosition, RgbColor>,
    correctStickerUseCase: CorrectStickerUseCase,
): Pair<CubeState, Set<StickerPosition>> {
    val proposed = nextSession.proposedCubeState
    if (proposed != null) {
        return proposed to emptySet()
    }
    var working = current.cubeState
    samples.forEach { (pos, rgb) ->
        val detected = ConstrainedColorClassifier.nearestColor(rgb.toLab())
        working = correctStickerUseCase(working, pos, detected)
    }
    return working to (current.unpaintedPositions - samples.keys)
}

internal fun orbitForFace(face: Face): CameraOrbit =
    when (face) {
        Face.FRONT -> CameraOrbit(yawDegrees = YAW_FRONT, pitchDegrees = PITCH_SIDE)
        Face.RIGHT -> CameraOrbit(yawDegrees = YAW_RIGHT, pitchDegrees = PITCH_SIDE)
        Face.BACK -> CameraOrbit(yawDegrees = YAW_BACK, pitchDegrees = PITCH_SIDE)
        Face.LEFT -> CameraOrbit(yawDegrees = YAW_LEFT, pitchDegrees = PITCH_SIDE)
        Face.UP -> CameraOrbit(yawDegrees = YAW_VERTICAL, pitchDegrees = PITCH_UP)
        Face.DOWN -> CameraOrbit(yawDegrees = YAW_VERTICAL, pitchDegrees = PITCH_DOWN)
    }

private fun buildSimulatedFaceSamples(
    refCube: CubeState,
    face: Face,
): Map<StickerPosition, RgbColor> {
    val n = refCube.size.dimension
    val map = linkedMapOf<StickerPosition, RgbColor>()
    for (r in 0 until n) {
        for (c in 0 until n) {
            val pos = StickerPosition(face, r, c)
            val ideal = RgbColor.canonicalFor(refCube.stickerAt(pos))
            val offset = ((r * ROW_FACTOR + c * COL_FACTOR + face.ordinal) % NOISE_MODULO) - NOISE_CENTER
            map[pos] =
                RgbColor(
                    r = (ideal.r + offset * 2).coerceIn(0, MAX_RGB),
                    g = (ideal.g - offset * 2).coerceIn(0, MAX_RGB),
                    b = (ideal.b + offset).coerceIn(0, MAX_RGB),
                )
        }
    }
    return map
}
