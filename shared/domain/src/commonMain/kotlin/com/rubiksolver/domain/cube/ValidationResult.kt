package com.rubiksolver.domain.cube

sealed interface ValidationResult {
    data class Valid(
        val state: CubeState,
    ) : ValidationResult

    data class AlreadySolved(
        val state: CubeState,
    ) : ValidationResult

    data class Invalid(
        val errors: List<ValidationError>,
    ) : ValidationResult
}
