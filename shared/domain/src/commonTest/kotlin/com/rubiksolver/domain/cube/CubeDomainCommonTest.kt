package com.rubiksolver.domain.cube

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class CubeDomainCommonTest {
    @Test
    fun `AC-1_5 scramble and algorithm parsing notation and inverse round-trip on 2x2 and 3x3`() {
        val scramble = Scramble.parse("R U2 F' L D B2")
        scramble.notation shouldBe "R U2 F' L D B2"
        Scramble.parse("   ").moves shouldBe emptyList()

        val algorithm = Algorithm.parse("R U R' U'", name = "Sexy Move")
        algorithm.notation shouldBe "R U R' U'"
        algorithm.inverse().name shouldBe "Sexy Move (inverse)"
        algorithm.moves.first().toString() shouldBe "R"

        for (size in CubeSize.entries) {
            val solved = CubeState.solved(size)
            val mixed = solved.apply(scramble)
            mixed.isSolved() shouldBe false
            mixed.apply(scramble.inverse()) shouldBe solved
        }
    }

    @Test
    fun `AC-1_6 custom color scheme and precondition checks`() {
        val custom =
            ColorScheme(
                mapOf(
                    Face.UP to StickerColor.WHITE,
                    Face.DOWN to StickerColor.YELLOW,
                    Face.FRONT to StickerColor.BLUE,
                    Face.BACK to StickerColor.GREEN,
                    Face.LEFT to StickerColor.RED,
                    Face.RIGHT to StickerColor.ORANGE,
                ),
            )
        custom.areOppositeColors(StickerColor.BLUE, StickerColor.GREEN) shouldBe true
        custom.areOppositeColors(StickerColor.WHITE, StickerColor.BLUE) shouldBe false

        shouldThrow<IllegalArgumentException> {
            Face.fromSymbol('X')
        }
        shouldThrow<IllegalArgumentException> {
            Move.parse("R3")
        }
        shouldThrow<IllegalArgumentException> {
            Move.parse("RUX")
        }
        shouldThrow<IllegalArgumentException> {
            StickerPosition(Face.UP, -1, 0)
        }
        shouldThrow<IllegalArgumentException> {
            StickerPosition(Face.UP, 3, 0).validateFor(CubeSize.THREE)
        }
        shouldThrow<IllegalArgumentException> {
            ColorScheme(mapOf(Face.UP to StickerColor.WHITE))
        }
        shouldThrow<IllegalArgumentException> {
            ColorScheme(Face.entries.associateWith { StickerColor.WHITE })
        }
        shouldThrow<IllegalArgumentException> {
            CubeState(CubeSize.TWO, emptyMap())
        }
    }

    @Test
    fun `AC-2_1 to AC-2_3 validator detects count center and impossible piece errors`() {
        val solved3x3 = CubeState.solved(CubeSize.THREE)

        val wrongCountState = solved3x3.withSticker(StickerPosition(Face.FRONT, 0, 0), StickerColor.WHITE)
        val countInvalid = CubeValidator.validate(wrongCountState).shouldBeInstanceOf<ValidationResult.Invalid>()
        countInvalid.errors.filterIsInstance<ValidationError.WrongColorCount>().shouldNotBeEmpty()

        val dupCenterState =
            solved3x3
                .withSticker(StickerPosition(Face.BACK, 1, 1), StickerColor.GREEN)
                .withSticker(StickerPosition(Face.FRONT, 0, 0), StickerColor.BLUE)
        val centerInvalid = CubeValidator.validate(dupCenterState).shouldBeInstanceOf<ValidationResult.Invalid>()
        centerInvalid.errors.filterIsInstance<ValidationError.InvalidCenters>().shouldNotBeEmpty()

        val nonOppositeCentersState =
            solved3x3
                .withSticker(StickerPosition(Face.FRONT, 1, 1), StickerColor.RED)
                .withSticker(StickerPosition(Face.RIGHT, 1, 1), StickerColor.GREEN)
        val nonOppInvalid =
            CubeValidator.validate(nonOppositeCentersState).shouldBeInstanceOf<ValidationResult.Invalid>()
        nonOppInvalid.errors.filterIsInstance<ValidationError.InvalidCenters>().shouldNotBeEmpty()

        val impossiblePieceState =
            solved3x3
                .withSticker(StickerPosition(Face.FRONT, 0, 0), StickerColor.YELLOW)
                .withSticker(StickerPosition(Face.DOWN, 0, 0), StickerColor.GREEN)
                .withSticker(StickerPosition(Face.FRONT, 0, 1), StickerColor.YELLOW)
                .withSticker(StickerPosition(Face.DOWN, 0, 1), StickerColor.GREEN)
        val pieceInvalid = CubeValidator.validate(impossiblePieceState).shouldBeInstanceOf<ValidationResult.Invalid>()
        pieceInvalid.errors.filterIsInstance<ValidationError.ImpossiblePiece>().shouldNotBeEmpty()
    }

    @Test
    fun `AC-2_4 to AC-2_6 validator detects orientation parity and solved states`() {
        val solved2x2 = CubeState.solved(CubeSize.TWO)
        CubeValidator.validate(solved2x2).shouldBeInstanceOf<ValidationResult.AlreadySolved>()

        val solved3x3 = CubeState.solved(CubeSize.THREE)
        CubeValidator.validate(solved3x3).shouldBeInstanceOf<ValidationResult.AlreadySolved>()

        val urf2 = CornerSlot.URF.positions(CubeSize.TWO)
        val twisted2x2 =
            solved2x2
                .withSticker(urf2[0], solved2x2.stickerAt(urf2[1]))
                .withSticker(urf2[1], solved2x2.stickerAt(urf2[2]))
                .withSticker(urf2[2], solved2x2.stickerAt(urf2[0]))
        val twistedInvalid = CubeValidator.validate(twisted2x2).shouldBeInstanceOf<ValidationResult.Invalid>()
        twistedInvalid.errors.filterIsInstance<ValidationError.TwistedCorner>().shouldNotBeEmpty()

        val uf = EdgeSlot.UF.positions()
        val ur = EdgeSlot.UR.positions()
        val flippedEdge3x3 =
            solved3x3
                .withSticker(uf[0], solved3x3.stickerAt(uf[1]))
                .withSticker(uf[1], solved3x3.stickerAt(uf[0]))
        val flippedInvalid = CubeValidator.validate(flippedEdge3x3).shouldBeInstanceOf<ValidationResult.Invalid>()
        flippedInvalid.errors.filterIsInstance<ValidationError.FlippedEdge>().shouldNotBeEmpty()

        val swappedEdges3x3 =
            solved3x3
                .withSticker(uf[0], solved3x3.stickerAt(ur[0]))
                .withSticker(uf[1], solved3x3.stickerAt(ur[1]))
                .withSticker(ur[0], solved3x3.stickerAt(uf[0]))
                .withSticker(ur[1], solved3x3.stickerAt(uf[1]))
        val parityInvalid = CubeValidator.validate(swappedEdges3x3).shouldBeInstanceOf<ValidationResult.Invalid>()
        parityInvalid.errors.filterIsInstance<ValidationError.PermutationParityMismatch>().shouldNotBeEmpty()
    }
}
