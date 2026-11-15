# AGENTS.md — Rules every agent MUST follow

These rules apply to every contributor, human or AI agent. They are not optional.

## 0. Source of truth

- Functional spec (**what**): [docs/specs/functional-spec.md](docs/specs/functional-spec.md)
- Technical spec (**how**): [docs/specs/technical-spec.md](docs/specs/technical-spec.md)
- If code and spec disagree, the spec wins. If the spec is wrong, change the spec first, in the same PR.
- Keep the specs elegant: one requirement, one place, one ID. Never duplicate content between specs.
  The functional spec never names technologies; the technical spec never restates requirements, it references
  their IDs (`FR-x`, `NFR-x`, `AC-x.y`).

## 1. Plan first

- Never implement a feature without a plan.
- Plans live in `docs/specs/wip/<feature>.md`, are committed **only** on the feature branch, and **must be deleted**
  before the branch is merged. CI rejects PRs to `main` that still contain plans.
- A plan references the requirement IDs it fulfils and lists the acceptance tests it will add.

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
