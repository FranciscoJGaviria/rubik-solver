import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "com.rubiksolver.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rubiksolver.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(projects.shared.presentation)
    implementation(projects.shared.infrastructure)
    implementation(projects.shared.application)
    implementation(projects.shared.domain)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.core)
}
