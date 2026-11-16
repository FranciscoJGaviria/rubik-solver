# Fase 6 — Guía Animada Paso a Paso

## Objetivo

Implementar el flujo completo de reproducción de la **Guía Animada Paso a Paso** (`FR-3.3`..`FR-3.7`), junto con las preferencias de velocidad de animación (`FR-7.2`) y disposición para mano izquierda/derecha (`FR-7.4`), conectando el `Solver` de la Fase 5 con el motor 3D de la Fase 4.

## Requisitos y criterios de aceptación cubiertos

- `FR-3.3` (`AC-3.3`): Controles de reproducción: play, pause, siguiente paso, paso anterior (animando el movimiento inverso y volviendo al paso previo), salto por etapa y velocidad.
- `FR-3.4` (`AC-3.4`): Instrucción inicial de cómo sostener el cubo (qué cara/color arriba y cuál al frente) antes del primer paso.
- `FR-3.5` (`AC-3.5`): Indicador permanente de progreso: etapa actual y `"n / m"` (p. ej. etapa 2 y `"12 / 40"`).
- `FR-3.6` (`AC-3.6`): Recuperación si el usuario se pierde: reingresar el estado actual del cubo inicia una nueva solución desde ese estado.
- `FR-3.7` (`AC-3.7`): Al aplicar el último paso y quedar resuelto el cubo, se muestra una celebración con el resumen (nombre del método y número de movimientos).
- `FR-7.2` (`AC-7.2`): Velocidad de animación por defecto aplicada al iniciar una guía (`SLOW`, `NORMAL`, `FAST`).
- `FR-7.4` (`AC-7.4`): Disposición de controles principales a la izquierda (`Handedness.LEFT`) o a la derecha (`Handedness.RIGHT`).

## Pasos de implementación

1. Crear la rama `feat/animated-solve-guide` y guardar este plan temporal en `docs/specs/wip/phase6-animated-solve-guide.md`.
2. Escribir primero las pruebas ATDD y multiplataforma (`SolveGuidePlaybackAcceptanceSpec`, `SolveGuideControllerTest`, `GuideDomainSeedsTest`).
3. Implementar `AnimationSpeed`, `Handedness`, `HoldingInstruction`, `CompletionSummary` en `:shared:domain`, `SolveGuideSession` y `ManageSolveGuideUseCase` en `:shared:application`, y `SolveGuideController` + `SolveGuideScreen` en `:shared:presentation`.
4. Verificar trazabilidad, `ktlintCheck`, `detekt`, `jvmTest` y `koverVerify` (≥ 95 % en `:shared:domain`) y abrir el PR hacia `main` para revisión humana.
