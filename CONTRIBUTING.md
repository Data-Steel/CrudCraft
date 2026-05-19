# Contributing to CrudCraft

## Workflow

- Base branch: `main`
- Use Conventional Commits in PR titles (`feat:`, `fix:`, `docs:`, `chore:`).
- Keep PRs focused; include tests and docs updates when behavior changes.

## Dev Setup

- JDK 21
- Maven 3.9+
- Reactor root: `crudcraft-parent`

Main module groups:

- `crudcraft-api`
- `crudcraft-codegen`
- `crudcraft-runtime-*` capability modules
- `crudcraft-starter-*` modules
- `crudcraft-sample-app`
- `crudcraft-tools`

## Quality Rules

- Keep module boundaries intact (`runtime-core` must not import optional runtime capability packages directly).
- Add or update tests in affected module(s).
- Run full test suite before merge:
  - `./mvnw test`
- Dependency versions are centralized in the root `dependencyManagement` section. A child POM may
  omit `<version>` only when the dependency is managed there; `RepositoryGovernanceTest` fails the
  build for unmanaged versionless dependencies.
- Build prerequisites are enforced in Maven: Java 21+, Maven 3.9+, and no duplicate dependency
  declarations in a single POM.

## Release Notes

- No snapshot publishing.
- Feature, fix and dependency PRs target `main`.
- Release Please creates the release PR, tag and GitHub Release from `main`.
- Maven Central publish runs from the published GitHub Release, and docs deploy only after that publish succeeds.
- The release workflow verifies every published library artifact on Maven Central before signing
  release bundles and dispatching docs deployment.
