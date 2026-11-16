package com.rubiksolver.domain.learning

import com.rubiksolver.domain.cube.Algorithm
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.solving.Method

object Curriculum {
    const val NOTATION_TECHNIQUE_ID = "notation-basics"
    const val WHITE_CROSS_TECHNIQUE_ID = "beginner-3x3-white-cross"
    const val WHITE_CORNER_TECHNIQUE_ID = "beginner-3x3-white-corner"
    const val MIDDLE_EDGE_TECHNIQUE_ID = "beginner-3x3-middle-edge"
    const val BEGINNER_2X2_FIRST_LAYER_ID = "beginner-2x2-first-layer"
    const val ORTEGA_PBL_TECHNIQUE_ID = "ortega-2x2-pbl"
    const val CFOP_SUNE_TECHNIQUE_ID = "cfop-3x3-oll-sune"
    const val ROUX_CMLL_TECHNIQUE_ID = "roux-3x3-cmll"

    val allLessons: List<Lesson> =
        listOf(
            Lesson(
                title = "Cube Notation Basics",
                summary = "Understand U, D, L, R, F, B and prime/double turns.",
                technique =
                    Technique(
                        id = NOTATION_TECHNIQUE_ID,
                        name = "Singmaster Notation",
                        size = CubeSize.THREE,
                        method = Method.BEGINNER,
                        prerequisiteIds = emptySet(),
                        targetAlgorithm = Algorithm.parse("R U R' U'", name = "Sexy Move"),
                    ),
            ),
            Lesson(
                title = "White cross",
                summary = "Align the four white edges with their side center colors.",
                technique =
                    Technique(
                        id = WHITE_CROSS_TECHNIQUE_ID,
                        name = "White cross",
                        size = CubeSize.THREE,
                        method = Method.BEGINNER,
                        prerequisiteIds = setOf(NOTATION_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("F R U R' U' F'", name = "Cross Edge Setup"),
                    ),
            ),
            Lesson(
                title = "Insert a white corner",
                summary = "Place a first-layer white corner using the right-hand trigger.",
                technique =
                    Technique(
                        id = WHITE_CORNER_TECHNIQUE_ID,
                        name = "Insert a white corner",
                        size = CubeSize.THREE,
                        method = Method.BEGINNER,
                        prerequisiteIds = setOf(WHITE_CROSS_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("R U R' U'", name = "Corner Trigger"),
                    ),
            ),
            Lesson(
                title = "Insert a middle-layer edge",
                summary = "Move an edge from the top layer into the middle slot.",
                technique =
                    Technique(
                        id = MIDDLE_EDGE_TECHNIQUE_ID,
                        name = "Middle-layer edge insertion",
                        size = CubeSize.THREE,
                        method = Method.BEGINNER,
                        prerequisiteIds = setOf(WHITE_CORNER_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("U R U' R' U' F' U F", name = "Right Edge Insert"),
                    ),
            ),
            Lesson(
                title = "2x2 First Layer Corners",
                summary = "Build the first layer of a 2x2 cube matching side colors.",
                technique =
                    Technique(
                        id = BEGINNER_2X2_FIRST_LAYER_ID,
                        name = "2x2 First Layer",
                        size = CubeSize.TWO,
                        method = Method.BEGINNER,
                        prerequisiteIds = setOf(NOTATION_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("R U R' U'", name = "2x2 Corner Insert"),
                    ),
            ),
            Lesson(
                title = "Ortega PBL Adjacent Swap",
                summary = "Permute both layers of a 2x2 cube at once.",
                technique =
                    Technique(
                        id = ORTEGA_PBL_TECHNIQUE_ID,
                        name = "Ortega PBL",
                        size = CubeSize.TWO,
                        method = Method.ORTEGA,
                        prerequisiteIds = setOf(BEGINNER_2X2_FIRST_LAYER_ID),
                        targetAlgorithm = Algorithm.parse("R2 U' B2 U2 R2 U' R2", name = "Ortega PBL Bar"),
                    ),
            ),
            Lesson(
                title = "CFOP 2-Look OLL — Sune",
                summary = "Orient three top-layer corners with the Sune trigger.",
                technique =
                    Technique(
                        id = CFOP_SUNE_TECHNIQUE_ID,
                        name = "Sune (OLL 27)",
                        size = CubeSize.THREE,
                        method = Method.CFOP,
                        prerequisiteIds = setOf(MIDDLE_EDGE_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("R U R' U R U2 R'", name = "Sune"),
                    ),
            ),
            Lesson(
                title = "Roux CMLL — Sune Corner Orientation",
                summary = "Orient the four top corners while preserving the left and right 1x2x3 blocks.",
                technique =
                    Technique(
                        id = ROUX_CMLL_TECHNIQUE_ID,
                        name = "CMLL Sune",
                        size = CubeSize.THREE,
                        method = Method.ROUX,
                        prerequisiteIds = setOf(CFOP_SUNE_TECHNIQUE_ID),
                        targetAlgorithm = Algorithm.parse("R U R' U R U2 R'", name = "CMLL Sune"),
                    ),
            ),
        )

    fun findLessonById(techniqueId: String): Lesson = allLessons.first { it.technique.id == techniqueId }

    fun lessonStatuses(masteredTechniqueIds: Set<String>): List<LessonStatus> =
        allLessons.map { lesson ->
            val unlocked = masteredTechniqueIds.containsAll(lesson.technique.prerequisiteIds)
            LessonStatus(
                lesson = lesson,
                isLocked = !unlocked,
                isMastered = lesson.technique.id in masteredTechniqueIds,
            )
        }

    fun recommendNextLesson(masteredTechniqueIds: Set<String>): Lesson? =
        lessonStatuses(masteredTechniqueIds)
            .firstOrNull { !it.isLocked && !it.isMastered }
            ?.lesson

    fun techniqueIdsForMethod(
        method: Method,
        size: CubeSize,
    ): Set<String> =
        allLessons
            .filter { it.technique.method == method && it.technique.size == size }
            .mapTo(linkedSetOf()) { it.technique.id }

    fun algorithmReference(
        method: Method,
        masteredTechniqueIds: Set<String>,
    ): List<AlgorithmReferenceEntry> =
        allLessons
            .filter { it.technique.method == method }
            .map { lesson ->
                AlgorithmReferenceEntry(
                    techniqueId = lesson.technique.id,
                    name = lesson.technique.name,
                    method = lesson.technique.method,
                    algorithm = lesson.technique.targetAlgorithm,
                    isMastered = lesson.technique.id in masteredTechniqueIds,
                )
            }
}
