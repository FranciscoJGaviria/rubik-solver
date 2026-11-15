package com.rubiksolver.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.FunSpec

class OnionArchitectureKonsistSpec :
    FunSpec({
        val domainScope = Konsist.scopeFromDirectory("shared/domain/src/commonMain")
        val applicationScope = Konsist.scopeFromDirectory("shared/application/src/commonMain")
        val infrastructureScope = Konsist.scopeFromDirectory("shared/infrastructure/src/commonMain")
        val presentationScope = Konsist.scopeFromDirectory("shared/presentation/src/commonMain")

        test("domain layer has zero imports outside kotlin.* and com.rubiksolver.domain.*") {
            domainScope
                .files
                .imports
                .assertTrue { import ->
                    import.name.startsWith("kotlin.") ||
                        import.name.startsWith("com.rubiksolver.domain.")
                }
        }

        test("domain layer does not depend on application, infrastructure or presentation") {
            domainScope
                .files
                .imports
                .assertFalse { import ->
                    import.name.startsWith("com.rubiksolver.application.") ||
                        import.name.startsWith("com.rubiksolver.infrastructure.") ||
                        import.name.startsWith("com.rubiksolver.presentation.")
                }
        }

        test("application layer does not depend on infrastructure or presentation") {
            applicationScope
                .files
                .imports
                .assertFalse { import ->
                    import.name.startsWith("com.rubiksolver.infrastructure.") ||
                        import.name.startsWith("com.rubiksolver.presentation.")
                }
        }

        test("infrastructure and presentation layers do not depend on each other") {
            infrastructureScope
                .files
                .imports
                .assertFalse { import ->
                    import.name.startsWith("com.rubiksolver.presentation.")
                }
            presentationScope
                .files
                .imports
                .assertFalse { import ->
                    import.name.startsWith("com.rubiksolver.infrastructure.")
                }
        }

        test("all domain properties are immutable (val, never var)") {
            domainScope
                .classes()
                .properties()
                .assertTrue { property ->
                    property.isVal
                }
        }
    })
