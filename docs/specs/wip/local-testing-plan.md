# Temporary Plan — Local Compose Desktop Visual Runner (`ADR-07`)

## Architectural Decision Fulfilled

- `ADR-07`: Enable a Compose Multiplatform JVM Desktop entry point (`shared/presentation/src/jvmMain/kotlin/com/rubiksolver/presentation/Main.kt`) so developers can launch and visually test `App()` on macOS/Linux/Windows via `./gradlew :shared:presentation:run`.

## Steps

1. Update `docs/specs/technical-spec.md` with `ADR-07` and JVM desktop runner documentation.
2. Add `jvmMain` dependency `compose.desktop.currentOs` and `compose.desktop.application` configuration in `shared/presentation/build.gradle.kts`.
3. Create `shared/presentation/src/jvmMain/kotlin/com/rubiksolver/presentation/Main.kt` mounting `App()` in a `Window` (`440.dp x 900.dp`).
4. Run verification (`ktlintCheck`, `detekt`, `jvmTest`) and launch `./gradlew :shared:presentation:run`.
