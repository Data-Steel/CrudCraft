---
title: "Contributor Handbook"
description: "Make CrudCraft changes in the right module, with the right tests, generated output review, documentation, and verification."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/architecture"
  - "/doc-requirement"
  - "/maintainer-handbook"
---

# Contributor Handbook

This handbook is for people changing the CrudCraft repository. It is deliberately practical: find the owning module, make the smallest correct change, prove it at the right layer, update the canonical docs, and give reviewers exact verification.

Maintainer policy lives in [Maintainer Handbook](../maintainer-handbook/). Contributors can propose policy changes, but merge gates, release decisions, compatibility exceptions, and security handling are maintainer decisions.

## Start Here

| Task | Read |
|---|---|
| Set up a machine | [Development Setup](development-setup.md) |
| Find the owning directory | [Repository Structure](repository-structure.md) |
| Choose the owning Maven module | [Module Overview](module-overview.md) |
| Run focused checks while coding | [Running Tests](running-tests.md) |
| Add useful coverage | [Writing Tests](writing-tests.md) |
| Run final local verification | [Local Build](local-build.md) |
| Follow Java/codegen/runtime rules | [Coding Standards](coding-standards.md) |
| Update docs correctly | [Documentation Standards](documentation-standards.md) |
| Prepare a PR | [Pull Request Process](pull-request-process.md) |
| Self-check before review | [Review Checklist](review-checklist.md) |

## Contributor Contract

Every PR should answer these questions without reviewer guesswork:

- Which behavior changed?
- Which module owns that behavior?
- Is the change compile-time codegen, generated source shape, runtime behavior, starter wiring, documentation, or policy?
- Which tests would fail without it?
- Did generated source change, and was the diff reviewed?
- Did public behavior or docs change?
- Which exact commands passed locally?

## Example Change Path

For a new generated search operator:

1. Update public API if an enum or annotation changes in `crudcraft-api`.
2. Update descriptor extraction and writers in `crudcraft-codegen`.
3. Update request-time validation/execution in `crudcraft-runtime-search`.
4. Add codegen tests, runtime tests, and sample app coverage when the generated endpoint behavior changes.
5. Update [Search Feature Guides](../feature-guides/search/).
6. Run focused commands while coding, then final verification from [Local Build](local-build.md).
7. In the PR, call out generated output, compatibility, docs, and verification.

## Related Documentation

- [Architecture](../architecture/)
- [Documentation Requirements](../doc-requirement.md)
- [Maintainer Handbook](../maintainer-handbook/)
