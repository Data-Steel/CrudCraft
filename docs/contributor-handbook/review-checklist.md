---
title: "Review Checklist"
description: "Self-check a CrudCraft change before requesting maintainer review."
section: "Contributor Handbook"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/contributor-handbook/pull-request-process"
  - "/maintainer-handbook/quality-gates"
  - "/architecture/testing-architecture"
---

# Review Checklist

Use this checklist before requesting review and after responding to requested changes. It is a readiness check, not merge approval.

## Scope

- The PR has one clear purpose.
- The owning module is correct.
- Unrelated cleanup is removed.
- Generated artifacts are not edited as source.
- Dependency changes are not mixed with unrelated behavior.

## Behavior And Contracts

- Public annotation, generated API, runtime, starter, configuration, and documentation impact are identified.
- Generated DTO/route/metadata/output changes are intentional and described.
- Compatibility impact is stated.
- Security-sensitive changes include allowed and denied behavior.

## Tests

- Tests exist at the layer where the contract changed.
- Failure paths are covered when relevant.
- Golden fixtures were reviewed before updating.
- Sample app coverage exists when generated code and runtime modules interact.

## Docs

- Feature facts are in the relevant Feature Guide.
- Architecture changed only when boundaries/contracts/lifecycle changed.
- Contributor/Maintainer pages changed only for repository workflow or policy.
- Links and docs inventory remain valid.

## Verification

- Focused commands are listed with outcomes.
- Final local verification is listed or skipped with a reason.
- Docs checks are listed when docs or public behavior changed.
- Known residual risk is stated plainly.

## Related Documentation

- [Pull Request Process](pull-request-process.md)
- [Quality Gates](../maintainer-handbook/quality-gates.md)
- [Testing Architecture](../architecture/testing-architecture.md)
