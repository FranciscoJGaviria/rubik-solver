import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { element -> element.file.path.contains("/build/") }
        }
    }

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        source.setFrom(
            files(
                "src/commonMain/kotlin",
                "src/commonTest/kotlin",
                "src/jvmMain/kotlin",
                "src/jvmTest/kotlin",
                "src/androidMain/kotlin",
                "src/iosMain/kotlin",
                "src/main/kotlin",
            ),
        )
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
        exclude { it.file.path.contains("/build/") }
    }
}
