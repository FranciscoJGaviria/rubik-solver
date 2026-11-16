package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.ColorScheme
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.profile.AnimationSpeed
import com.rubiksolver.domain.solving.CompletionSummary
import com.rubiksolver.domain.solving.HoldingInstruction
import com.rubiksolver.domain.solving.Method
import com.rubiksolver.domain.solving.Solution
import com.rubiksolver.domain.solving.Stage

class ManageSolveGuideUseCase(
    private val generateSolutionUseCase: GenerateSolutionUseCase = GenerateSolutionUseCase(),
) {
    fun startGuide(
        initialState: CubeState,
        method: Method,
        defaultSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): SolveGuideSession {
        val solution = generateSolutionUseCase(initialState, method, scheme)
        return startFromSolution(initialState, solution, defaultSpeed, scheme)
    }

    fun startFromSolution(
        initialState: CubeState,
        solution: Solution,
        defaultSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): SolveGuideSession {
        val holding = HoldingInstruction.fromScheme(scheme)
        val (stageNum, stageName) = resolveStageForStep(solution.stages, 0)
        val completion =
            if (solution.totalMoves == 0 && initialState.isSolved()) {
                CompletionSummary(method = solution.method, totalMoves = 0, celebrated = true)
            } else {
                null
            }
        return SolveGuideSession(
            initialCubeState = initialState,
            currentCubeState = initialState,
            solution = solution,
            holdingInstruction = holding,
            currentStepNumber = 0,
            currentStageNumber = stageNum,
            currentStageName = stageName,
            lastAnimatedMove = null,
            isPlaying = false,
            speed = defaultSpeed,
            completionSummary = completion,
        )
    }

    fun stepForward(session: SolveGuideSession): SolveGuideSession {
        val total = session.solution.totalMoves
        return if (session.currentStepNumber >= total) {
            session.copy(isPlaying = false)
        } else {
            val nextStepNum = session.currentStepNumber + 1
            val step = session.solution.steps[nextStepNum - 1]
            val nextCube = session.currentCubeState.apply(step.move)
            val (stageNum, stageName) = resolveStageForStep(session.solution.stages, nextStepNum)
            val completed = nextStepNum == total && nextCube.isSolved()
            val summary =
                if (completed) {
                    CompletionSummary(method = session.solution.method, totalMoves = total, celebrated = true)
                } else {
                    null
                }
            session.copy(
                currentCubeState = nextCube,
                currentStepNumber = nextStepNum,
                currentStageNumber = stageNum,
                currentStageName = stageName,
                lastAnimatedMove = step.move,
                isPlaying = if (completed) false else session.isPlaying,
                completionSummary = summary,
            )
        }
    }

    fun stepBackward(session: SolveGuideSession): SolveGuideSession =
        if (session.currentStepNumber <= 0) {
            session
        } else {
            val currentStep = session.solution.steps[session.currentStepNumber - 1]
            val reverseMove = currentStep.move.inverse()
            val prevStepNum = session.currentStepNumber - 1
            val prevCube = session.currentCubeState.apply(reverseMove)
            val (stageNum, stageName) = resolveStageForStep(session.solution.stages, prevStepNum)
            session.copy(
                currentCubeState = prevCube,
                currentStepNumber = prevStepNum,
                currentStageNumber = stageNum,
                currentStageName = stageName,
                lastAnimatedMove = reverseMove,
                completionSummary = null,
            )
        }

    fun jumpToStep(
        session: SolveGuideSession,
        targetStepNumber: Int,
    ): SolveGuideSession {
        val clamped = targetStepNumber.coerceIn(0, session.solution.totalMoves)
        val movesToApply = session.solution.moves.take(clamped)
        val targetCube = session.initialCubeState.apply(movesToApply)
        val (stageNum, stageName) = resolveStageForStep(session.solution.stages, clamped)
        val completed = clamped == session.solution.totalMoves && targetCube.isSolved()
        val summary =
            if (completed) {
                CompletionSummary(
                    method = session.solution.method,
                    totalMoves = session.solution.totalMoves,
                    celebrated = true,
                )
            } else {
                null
            }
        return session.copy(
            currentCubeState = targetCube,
            currentStepNumber = clamped,
            currentStageNumber = stageNum,
            currentStageName = stageName,
            lastAnimatedMove = null,
            isPlaying = false,
            completionSummary = summary,
        )
    }

    fun jumpToStage(
        session: SolveGuideSession,
        stageIndex: Int,
    ): SolveGuideSession {
        if (session.solution.stages.isEmpty()) return session
        val clampedStage = stageIndex.coerceIn(0, session.solution.stages.lastIndex)
        val stepsBefore =
            session.solution.stages
                .take(clampedStage)
                .sumOf { it.steps.size }
        return jumpToStep(session, stepsBefore)
    }

    fun setPlaying(
        session: SolveGuideSession,
        isPlaying: Boolean,
    ): SolveGuideSession = session.copy(isPlaying = isPlaying)

    fun setSpeed(
        session: SolveGuideSession,
        speed: AnimationSpeed,
    ): SolveGuideSession = session.copy(speed = speed)

    fun recoverWhenLost(
        session: SolveGuideSession,
        reenteredCubeState: CubeState,
        scheme: ColorScheme = ColorScheme.STANDARD,
    ): SolveGuideSession =
        startGuide(
            initialState = reenteredCubeState,
            method = session.solution.method,
            defaultSpeed = session.speed,
            scheme = scheme,
        )

    private fun resolveStageForStep(
        stages: List<Stage>,
        stepNumber: Int,
    ): Pair<Int, String> {
        var resolved: Pair<Int, String>? = null
        if (stages.isEmpty()) {
            resolved = 1 to "Solved"
        } else {
            val targetOneBased = stepNumber.coerceAtLeast(1)
            var accumulated = 0
            for ((idx, stage) in stages.withIndex()) {
                accumulated += stage.steps.size
                if (resolved == null && targetOneBased <= accumulated) {
                    resolved = (idx + 1) to stage.name
                }
            }
        }
        return resolved ?: (stages.size to stages.last().name)
    }
}
