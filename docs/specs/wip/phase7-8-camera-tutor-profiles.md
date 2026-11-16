# Fases 7 y 8 (Unificadas) — Escaneo con Cámara, Modo Tutor, Perfiles y Progreso

## Objetivo

Completar en una sola rama (`feat/camera-tutor-and-profiles`) y un único Pull Request las dos últimas fases del roadmap (`docs/README.md`), cubriendo los 16 criterios de aceptación restantes para alcanzar 42 / 42 `AC-x.y` verificados en `scripts/verify_spec_traceability.py`.

## Requisitos y criterios de aceptación cubiertos

- **Fase 7 (`Recognition`):** `FR-1.2`, `FR-1.3` (`AC-1.2.1`, `AC-1.2.2`).
- **Fase 8 (`Learning`):** `FR-5.1`..`FR-5.9` (`AC-5.1`..`AC-5.9`).
- **Fase 8 (`Profile & Settings`):** `FR-6.1`..`FR-6.4`, `FR-7.1` (`AC-6.1`..`AC-6.4`, `AC-7.1`).

## Pasos de implementación

1. Crear la rama `feat/camera-tutor-and-profiles` y guardar este plan temporal en `docs/specs/wip/phase7-8-camera-tutor-profiles.md`.
2. Escribir primero las suites ATDD y multiplataforma (`CameraScanAcceptanceSpec`, `TutorAcceptanceSpec`, `ProfileAndSettingsAcceptanceSpec`, `RecognitionTutorProfileCommonTest`).
3. Implementar el dominio `Recognition` (`RgbColor`, `LabColor`, `ScanOrientationGuide`, `ConstrainedColorClassifier`), el dominio `Learning` (`Technique`, `LessonPhase`, `Lesson`, `Curriculum`, `PracticeScrambleGenerator`, `HintPolicy`, `MasteryTracker`, `GraduationPolicy`) y el dominio `Profile` (`Profile`, `SolveRecord`, `AdultCheckChallenge`, `AppLanguage`).
4. Implementar los puertos, casos de uso, adaptadores en memoria y pantallas UI (`CameraScannerScreen`, `TutorScreen`, `ProfileScreen`).
5. Verificar trazabilidad (42/42 ACs), `ktlintCheck`, `detekt`, `jvmTest` y `koverVerify` (≥ 95 % en `:shared:domain`) y abrir el PR hacia `main` para revisión humana.
