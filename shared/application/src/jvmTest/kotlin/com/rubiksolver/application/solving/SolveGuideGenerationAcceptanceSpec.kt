package com.rubiksolver.application.solving

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import com.rubiksolver.domain.cube.Scramble
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.DefaultSolverFactory
import com.rubiksolver.domain.solving.ExplanationKey
import com.rubiksolver.domain.solving.Method
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SolveGuideGenerationAcceptanceSpec :
    BehaviorSpec({
        val solver = DefaultSolverFactory.create()
        val getAvailableMethods = GetAvailableMethodsUseCase(solver)
        val generateSolution = GenerateSolutionUseCase(solver)

        Given("a valid 2x2 and 3x3 cube state (AC-3.1)") {
            When("the user opens the method choice for 2x2 and 3x3") {
                val methods2x2 = getAvailableMethods(CubeSize.TWO, ProfileKind.ADULT, lastUsedMethod = Method.ORTEGA)
                val methods3x3 = getAvailableMethods(CubeSize.THREE, ProfileKind.KID, lastUsedMethod = Method.CFOP)

                Then("2x2 offers Beginner, Ortega and Optimal, and Kid profiles default to Beginner") {
                    methods2x2.available.shouldContainExactly(
                        Method.BEGINNER,
                        Method.ORTEGA,
                        Method.OPTIMAL,
                    )
                    methods2x2.defaultMethod shouldBe Method.ORTEGA

                    methods3x3.available.shouldContainExactly(
                        Method.BEGINNER,
                        Method.CFOP,
                        Method.ROUX,
                        Method.FAST,
                    )
                    methods3x3.defaultMethod shouldBe Method.BEGINNER
                }
            }
        }

        Given("a guide with method Beginner versus Fast or Optimal (AC-3.2.1)") {
            val scrambled3x3 = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("R U R' U' F D"))
            val scrambled2x2 = CubeState.solved(CubeSize.TWO).apply(Scramble.parse("R U R' F2"))

            When("solutions are generated for Beginner, Fast and Optimal") {
                val beginnerSolution = generateSolution(scrambled3x3, Method.BEGINNER)
                val fastSolution = generateSolution(scrambled3x3, Method.FAST)
                val optimal2x2Solution = generateSolution(scrambled2x2, Method.OPTIMAL)

                Then("Beginner steps include notation and explanations while Fast and Optimal show moves only") {
                    beginnerSolution.steps.shouldNotBeEmpty()
                    beginnerSolution.steps.forEach { step ->
                        step.move.notation.isNotBlank() shouldBe true
                        step.explanation.shouldNotBeNull()
                    }

                    fastSolution.steps.shouldNotBeEmpty()
                    fastSolution.steps.forEach { step ->
                        step.explanationKey shouldBe ExplanationKey.MOVE_ONLY
                        step.explanation.shouldBeNull()
                    }

                    optimal2x2Solution.steps.shouldNotBeEmpty()
                    optimal2x2Solution.steps.forEach { step ->
                        step.explanationKey shouldBe ExplanationKey.MOVE_ONLY
                        step.explanation.shouldBeNull()
                    }
                }
            }
        }

        Given("any valid cube state and any available method (AC-3.2.2)") {
            val state2x2 = CubeState.solved(CubeSize.TWO).apply(Scramble.parse("R U2 F' R U'"))
            val state3x3 = CubeState.solved(CubeSize.THREE).apply(Scramble.parse("F R U' R' F' L D2"))

            When("all steps of the solution are applied for every method") {
                Then("the cube is solved in every case") {
                    for (method in getAvailableMethods(CubeSize.TWO, ProfileKind.ADULT).available) {
                        val solution = generateSolution(state2x2, method)
                        state2x2.apply(solution.moves).isSolved() shouldBe true
                    }
                    for (method in getAvailableMethods(CubeSize.THREE, ProfileKind.ADULT).available) {
                        val solution = generateSolution(state3x3, method)
                        state3x3.apply(solution.moves).isSolved() shouldBe true
                    }
                }
            }
        }
    })
