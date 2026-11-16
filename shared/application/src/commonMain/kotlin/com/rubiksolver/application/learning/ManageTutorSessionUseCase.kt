package com.rubiksolver.application.learning

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.learning.GraduationOutcome
import com.rubiksolver.domain.learning.GraduationPolicy
import com.rubiksolver.domain.learning.HintPolicy
import com.rubiksolver.domain.learning.HintProgress
import com.rubiksolver.domain.learning.Lesson
import com.rubiksolver.domain.learning.LessonPhase
import com.rubiksolver.domain.learning.MasteryState
import com.rubiksolver.domain.learning.MasteryTracker
import com.rubiksolver.domain.learning.PracticeScrambleGenerator
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.Method

data class TutorLessonSession(
    val lesson: Lesson,
    val profileKind: ProfileKind,
    val currentPhase: LessonPhase = LessonPhase.CONCEPT,
    val practiceCubeState: CubeState,
    val hintProgress: HintProgress = HintPolicy.initialProgress(),
    val masteryState: MasteryState = MasteryState(),
    val showTimer: Boolean,
    val encouragementMessage: String? = null,
)

class ManageTutorSessionUseCase {
    fun startLesson(
        lesson: Lesson,
        profileKind: ProfileKind,
    ): TutorLessonSession =
        TutorLessonSession(
            lesson = lesson,
            profileKind = profileKind,
            currentPhase = LessonPhase.CONCEPT,
            practiceCubeState = PracticeScrambleGenerator.generateFor(lesson.technique),
            showTimer = profileKind != ProfileKind.KID,
            encouragementMessage = null,
        )

    fun advancePhase(session: TutorLessonSession): TutorLessonSession =
        session.copy(currentPhase = session.currentPhase.next())

    fun recordPracticeAttempt(
        session: TutorLessonSession,
        succeeded: Boolean,
    ): TutorLessonSession {
        val currentHintLevel = session.hintProgress.currentLevel
        val nextHint = HintPolicy.recordOutcome(session.hintProgress, succeeded)
        val nextMastery = MasteryTracker.recordPractice(session.masteryState, currentHintLevel, succeeded)
        val encouragement =
            if (succeeded && session.profileKind == ProfileKind.KID) {
                "Awesome job! You nailed ${session.lesson.title}!"
            } else if (succeeded) {
                "Practice completed."
            } else {
                null
            }
        return session.copy(
            hintProgress = nextHint,
            masteryState = nextMastery,
            practiceCubeState = PracticeScrambleGenerator.generateFor(session.lesson.technique),
            encouragementMessage = encouragement,
        )
    }

    fun evaluateGraduation(
        method: Method,
        size: CubeSize,
        masteredTechniqueIds: Set<String>,
        usedHints: Boolean,
        finalCubeState: CubeState,
    ): GraduationOutcome =
        GraduationPolicy.evaluate(
            method = method,
            size = size,
            masteredTechniqueIds = masteredTechniqueIds,
            usedHints = usedHints,
            finalCubeState = finalCubeState,
        )
}
