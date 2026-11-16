package com.rubiksolver.domain.profile

private const val SLOW_DURATION_MS: Long = 520L
private const val NORMAL_DURATION_MS: Long = 280L
private const val FAST_DURATION_MS: Long = 150L

enum class AnimationSpeed(val durationMillis: Long) {
    SLOW(SLOW_DURATION_MS),
    NORMAL(NORMAL_DURATION_MS),
    FAST(FAST_DURATION_MS),
}
