---
title: "Pull Request Process"
description: "Prepare a focused CrudCraft pull request with behavior impact, tests, generated output review, docs, compatibility notes, and verification."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/contributor-handbook/review-checklist"
  - "/contributor-handbook/local-build"
  - "/contributor-handbook/documentation-standards"
---

# Pull Request Process

A CrudCraft PR should be easy to review because the scope is clear, the owning modules are right, and the evidence matches the risk.

## Before Opening

1. Keep the PR to one behavior, bug fix, dependency group, docs change, or tooling change.
2. Remove unrelated formatting and exploratory edits.
3. Confirm module boundaries.
4. Run focused tests for the changed layer.
5. Review generated diffs when codegen changed.
6. Update Feature Guides or architecture/handbook docs when public behavior or project policy changed.
7. Run final verification or explain what remains unrun.

## PR Description Template

```text
Summary:
- ...

Behavior:
- ...

Generated output:
- None / changed DTO components / changed routes / changed metadata / changed Insomnia output.

Tests:
- ...

Docs:
- ...

Compatibility:
- Compatible / user-visible behavior correction / breaking / needs maintainer decision.

Verification:
- ...
```

## Good Examples

Generated route change:

```text
Generated output:
- `BookController` now includes `GET /books/export` when `CrudEndpoint.EXPORT` is included.
- Golden fixture reviewed; no existing routes changed.
```

Runtime security change:

```text
Tests:
- Field-security export regression test proves protected fields are filtered before serialization.
- Sample app security test covers denied export for non-admin users.
```

## Common Review Blockers

- “Tests updated” without commands.
- Generated output changed without explanation.
- Public behavior changed without docs.
- New dependency added without module/starter impact.
- Security behavior changed without denied-path tests.
- Compatibility impact left for reviewers to infer.

## Related Documentation

- [Review Checklist](review-checklist.md)
- [Local Build](local-build.md)
- [Documentation Standards](documentation-standards.md)
