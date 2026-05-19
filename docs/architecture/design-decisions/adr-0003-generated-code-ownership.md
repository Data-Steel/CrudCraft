---
title: "ADR 0003: Generated Code Ownership"
description: "Records the decision to separate CrudCraft strict regenerated files from editable generated stubs."
section: "Architecture"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/generated-code-lifecycle"
  - "/architecture/contract-model"
  - "/architecture/testing-architecture"
---

# ADR 0003: Generated Code Ownership

## Status

Accepted.

## Context

CrudCraft writes Java source into the consuming application. Some generated files must always match annotations exactly, while other generated files are useful as application-owned customization points.

Examples of strict outputs include DTO records, generated metadata, search request/specification artifacts, and generated endpoint shape. Examples of customization surfaces include editable stubs documented by file header and Feature Guides.

## Problem

If all generated files are overwritten, users lose custom code. If all generated files are preserved, generated output becomes stale after annotations or model shape change. Both failures are hard to debug because the affected source lives inside the user's application build.

## Decision

Classify generated files as either strict generated files or editable stubs.

```text
Strict generated file
  -> owned by CrudCraft
  -> may be overwritten every generation

Editable generated stub
  -> created by CrudCraft when missing
  -> owned by the application after first creation
```

Ownership must be visible through docs, file headers, or both.

## Consequences

- Deterministic generated files can be golden-tested.
- Users get supported customization locations without editing strict source.
- Ownership changes are breaking or at least compatibility-sensitive.
- Editable stubs can become stale and may require manual application review after model changes.

## Invariants

- Strict generated files may be overwritten.
- Editable stubs must not be overwritten after first creation.
- Generated output must be deterministic for the same input.
- Generated public API shape changes require contract review.
- Manual edits to strict generated files are unsupported.

## Tests

Changes touching this decision should include:

- golden tests for strict generated output;
- compile tests for generated source ownership changes;
- tests proving editable files are skipped when they already exist;
- docs updates for any changed ownership rule.

## Related Documentation

- [Generated Code Lifecycle](../generated-code-lifecycle.md)
- [Contract Model](../contract-model.md)
- [Testing Architecture](../testing-architecture.md)
