# AGENTS.md — Rules every agent MUST follow

These rules apply to every contributor, human or AI agent. They are not optional.

## 0. Source of truth

- Functional spec (**what**): [docs/specs/functional-spec.md](docs/specs/functional-spec.md)
- Technical spec (**how**): [docs/specs/technical-spec.md](docs/specs/technical-spec.md)
- If code and spec disagree, the spec wins. If the spec is wrong, change the spec first, in the same PR.
- Keep the specs elegant: one requirement, one place, one ID. Never duplicate content between specs.
  The functional spec never names technologies; the technical spec never restates requirements, it references
  their IDs (`FR-x`, `NFR-x`, `AC-x.y`).

## 1. Change workflow (spec → plan → test → code)

Every change follows this exact sequence, in the same short-lived branch and PR:

1. **Classify the change:**

   | Change type | Functional spec (`functional-spec.md`) | Technical spec (`technical-spec.md`) |
   |---|---|---|
   | New feature / behaviour change | Add or edit `FR-x.y` / `NFR-x` + Gherkin `AC-x.y` | Update if design or architecture changes |
   | Architecture / design adjustment | — | Update the affected section + add/update a row in *Decisions* (ADR table) and C4 diagrams if impacted |
   | Bug fix (spec already correct) | Add a missing `AC-x.y` if the scenario was uncovered | — |
   | Refactor (no behaviour change) | — | Update only if module/package structure documented in the spec changes |

2. **Update the specs first.**
   - New requirements take the next sequential ID; IDs are **never** reused or renumbered.
   - Deprecated requirements are marked `~~FR-x.y~~ (removed)` along with their `AC-x.y`, and the corresponding acceptance tests are deleted.
3. **Write the temporary plan** in `docs/specs/wip/<feature>.md`:
   - Committed **only** on the feature branch and **must be deleted** before merging (CI blocks PRs to `main` that contain plans).
   - Lists the requirement IDs (`FR-x.y`, `NFR-x`, `AC-x.y`) or architectural decisions it fulfils, the acceptance tests it will add/modify, and the implementation steps.
4. **Get human review** on the spec changes and plan before writing production code, and at the end of each phase.
5. **Execute with ATDD + TDD** (§2): failing acceptance test → inner TDD loop → green.
6. **Delete the `wip/` plan** and open/merge the PR with a Conventional Commit title.

If during implementation you discover the spec is incomplete or wrong, **stop**, update the spec first in the same branch, and highlight the spec change in the PR description.

## 2. Acceptance-first, test-driven (ATDD + TDD)

- Start every feature from its acceptance criteria (Gherkin `AC-x.y` in the functional spec).
- Write the failing acceptance test first (Kotest `BehaviorSpec` in `commonTest`, named with the AC ID).
- Then the inner TDD loop: red → green → refactor. No production code without a failing test that demands it.
- Domain layer: aim for 100% coverage; use property-based tests for cube invariants.
- Never delete or weaken a test to make the build pass.

## 3. Architecture: Onion + DDD

- Layers, inner to outer: `domain` → `application` → `infrastructure` / `presentation` → app (composition root).
- Dependencies point inward only. `domain` has zero dependencies: no Compose, no platform APIs, no DI, no I/O.
- Bounded contexts: **Cube**, **Solving**, **Learning**, **Recognition**, **Profile** (see the technical spec).
- Name code with the ubiquitous language from the functional spec glossary.
- Ports (interfaces) live in `domain`/`application`; adapters live in `infrastructure`.
- Prefer immutable value objects and aggregates; enforce invariants in constructors/factories.
- Architecture rules are enforced by Konsist tests. Never disable or bypass them.

## 4. Language & platform

- Kotlin only. Kotlin Multiplatform for 100% of the logic and UI (Compose Multiplatform).
- Platform-specific code only through `expect`/`actual` or adapters in `infrastructure`.
- Sole exception: the minimal iOS host entry point required by Xcode.

## 5. Git & delivery

- Trunk-based development: `main` is always releasable. Branches are short-lived (≤ 2 days).
- Branch names: `<type>/<short-description>` with types `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`.
- [Conventional Commits](https://www.conventionalcommits.org/) for every commit and PR title. Squash-merge to `main`.
- Incomplete features ship behind feature flags, never on long-lived branches.
- CI/CD runs on GitHub Actions. A red pipeline blocks the merge.

## 6. Definition of Done

- Acceptance tests for the referenced `AC-x.y` pass.
- Unit, property and architecture (Konsist) tests pass; lint (ktlint, detekt) passes.
- Specs updated if behaviour or design changed.
- The `docs/specs/wip/` plan for the feature is deleted.
