package com.rubiksolver.application.profile

import com.rubiksolver.domain.profile.AdultCheckChallenge
import com.rubiksolver.domain.profile.Profile
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.profile.SolveRecord
import com.rubiksolver.domain.solving.Method

interface ProfileRepository {
    fun saveProfile(profile: Profile)

    fun listProfiles(): List<Profile>

    fun removeProfile(profileId: String): Boolean

    fun recordMasteredTechnique(
        profileId: String,
        techniqueId: String,
    )

    fun getMasteredTechniques(profileId: String): Set<String>

    fun saveSolveRecord(record: SolveRecord)

    fun getSolveHistory(profileId: String): List<SolveRecord>
}

class ManageProfilesUseCase(
    private val repository: ProfileRepository,
) {
    private var nextIdCounter = 1

    fun createProfile(
        name: String,
        avatar: String,
        kind: ProfileKind,
    ): Profile {
        val profile =
            Profile(
                id = "profile-${nextIdCounter++}-${name.lowercase()}",
                name = name,
                avatar = avatar,
                kind = kind,
            )
        repository.saveProfile(profile)
        return profile
    }

    fun listProfiles(): List<Profile> = repository.listProfiles()

    fun recordMasteredTechnique(
        profileId: String,
        techniqueId: String,
    ) {
        repository.recordMasteredTechnique(profileId, techniqueId)
    }

    fun masteredTechniques(profileId: String): Set<String> = repository.getMasteredTechniques(profileId)

    fun recordSolve(
        profileId: String,
        method: Method,
        moveCount: Int,
        elapsedMillis: Long,
    ): SolveRecord {
        val record =
            SolveRecord(
                profileId = profileId,
                method = method,
                moveCount = moveCount,
                elapsedMillis = elapsedMillis,
            )
        repository.saveSolveRecord(record)
        return record
    }

    fun solveHistory(profileId: String): List<SolveRecord> = repository.getSolveHistory(profileId)

    fun deleteProfile(
        activeProfile: Profile,
        targetProfileId: String,
        challenge: AdultCheckChallenge? = null,
        providedAnswer: Int? = null,
    ): Boolean {
        val allowed =
            if (activeProfile.kind == ProfileKind.KID) {
                challenge != null && providedAnswer != null && challenge.verify(providedAnswer)
            } else {
                true
            }
        return if (allowed) {
            repository.removeProfile(targetProfileId)
        } else {
            false
        }
    }
}
