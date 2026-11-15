# Documentation

| Document | Purpose |
|---|---|
| [Functional spec](specs/functional-spec.md) | **What** the product does. No technology. |
| [Technical spec](specs/technical-spec.md) | **How** it is built. References functional IDs, never restates them. |
| [Architecture diagrams](architecture/images/) | C4 diagrams (context, containers, components). |
| [`specs/wip/`](specs/wip/) | Temporary feature plans. Exist only on feature branches; deleted before merge. |

Contribution rules: [AGENTS.md](../AGENTS.md).

## Roadmap

Each phase starts with its own plan in `specs/wip/` on a short-lived branch.

| Phase | Scope | Main requirements |
|---|---|---|
| 1 | Specifications and agent rules | — |
| 2 | Kotlin Multiplatform skeleton, onion modules, full CI, architecture tests | NFR-6 |
| 3 | Cube domain: sizes, moves, notation, validation | FR-2 |
| 4 | 3D engine and interactive viewer | FR-4 |
| 5 | Manual input and solvers (beginner + fast/optimal) | FR-1.4, FR-1.5, FR-3 |
| 6 | Animated step-by-step guide | FR-3 |
| 7 | Camera scanning | FR-1.2, FR-1.3 |
| 8 | Tutor mode, profiles and progress; Ortega, CFOP and Roux | FR-5, FR-6 |
