package com.rubiksolver.domain.cube

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

class CubeSizeBehaviorSpec :
    BehaviorSpec({
        Given("the user chooses a supported cube size (AC-1.1)") {
            When("the chosen size is 2x2") {
                val size = CubeSize.TWO
                Then("each face has 4 stickers and the cube has 24 stickers") {
                    size.stickersPerFace shouldBe 4
                    size.totalStickers shouldBe 24
                }
            }
            When("the chosen size is 3x3") {
                val size = CubeSize.THREE
                Then("each face has 9 stickers and the cube has 54 stickers") {
                    size.stickersPerFace shouldBe 9
                    size.totalStickers shouldBe 54
                }
            }
            Then("every CubeSize satisfies totalStickers = 6 * dimension^2") {
                checkAll(Arb.enum<CubeSize>()) { size ->
                    size.stickersPerFace shouldBe size.dimension * size.dimension
                    size.totalStickers shouldBe CubeSize.FACES_COUNT * size.stickersPerFace
                }
            }
        }
    })
