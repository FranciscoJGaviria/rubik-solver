# Fase 2 — Esqueleto Kotlin Multiplatform, Módulos Onion, Konsist y CI Completo

## Objetivo

Construir la base compilable y verificada del proyecto siguiendo `docs/specs/technical-spec.md` y `AGENTS.md`:

1. **Módulos Gradle por capa Onion** (`:shared:domain`, `:shared:application`, `:shared:infrastructure`, `:shared:presentation`, `:androidApp`) + proyecto host mínimo **`iosApp/`**.
2. **Paquetes de los 5 Bounded Contexts** (`cube`, `solving`, `learning`, `recognition`, `profile`) con marcadores mínimos en `:shared:domain`.
3. **Pruebas de arquitectura (Konsist)** que bloquean cualquier violación de dependencias entre capas o de pureza del dominio.
4. **Arnés ATDD (Kotest `BehaviorSpec`)** + script de verificación de trazabilidad (`scripts/verify_spec_traceability.py`) para comprobar IDs entre `functional-spec.md`, `technical-spec.md` y tests.
5. **Calidad estática y cobertura:** `ktlint`, `detekt` y `Kover` (gate ≥ 95 % en `:shared:domain`).
6. **CI/CD en GitHub Actions:** `.github/workflows/ci.yml` y `.github/workflows/release.yml` (`release-please`).

## Requisitos y decisiones que cumple

- **Funcionales / Calidad:** `NFR-6` (Android 8.0 / API 26+ e iOS 16+), `NFR-1` (cero dependencias de red), `FR-1.1` (`AC-1.1`).
- **Arquitectónicos:** `ADR-01` (módulos Gradle por capa Onion y paquetes por Bounded Context), `ADR-06` (Kotest `BehaviorSpec` en `commonTest`).

## Pasos de implementación

1. Actualizar `docs/specs/technical-spec.md` §1 y guardar este plan temporal en `docs/specs/wip/phase2-kmp-skeleton.md`.
2. Instalar y fijar `java` (JDK 21) y `gradle` con `asdf` en `.tool-versions` y generar el Gradle Wrapper (`./gradlew`).
3. Escribir primero las pruebas en rojo:
   - `shared/domain/src/jvmTest/kotlin/com/rubiksolver/architecture/OnionArchitectureKonsistSpec.kt`
   - `shared/domain/src/commonTest/kotlin/com/rubiksolver/domain/cube/CubeSizeSpec.kt` (`AC-1.1`)
4. Crear los módulos `:shared:domain`, `:shared:application`, `:shared:infrastructure`, `:shared:presentation`, `:androidApp` e `iosApp/` y pasar a verde (`RED -> GREEN`).
5. Configurar `ktlint`, `detekt`, `Kover`, `scripts/verify_spec_traceability.py`, `.github/workflows/ci.yml` y `.github/workflows/release.yml`.
6. Ejecutar todas las pruebas y presentar revisión humana antes de borrar este plan y abrir el PR a `main`.
