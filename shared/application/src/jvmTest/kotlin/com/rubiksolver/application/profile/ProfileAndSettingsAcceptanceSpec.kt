package com.rubiksolver.application.profile

import com.rubiksolver.domain.learning.Curriculum
import com.rubiksolver.domain.profile.AdultCheckChallenge
import com.rubiksolver.domain.profile.AppLanguage
import com.rubiksolver.domain.profile.Profile
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.profile.SolveRecord
import com.rubiksolver.domain.solving.Method
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

private class FakeProfileRepository : ProfileRepository {
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
        masteryByProfile.getOrPut(profileId) { linkedSetOf() }.add(techniqueId)
    }

    override fun getMasteredTechniques(profileId: String): Set<String> = masteryByProfile[profileId]?.toSet().orEmpty()

    override fun saveSolveRecord(record: SolveRecord) {
        historyByProfile.getOrPut(record.profileId) { mutableListOf() }.add(record)
    }

    override fun getSolveHistory(profileId: String): List<SolveRecord> = historyByProfile[profileId]?.toList().orEmpty()
}

class ProfileAndSettingsAcceptanceSpec :
    BehaviorSpec({
        val repository = FakeProfileRepository()
        val manageProfiles = ManageProfilesUseCase(repository)

        Given("an empty local profile list (AC-6.1)") {
            When("the user creates a Kid profile named Sofi") {
                val sofi = manageProfiles.createProfile(name = "Sofi", avatar = "cubie-star", kind = ProfileKind.KID)
                val allProfiles = manageProfiles.listProfiles()

                Then("Sofi appears in the profile list with kind Kid") {
                    allProfiles.shouldContain(sofi)
                    sofi.name shouldBe "Sofi"
                    sofi.kind shouldBe ProfileKind.KID
                }
            }
        }

        Given("profiles Sofi and Ana (AC-6.2)") {
            val sofi = manageProfiles.createProfile(name = "Sofi", avatar = "star", kind = ProfileKind.KID)
            val ana = manageProfiles.createProfile(name = "Ana", avatar = "rocket", kind = ProfileKind.ADULT)

            When("Sofi masters a technique") {
                manageProfiles.recordMasteredTechnique(sofi.id, Curriculum.WHITE_CROSS_TECHNIQUE_ID)

                Then("the technique is mastered for Sofi and not mastered for Ana") {
                    manageProfiles
                        .masteredTechniques(sofi.id)
                        .contains(Curriculum.WHITE_CROSS_TECHNIQUE_ID) shouldBe true
                    manageProfiles
                        .masteredTechniques(ana.id)
                        .contains(Curriculum.WHITE_CROSS_TECHNIQUE_ID) shouldBe false
                }
            }
        }

        Given("the timer is enabled for a profile (AC-6.3)") {
            val ana = manageProfiles.createProfile(name = "AnaTimed", avatar = "clock", kind = ProfileKind.ADULT)

            When("the user finishes a timed solve") {
                val record =
                    manageProfiles.recordSolve(
                        profileId = ana.id,
                        method = Method.CFOP,
                        moveCount = 48,
                        elapsedMillis = 19_450L,
                    )
                val history = manageProfiles.solveHistory(ana.id)

                Then("the time is stored in the history") {
                    history.shouldContain(record)
                    history.first().elapsedMillis shouldBe 19_450L
                }
            }
        }

        Given("the active profile is a Kid profile (AC-6.4)") {
            val kid = manageProfiles.createProfile(name = "Leo", avatar = "lion", kind = ProfileKind.KID)
            val challenge = AdultCheckChallenge(leftOperand = 7, rightOperand = 8)

            When("deleting a profile is requested without and with passing the adult check") {
                val rejectedDeletion =
                    manageProfiles.deleteProfile(
                        activeProfile = kid,
                        targetProfileId = kid.id,
                        challenge = challenge,
                        providedAnswer = 10,
                    )
                val acceptedDeletion =
                    manageProfiles.deleteProfile(
                        activeProfile = kid,
                        targetProfileId = kid.id,
                        challenge = challenge,
                        providedAnswer = 15,
                    )

                Then("an adult check is required before deletion") {
                    rejectedDeletion shouldBe false
                    acceptedDeletion shouldBe true
                }
            }
        }

        Given("the device language is Spanish (AC-7.1)") {
            When("the app starts for the first time") {
                val resolvedLanguage = AppLanguage.fromDeviceLocaleTag("es-ES")
                val fallbackLanguage = AppLanguage.fromDeviceLocaleTag("fr-FR")

                Then("the app is in Spanish and unsupported languages default to English") {
                    resolvedLanguage shouldBe AppLanguage.SPANISH
                    fallbackLanguage shouldBe AppLanguage.ENGLISH
                }
            }
        }
    })
