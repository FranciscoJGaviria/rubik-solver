package com.rubiksolver.infrastructure.storage

import com.rubiksolver.application.profile.ProfileRepository
import com.rubiksolver.domain.profile.Profile
import com.rubiksolver.domain.profile.SolveRecord

class InMemoryProfileRepositoryAdapter : ProfileRepository {
    private val profiles = linkedMapOf<String, Profile>()
    private val masteryByProfile = mutableMapOf<String, MutableSet<String>>()
    private val historyByProfile = mutableMapOf<String, MutableList<SolveRecord>>()

    override fun saveProfile(profile: Profile) {
        profiles[profile.id] = profile
    }

    override fun listProfiles(): List<Profile> = profiles.values.toList()

    override fun removeProfile(profileId: String): Boolean {
        masteryByProfile.remove(profileId)
        historyByProfile.remove(profileId)
        return profiles.remove(profileId) != null
    }

    override fun recordMasteredTechnique(
        profileId: String,
        techniqueId: String,
    ) {
        val set = masteryByProfile.getOrPut(profileId) { linkedSetOf() }
        set.add(techniqueId)
    }

    override fun getMasteredTechniques(profileId: String): Set<String> = masteryByProfile[profileId]?.toSet().orEmpty()

    override fun saveSolveRecord(record: SolveRecord) {
        val list = historyByProfile.getOrPut(record.profileId) { mutableListOf() }
        list.add(record)
    }

    override fun getSolveHistory(profileId: String): List<SolveRecord> = historyByProfile[profileId]?.toList().orEmpty()
}
