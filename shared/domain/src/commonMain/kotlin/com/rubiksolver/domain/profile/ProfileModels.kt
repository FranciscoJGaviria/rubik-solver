package com.rubiksolver.domain.profile

import com.rubiksolver.domain.solving.Method

data class Profile(
    val id: String,
    val name: String,
    val avatar: String,
    val kind: ProfileKind,
)

data class SolveRecord(
    val profileId: String,
    val method: Method,
    val moveCount: Int,
    val elapsedMillis: Long,
)

data class AdultCheckChallenge(
    val leftOperand: Int,
    val rightOperand: Int,
) {
    val prompt: String
        get() = "$leftOperand + $rightOperand = ?"

    fun verify(answer: Int): Boolean = answer == leftOperand + rightOperand
}

enum class AppLanguage(val code: String) {
    SPANISH("es"),
    ENGLISH("en"),
    ;

    companion object {
        fun fromDeviceLocaleTag(localeTag: String): AppLanguage {
            val normalized = localeTag.trim().lowercase()
            return if (normalized.startsWith("es")) {
                SPANISH
            } else {
                ENGLISH
            }
        }
    }
}
