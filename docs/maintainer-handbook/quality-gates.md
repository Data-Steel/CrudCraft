---
title: "Quality Gates"
description: "Decide whether CrudCraft changes have enough tests, generated output review, docs, compatibility analysis, and CI evidence to merge."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/architecture/testing-architecture"
  - "/contributor-handbook/review-checklist"
  - "/maintainer-handbook/ci-cd"
---

# Quality Gates

Quality gates are merge policy. Passing a focused test is not enough when the change affects generated code, runtime behavior, optional modules, docs, or release contracts.

## Merge Blockers

Block merge when:

- required CI checks fail;
- generated output changed without review;
- generated public API or HTTP shape changed without compatibility review;
- public behavior changed without docs;
- security-sensitive behavior lacks denied-path tests;
- optional runtime dependencies became mandatory by accident;
- docs duplicate or contradict canonical Feature Guides;
- release/version/deprecation impact is unresolved;
- quality gates were skipped without a clear maintainer decision.

## Evidence Matrix

| Change | Required evidence |
|---|---|
| Public annotation/enum/interface | API tests, codegen consumption tests, docs, compatibility review. |
| Descriptor or writer behavior | Processor/writer tests, golden diff review, deterministic output check. |
| Generated controller/service/DTO shape | Golden/compile tests, generated diff explanation, docs if public. |
| Runtime core | Runtime tests and sample app coverage when generated services rely on the behavior. |
| Search/projection/export/security runtime | Runtime tests, denied/invalid/failure tests, generated integration when exposed via endpoints. |
| Starter composition | Dependency review, starter tests where available, Runtime Modules docs update. |
| Docs-only | Link/navigation checks, no stale deleted-section references, correct inventory placement. |
| CI/tooling | Local reproduction or dry-run evidence, policy update when gate behavior changes. |
| Dependency upgrade | Dependency Management checklist and affected test evidence. |

## Maintainer Decision Notes

When blocking, say exactly what evidence is missing:

```text
Blocked: `BookResponseDto` generated shape changed. Need golden diff explanation,
compatibility decision, and Feature Guide update before merge.
```

When approving a risk:

```text
Accepted risk: full OWASP scan not run locally; OSV passed on this PR and
weekly/manual OWASP dependency-scan has no blocking finding for the affected dependency.
```

## Related Documentation

- [Testing Architecture](../architecture/testing-architecture.md)
- [Review Checklist](../contributor-handbook/review-checklist.md)
- [CI/CD](ci-cd.md)
