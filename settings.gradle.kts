rootProject.name = "rubik-solver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared:domain")
include(":shared:application")
include(":shared:infrastructure")
include(":shared:presentation")

val androidEnabled = providers.gradleProperty("androidEnabled").orNull == "true"

if (androidEnabled) {
    include(":androidApp")
}
