package com.rubiksolver.domain.learning

import com.rubiksolver.domain.cube.Algorithm
import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.solving.Method

enum class LessonPhase {
    CONCEPT,
    DEMONSTRATION,
    GUIDED_PRACTICE,
    FREE_PRACTICE,
    ;

    fun next(): LessonPhase =
        when (this) {
            CONCEPT -> DEMONSTRATION
            DEMONSTRATION -> GUIDED_PRACTICE
            GUIDED_PRACTICE -> FREE_PRACTICE
            FREE_PRACTICE -> FREE_PRACTICE
        }
}

data class Technique(
    val id: String,
    val name: String,
    val size: CubeSize,
    val method: Method,
    val prerequisiteIds: Set<String>,
    val targetAlgorithm: Algorithm,
)

data class Lesson(
    val title: String,
    val summary: String,
    val technique: Technique,
)

data class LessonStatus(
    val lesson: Lesson,
    val isLocked: Boolean,
    val isMastered: Boolean,
)

data class AlgorithmReferenceEntry(
    val techniqueId: String,
    val name: String,
    val method: Method,
    val algorithm: Algorithm,
    val isMastered: Boolean,
)
