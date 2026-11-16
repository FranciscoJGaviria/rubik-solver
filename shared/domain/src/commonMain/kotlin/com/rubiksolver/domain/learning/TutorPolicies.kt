package com.rubiksolver.domain.learning

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.solving.Method

object PracticeScrambleGenerator {
    /**
     * Generates a cube state where applying [technique.targetAlgorithm] once completes the practice (FR-5.3, AC-5.3).
     */
    fun generateFor(technique: Technique): CubeState =
        CubeState
            .solved(technique.size)
            .apply(technique.targetAlgorithm.inverse())
}

data class HintProgress(
    val currentLevel: HintLevel = HintLevel.FULL_ANIMATION,
    val consecutiveSuccesses: Int = 0,
)

object HintPolicy {
    private const val PROMOTION_STREAK = 3

    fun initialProgress(): HintProgress = HintProgress()

    fun recordOutcome(
        progress: HintProgress,
        succeeded: Boolean,
    ): HintProgress {
        if (!succeeded) {
            return progress.copy(consecutiveSuccesses = 0)
        }
        val nextStreak = progress.consecutiveSuccesses + 1
        return if (nextStreak >= PROMOTION_STREAK) {
            HintProgress(
                currentLevel = fadeLevel(progress.currentLevel),
                consecutiveSuccesses = 0,
            )
        } else {
            progress.copy(consecutiveSuccesses = nextStreak)
        }
    }

    fun requestMoreHelp(progress: HintProgress): HintProgress =
        HintProgress(
            currentLevel = increaseHelpLevel(progress.currentLevel),
            consecutiveSuccesses = 0,
        )

    private fun fadeLevel(level: HintLevel): HintLevel =
        when (level) {
            HintLevel.FULL_ANIMATION -> HintLevel.NEXT_MOVE
            HintLevel.NEXT_MOVE -> HintLevel.NOTATION_ONLY
            HintLevel.NOTATION_ONLY -> HintLevel.STAGE_NAME
            HintLevel.STAGE_NAME -> HintLevel.NONE
            HintLevel.NONE -> HintLevel.NONE
        }

    private fun increaseHelpLevel(level: HintLevel): HintLevel =
        when (level) {
            HintLevel.NONE -> HintLevel.STAGE_NAME
            HintLevel.STAGE_NAME -> HintLevel.NOTATION_ONLY
            HintLevel.NOTATION_ONLY -> HintLevel.NEXT_MOVE
            HintLevel.NEXT_MOVE -> HintLevel.FULL_ANIMATION
            HintLevel.FULL_ANIMATION -> HintLevel.FULL_ANIMATION
        }
}

data class MasteryState(
    val consecutiveSuccessesAtNone: Int = 0,
    val isMastered: Boolean = false,
)

object MasteryTracker {
    private const val MASTERY_STREAK_AT_NONE = 5

    fun recordPractice(
        state: MasteryState,
        hintLevel: HintLevel,
        succeeded: Boolean,
    ): MasteryState {
        if (!succeeded || hintLevel != HintLevel.NONE) {
            val resetStreak = if (succeeded) state.consecutiveSuccessesAtNone else 0
            return state.copy(consecutiveSuccessesAtNone = resetStreak)
        }
        val nextStreak = state.consecutiveSuccessesAtNone + 1
        return MasteryState(
            consecutiveSuccessesAtNone = nextStreak,
            isMastered = state.isMastered || nextStreak >= MASTERY_STREAK_AT_NONE,
        )
    }
}

data class GraduationOutcome(
    val graduated: Boolean,
    val badgeName: String?,
)

object GraduationPolicy {
    fun evaluate(
        method: Method,
        size: CubeSize,
        masteredTechniqueIds: Set<String>,
        usedHints: Boolean,
        finalCubeState: CubeState,
    ): GraduationOutcome {
        val requiredTechniques = Curriculum.techniqueIdsForMethod(method, size)
        val allMastered = requiredTechniques.isNotEmpty() && masteredTechniqueIds.containsAll(requiredTechniques)
        val eligible = allMastered && !usedHints && finalCubeState.isSolved()
        return if (eligible) {
            val methodTitle = method.name.lowercase().replaceFirstChar(Char::uppercase)
            GraduationOutcome(
                graduated = true,
                badgeName = "$methodTitle ${size.dimension}x${size.dimension} Master",
            )
        } else {
            GraduationOutcome(graduated = false, badgeName = null)
        }
    }
}
