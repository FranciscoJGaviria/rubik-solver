# Fase 4 — Motor 3D en Kotlin Común y Visor Interactivo

## Objetivo

Implementar el motor de renderizado 3D 100 % en Kotlin común y el visor interactivo en Compose Multiplatform siguiendo `docs/specs/functional-spec.md` (`FR-4`, `FR-7.3`) y `docs/specs/technical-spec.md` §6 (`ADR-02`).

## Requisitos y criterios de aceptación cubiertos

- `FR-4.1` (`AC-4.1`): El cubo 3D refleja siempre el `CubeState` actual y actualiza los stickers tras completar la animación de un movimiento.
- `FR-4.2` (`AC-4.2`): Rotación libre del cubo completo (órbita de cámara) y botón para restablecer la vista por defecto sin alterar el `CubeState`.
- `FR-4.3` (`AC-4.3`): Animación del giro de la capa activa con interpolación suave e indicador visual de la dirección de giro.
- `FR-4.4` (`AC-4.4`): Modo *free-play*: deslizar sobre una capa del cubo 3D aplica el movimiento correspondiente.
- `FR-7.3` (`AC-7.3`): Asistencia para daltonismo: símbolo geométrico distinto por cada color de sticker.

## Pasos de implementación

1. Crear la rama `feat/cube-3d-engine` y guardar este plan temporal en `docs/specs/wip/phase4-3d-engine.md`.
2. Escribir primero las pruebas ATDD, TDD y Property-Based (`Cube3DViewerAcceptanceSpec`, `QuaternionAndProjectionPropertySpec`, `Cube3DCommonTest`, `ColorBlindSymbolTest`).
3. Implementar `ColorBlindSymbol` en `:shared:domain` y el motor 3D (`Vec3`, `Quaternion`, `CameraOrbit`, `LayerTurnAnimation`, `Cube3DProjectionEngine`, `FreePlayGesturePicker`, `CubeViewerController`, `Cube3DCanvas`, `CubeViewerScreen`) en `:shared:presentation`.
4. Verificar trazabilidad, `ktlintCheck`, `detekt`, `jvmTest` y `koverVerify` (≥ 95 % en `:shared:domain`) y abrir el PR hacia `main` para revisión humana.
