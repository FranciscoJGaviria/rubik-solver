package com.rubiksolver.application.learning

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.learning.Curriculum
import com.rubiksolver.domain.learning.HintLevel
import com.rubiksolver.domain.learning.HintPolicy
import com.rubiksolver.domain.learning.LessonPhase
import com.rubiksolver.domain.learning.MasteryState
import com.rubiksolver.domain.learning.MasteryTracker
import com.rubiksolver.domain.learning.PracticeScrambleGenerator
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.Method
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

class TutorAcceptanceSpec :
    BehaviorSpec({
        val tutorUseCase = ManageTutorSessionUseCase()

        Given("the user has not mastered notation (AC-5.1)") {
            val mastered = emptySet<String>()

            When("the user opens the curriculum") {
                val statuses = Curriculum.lessonStatuses(mastered)

                Then("the Beginner lessons are shown as locked while Notation is unlocked") {
                    val notationLesson = statuses.first { it.lesson.technique.id == Curriculum.NOTATION_TECHNIQUE_ID }
                    val whiteCrossLesson =
                        statuses.first {
                            it.lesson.technique.id == Curriculum.WHITE_CROSS_TECHNIQUE_ID
                        }
                    notationLesson.isLocked shouldBe false
                    whiteCrossLesson.isLocked shouldBe true
                }
            }
        }

        Given("a user starts a lesson in the tutor (AC-5.2)") {
            val lesson = Curriculum.allLessons.first()

            When("the user advances through all phases of the lesson") {
                val visitedPhases = mutableListOf<LessonPhase>()
                var session = tutorUseCase.startLesson(lesson, ProfileKind.ADULT)
                visitedPhases.add(session.currentPhase)
                repeat(3) {
                    session = tutorUseCase.advancePhase(session)
                    visitedPhases.add(session.currentPhase)
                }

                Then("the user goes through concept, demonstration, guided practice and free practice in that order") {
                    visitedPhases.shouldContainExactly(
                        LessonPhase.CONCEPT,
                        LessonPhase.DEMONSTRATION,
                        LessonPhase.GUIDED_PRACTICE,
                        LessonPhase.FREE_PRACTICE,
                    )
                }
            }
        }

        Given("the lesson Insert a white corner (AC-5.3)") {
            val lesson = Curriculum.findLessonById(Curriculum.WHITE_CORNER_TECHNIQUE_ID)

            When("a practice starts") {
                val practiceState = PracticeScrambleGenerator.generateFor(lesson.technique)

                Then("only that technique is needed to complete the practice") {
                    practiceState.isSolved() shouldBe false
                    val afterTechnique = practiceState.apply(lesson.technique.targetAlgorithm)
                    afterTechnique.isSolved() shouldBe true
                }
            }
        }

        Given("the user succeeded 3 times at hint level full animation (AC-5.4)") {
            var hintProgress = HintPolicy.initialProgress()
            repeat(3) {
                hintProgress = HintPolicy.recordOutcome(hintProgress, succeeded = true)
            }

            When("the next practice starts") {
                val currentHintLevel = hintProgress.currentLevel

                Then("the hint level is next move") {
                    currentHintLevel shouldBe HintLevel.NEXT_MOVE
                }
            }
        }

        Given("4 consecutive successes at hint level none (AC-5.5)") {
            var masteryState = MasteryState(consecutiveSuccessesAtNone = 4, isMastered = false)

            When("the user succeeds once more at hint level none") {
                masteryState = MasteryTracker.recordPractice(masteryState, HintLevel.NONE, succeeded = true)

                Then("the technique is marked as mastered") {
                    masteryState.isMastered shouldBe true
                }
            }
        }

        Given("all Beginner 3x3 techniques are mastered (AC-5.6)") {
            val beginnerIds = Curriculum.techniqueIdsForMethod(Method.BEGINNER, CubeSize.THREE)

            When("the user solves a random scramble without hints and confirms it is solved") {
                val outcome =
                    tutorUseCase.evaluateGraduation(
                        method = Method.BEGINNER,
                        size = CubeSize.THREE,
                        masteredTechniqueIds = beginnerIds,
                        usedHints = false,
                        finalCubeState = CubeState.solved(CubeSize.THREE),
                    )

                Then("the user graduates the Beginner 3x3 method and earns its badge") {
                    outcome.graduated shouldBe true
                    outcome.badgeName shouldBe "Beginner 3x3 Master"
                }
            }
        }

        Given("a Kid profile starting a tutor lesson (AC-5.7)") {
            val lesson = Curriculum.allLessons.first()

            When("a lesson starts and the kid succeeds a practice") {
                val session = tutorUseCase.startLesson(lesson, ProfileKind.KID)
                val afterSuccess = tutorUseCase.recordPracticeAttempt(session, succeeded = true)

                Then("no timer is shown and every success is encouraged") {
                    session.showTimer shouldBe false
                    afterSuccess.encouragementMessage.shouldNotBeNull().shouldNotBeBlank()
                }
            }
        }

        Given("the user mastered Notation and White cross (AC-5.8)") {
            val mastered = setOf(Curriculum.NOTATION_TECHNIQUE_ID, Curriculum.WHITE_CROSS_TECHNIQUE_ID)

            When("the tutor recommends the next lesson") {
                val nextLesson = Curriculum.recommendNextLesson(mastered).shouldNotBeNull()

                Then("the recommended lesson is Insert a white corner") {
                    nextLesson.technique.id shouldBe Curriculum.WHITE_CORNER_TECHNIQUE_ID
                    nextLesson.title shouldBe "Insert a white corner"
                }
            }
        }

        Given("the user opens the CFOP algorithm reference (AC-5.9)") {
            val mastered = setOf(Curriculum.CFOP_SUNE_TECHNIQUE_ID)

            When("the CFOP algorithm reference is queried") {
                val entries = Curriculum.algorithmReference(Method.CFOP, mastered)

                Then("each algorithm can be played in 3D and shows its mastery status") {
                    entries.shouldNotBeEmpty()
                    val sune = entries.first { it.techniqueId == Curriculum.CFOP_SUNE_TECHNIQUE_ID }
                    sune.algorithm.moves.shouldNotBeEmpty()
                    sune.isMastered shouldBe true
                }
            }
        }
    })
