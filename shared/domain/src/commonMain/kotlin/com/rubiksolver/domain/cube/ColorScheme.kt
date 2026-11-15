package com.rubiksolver.domain.cube

data class ColorScheme(
    val faceColors: Map<Face, StickerColor>,
) {
    init {
        require(faceColors.keys == Face.entries.toSet()) {
            "ColorScheme must assign a color to all 6 faces."
        }
        require(faceColors.values.toSet().size == Face.entries.size) {
            "ColorScheme must assign 6 distinct colors to the 6 faces."
        }
    }

    fun colorOf(face: Face): StickerColor = faceColors.getValue(face)

    fun faceOf(color: StickerColor): Face = faceColors.entries.first { it.value == color }.key

    fun areOppositeColors(
        first: StickerColor,
        second: StickerColor,
    ): Boolean = faceOf(first).opposite == faceOf(second)

    companion object {
        val STANDARD: ColorScheme =
            ColorScheme(
                mapOf(
                    Face.UP to StickerColor.WHITE,
                    Face.DOWN to StickerColor.YELLOW,
                    Face.FRONT to StickerColor.GREEN,
                    Face.BACK to StickerColor.BLUE,
                    Face.LEFT to StickerColor.ORANGE,
                    Face.RIGHT to StickerColor.RED,
                ),
            )
    }
}
