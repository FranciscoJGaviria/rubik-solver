package com.rubiksolver.domain.learning

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Face
import com.rubiksolver.domain.cube.StickerColor
import com.rubiksolver.domain.profile.AdultCheckChallenge
import com.rubiksolver.domain.profile.AppLanguage
import com.rubiksolver.domain.profile.Profile
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.profile.SolveRecord
import com.rubiksolver.domain.recognition.ConstrainedColorClassifier
import com.rubiksolver.domain.recognition.LabColor
import com.rubiksolver.domain.recognition.RgbColor
import com.rubiksolver.domain.recognition.ScanOrientationGuide
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecognitionTutorProfileCommonTest {
    @Test
    fun `AC-1_2_1 and AC-1_2_2 CIELAB conversion orientation guide and constrained classifier work on 2x2 and 3x3`() {
        for (size in CubeSize.entries) {
            val solved = CubeState.solved(size)
            val samples =
                solved.allStickers().mapValues { (_, color) ->
                    RgbColor.canonicalFor(color).toLab()
                }
            val classified = ConstrainedColorClassifier.classify(size, samples)
            classified shouldBe solved
        }

        ScanOrientationGuide.nextStepAfter(Face.FRONT).shouldNotBeNull().nextFace shouldBe Face.RIGHT
        ScanOrientationGuide.nextStepAfter(Face.DOWN).shouldBeNull()
        LabColor(50.0, 10.0, 20.0).distanceTo(LabColor(50.0, 10.0, 20.0)) shouldBe 0.0
    }

    @Test
    fun `AC-5_1 to AC-5_9 curriculum hint policy mastery and graduation rules work on all targets`() {
        val statuses = Curriculum.lessonStatuses(emptySet())
        statuses.first().isLocked shouldBe false

        var progress = HintPolicy.initialProgress()
        repeat(12) {
            progress = HintPolicy.recordOutcome(progress, succeeded = true)
        }
        progress.currentLevel shouldBe HintLevel.NONE
        val requestedHelp = HintPolicy.requestMoreHelp(progress)
        requestedHelp.currentLevel shouldBe HintLevel.STAGE_NAME
        val failedOnce = HintPolicy.recordOutcome(requestedHelp, succeeded = false)
        failedOnce.consecutiveSuccesses shouldBe 0

        var mastery = MasteryState()
        mastery = MasteryTracker.recordPractice(mastery, HintLevel.FULL_ANIMATION, succeeded = true)
        mastery.isMastered shouldBe false
        mastery = MasteryTracker.recordPractice(mastery, HintLevel.NONE, succeeded = false)
        mastery.consecutiveSuccessesAtNone shouldBe 0
        repeat(5) {
            mastery = MasteryTracker.recordPractice(mastery, HintLevel.NONE, succeeded = true)
        }
        mastery.isMastered shouldBe true

        val beginnerIds = Curriculum.techniqueIdsForMethod(Method.BEGINNER, CubeSize.THREE)
        val grad =
            GraduationPolicy.evaluate(
                method = Method.BEGINNER,
                size = CubeSize.THREE,
                masteredTechniqueIds = beginnerIds,
                usedHints = false,
                finalCubeState = CubeState.solved(CubeSize.THREE),
            )
        grad.graduated shouldBe true
    }

    @Test
    fun `AC-6_1 to AC-6_4 and AC-7_1 profile challenge and language models work on all targets`() {
        val profile = Profile(id = "p1", name = "Sofi", avatar = "star", kind = ProfileKind.KID)
        profile.kind shouldBe ProfileKind.KID

        val record = SolveRecord(profileId = "p1", method = Method.BEGINNER, moveCount = 30, elapsedMillis = 15_000L)
        record.moveCount shouldBe 30

        val challenge = AdultCheckChallenge(6, 7)
        challenge.prompt shouldBe "6 + 7 = ?"
        challenge.verify(13) shouldBe true
        challenge.verify(12) shouldBe false

        AppLanguage.fromDeviceLocaleTag("es-MX") shouldBe AppLanguage.SPANISH
        AppLanguage.fromDeviceLocaleTag("en-US") shouldBe AppLanguage.ENGLISH
        StickerColor.entries.size shouldBe 6
    }
}
