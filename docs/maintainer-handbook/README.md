---
title: "Maintainer Handbook"
description: "Govern CrudCraft merge gates, CI/CD, releases, compatibility, dependencies, security, regressions, documentation review, and versioning."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/contributor-handbook"
  - "/architecture"
  - "/documentation-inventory"
---

# Maintainer Handbook

Maintainers decide whether a change is safe to merge or release. That decision must be based on contracts, generated output, runtime behavior, documentation accuracy, and the actual CI gates in this repository.

Contributors own implementation evidence. Maintainers own whether that evidence is enough.

## Decision Map

| Decision | Page |
|---|---|
| Can this PR merge? | [Quality Gates](quality-gates.md) |
| Are CI and release workflows enforcing the right checks? | [CI/CD](ci-cd.md) |
| Can this exact source be released? | [Release Process](release-process.md) |
| Is this compatible with existing users? | [Compatibility Policy](compatibility-policy.md) |
| Which version impact is required? | [Versioning Policy](versioning-policy.md) |
| Can behavior be deprecated or removed? | [Deprecation Policy](deprecation-policy.md) |
| How are dependency upgrades reviewed? | [Dependency Management](dependency-management.md) |
| How are security-sensitive reports handled? | [Security Policy](security-policy.md) |
| How are released regressions handled? | [Regression Handling](regression-handling.md) |
| Are docs sufficient? | [Documentation Review Policy](documentation-review-policy.md) |

## Maintainer Review Standard

Before merge, a maintainer should be able to state:

- the affected contract level from [Contract Model](../architecture/contract-model.md);
- whether generated public source or HTTP API shape changed;
- whether runtime/starter dependencies changed;
- whether docs and examples match the actual behavior;
- which tests or CI checks prove the change;
- whether versioning, compatibility, deprecation, or release notes are affected.

## Related Documentation

- [Contributor Handbook](../contributor-handbook/)
- [Architecture](../architecture/)
- [Documentation Inventory](../documentation-inventory.md)
