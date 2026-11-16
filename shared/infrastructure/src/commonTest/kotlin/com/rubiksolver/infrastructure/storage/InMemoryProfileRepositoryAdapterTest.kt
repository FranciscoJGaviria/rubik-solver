package com.rubiksolver.infrastructure.storage

import com.rubiksolver.domain.profile.Profile
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.profile.SolveRecord
import com.rubiksolver.domain.solving.Method
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryProfileRepositoryAdapterTest {
    @Test
    fun `stores and removes profiles mastery and solve history`() {
        val repo = InMemoryProfileRepositoryAdapter()
        val profile = Profile(id = "p1", name = "Sofi", avatar = "star", kind = ProfileKind.KID)
        repo.saveProfile(profile)
        repo.listProfiles() shouldBe listOf(profile)

        repo.recordMasteredTechnique("p1", "tech-1")
        repo.getMasteredTechniques("p1") shouldBe setOf("tech-1")

        val record = SolveRecord(profileId = "p1", method = Method.BEGINNER, moveCount = 32, elapsedMillis = 12_000L)
        repo.saveSolveRecord(record)
        repo.getSolveHistory("p1") shouldBe listOf(record)

        repo.removeProfile("p1") shouldBe true
        repo.listProfiles() shouldBe emptyList()
    }
}
