# Contributing to CrudCraft

## Workflow
- Base branch: `develop`
- Use **Conventional Commits** in PR title: `feat: …`, `fix: …`, `docs: …`, `chore: …`
- Open PRs small, with tests (required), and docs (when relevant).

## Dev setup
- JDK 21, Maven 3.9+
- `crudcraft-parent` is the aggregator; modules: `crudcraft-codegen`, `crudcraft-runtime`, `crudcraft-projection`, `crudcraft-sample-app`,
    `crudcraft-security`, `crudcraft-starter`, `crudcraft-tools`.

## Code style & quality
- EditorConfig enforced
- Add/keep unit tests
- Avoid breaking changes without `BREAKING CHANGE:` note

## Releases
- SNAPSHOTs from `develop` to Sonatype
- **Release PR** to `main` via release-please