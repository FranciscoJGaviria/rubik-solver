# Fase 5 — Ingreso Manual y Solvers con Patrón Strategy (2x2 y 3x3)

## Objetivo

Implementar el Bounded Context **Solving** (`com.rubiksolver.domain.solving`) usando el patrón **Strategy** por `(CubeSize, Method)` (`docs/specs/technical-spec.md` §5, `ADR-03`), el adaptador de tablas de poda en `:shared:infrastructure`, los casos de uso de resolución en `:shared:application` y la interfaz de ingreso manual/scramble en `:shared:presentation`.

## Requisitos y criterios de aceptación cubiertos

- `FR-1.4`, `FR-1.5` (`AC-1.4`, `AC-1.5`): Flujo interactivo completo de pintado manual de las 6 caras y entrada de scramble en UI.
- `FR-3.1` (`AC-3.1`): Métodos ofrecidos según tamaño (`2x2`: `BEGINNER`, `ORTEGA`, `OPTIMAL`; `3x3`: `BEGINNER`, `CFOP`, `ROUX`, `FAST`) y método por defecto según perfil (`KID` → `BEGINNER`; `ADULT` → último usado o `BEGINNER`).
- `FR-3.2` (`AC-3.2.1`): Solución estructurada en `Stage`s y `Step`s: los métodos por etapas incluyen notación y explicación en lenguaje llano; `FAST` y `OPTIMAL` muestran solo movimientos (`ExplanationKey.MOVE_ONLY`).
- `FR-3.2`, `NFR-4` (`AC-3.2.2`): Para cualquier estado válido y cualquier método disponible, aplicar todos los pasos de `Solution` deja el cubo resuelto (con ≤ 11 movimientos en `2x2 OPTIMAL` y ≤ 25 en `3x3 FAST`).

## Pasos de implementación

1. Crear la rama `feat/manual-input-and-solvers` y guardar este plan temporal en `docs/specs/wip/phase5-manual-input-and-solvers.md`.
2. Actualizar `OnionArchitectureKonsistSpec` y escribir primero las pruebas ATDD y de propiedades (`SolveGuideGenerationAcceptanceSpec`, `SolvingStrategyContractPropertySpec`, `SolverCommonTest`, `ManualInputControllerTest`).
3. Implementar `Solver`, `SolvingStrategy`, `Solution`, `Stage`, `Step`, `ExplanationKey`, `PruningTables` y las 7 estrategias en `:shared:domain`.
4. Implementar `InMemoryPruningTablesAdapter` en `:shared:infrastructure`, `GetAvailableMethodsUseCase` y `GenerateSolutionUseCase` en `:shared:application`, y `ManualInputController` + `ManualCubeInputScreen` en `:shared:presentation`.
5. Verificar trazabilidad, `ktlintCheck`, `detekt`, `jvmTest` y `koverVerify` (≥ 95 % en `:shared:domain`) y abrir el PR hacia `main` para revisión humana.
