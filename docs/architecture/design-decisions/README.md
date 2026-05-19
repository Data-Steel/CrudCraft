---
title: "Design Decisions"
description: "Read the architecture decision records that define long-lived CrudCraft design constraints."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/system-overview"
  - "/architecture/module-boundaries"
  - "/architecture/generated-code-lifecycle"
---

# Design Decisions

Architecture decision records explain why a constraint exists. They are not feature guides; they are review anchors for changes that would otherwise blur module ownership, generated file ownership, or runtime compatibility.

## Current Decisions

| ADR | Decision | Use it when |
|---|---|---|
| [ADR 0001](adr-0001-codegen-runtime-split.md) | Keep compile-time generation separate from request-time runtime behavior. | A change moves behavior between `crudcraft-codegen`, generated source, and `crudcraft-runtime-*`. |
| [ADR 0002](adr-0002-runtime-module-boundaries.md) | Keep optional runtime capabilities in separate runtime modules and starters. | A dependency would make search, projection, export, security, or extensions mandatory. |
| [ADR 0003](adr-0003-generated-code-ownership.md) | Separate strict regenerated files from editable stubs. | A change affects whether generated files are overwritten or application-owned. |

## ADR Quality Bar

New ADRs should state:

- context and concrete code paths affected;
- problem the decision solves;
- accepted decision;
- alternatives considered;
- consequences and tradeoffs;
- invariants reviewers can enforce;
- tests or docs that should change when the decision is touched.

## Related Documentation

- [Architecture](../)
- [Module Boundaries](../module-boundaries.md)
- [Generated Code Lifecycle](../generated-code-lifecycle.md)
