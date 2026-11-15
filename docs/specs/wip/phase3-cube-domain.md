# Fase 3 — Dominio Cube (2x2 y 3x3, Movimientos, Notación y Validación Algebraica)

## Objetivo

Implementar el Bounded Context **Cube** (`com.rubiksolver.domain.cube`) y sus casos de uso de entrada y validación en `:shared:application` (`com.rubiksolver.application.cube`), cumpliendo el 100 % de los invariantes de `docs/specs/functional-spec.md` y `docs/specs/technical-spec.md` §3–§4.

## Requisitos y criterios de aceptación cubiertos

- `FR-1.3` (`AC-1.3`): Corrección de cualquier sticker antes de confirmar
- `FR-1.4` (`AC-1.4`): Ingreso manual pintando los stickers (24 en 2x2, 54 en 3x3)
- `FR-1.5` (`AC-1.5`): Construcción del estado a partir de un scramble en notación (`"R U R' U'"`)
- `FR-1.6` (`AC-1.6`): Esquema de colores estándar y personalizado
- `FR-2.1` (`AC-2.1`): Rechazo por conteo incorrecto de stickers por color
- `FR-2.2` (`AC-2.2`): Rechazo por centros duplicados o pares opuestos inválidos en 3x3
- `FR-2.3` (`AC-2.3`): Rechazo por pieza imposible o duplicada
- `FR-2.4` (`AC-2.4`): Rechazo por estado irresoluble algebraicamente (esquina girada, arista volteada o paridad impar)
- `FR-2.5` (`AC-2.5`): Explicación en lenguaje simple adaptada para perfiles `KID` y `ADULT` con stickers resaltados
- `FR-2.6` (`AC-2.6`): Detección de cubo ya resuelto

## Pasos de implementación

1. Crear la rama `feat/cube-domain` y registrar este plan temporal en `docs/specs/wip/phase3-cube-domain.md`.
2. Escribir primero las suites ATDD y property-based tests (`CubeInputAcceptanceSpec`, `CubeValidationAcceptanceSpec`, `CubeGroupInvariantsPropertySpec`, `CubeDomainCommonTest`).
3. Implementar los value objects y el agregado en `:shared:domain` (`Face`, `StickerColor`, `ColorScheme`, `StickerPosition`, `TurnDirection`, `Move`, `Algorithm`, `Scramble`, `CornerSlot`, `EdgeSlot`, `CubeTopology`, `CubeState`, `ValidationError`, `ValidationResult`, `CubeValidator`).
4. Implementar los casos de uso en `:shared:application` (`EnterManualCubeUseCase`, `CorrectStickerUseCase`, `ApplyScrambleUseCase`, `ValidateCubeUseCase`).
5. Verificar trazabilidad, `ktlintCheck`, `detekt`, `jvmTest` y `koverVerify` (cobertura ≥ 95% en `:shared:domain`) y abrir el PR para revisión humana.
