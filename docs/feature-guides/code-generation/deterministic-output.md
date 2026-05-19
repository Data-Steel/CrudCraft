---
title: "Deterministic Output"
description: "Keep CrudCraft generated source stable for the same entity model and configuration."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Contributors"
  - "Maintainers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
related:
  - "/architecture/generated-code-lifecycle"
  - "/architecture/testing-architecture"
  - "/feature-guides/code-generation"
---

# Deterministic Output

Deterministic output means the same model and generation options produce the same generated source.

Use this page when reviewing generated diffs or changing generator logic.

## Who this page is for

This page is for contributors and maintainers working on `crudcraft-codegen`.

## When to use this page

Use this page when generated output changes unexpectedly or generator ordering changes.

## When not to use this page

Do not use this page for runtime configuration.

## Prerequisites

- The same source model is used for both generation runs.
- Processor options are the same.
- The build runs from a clean state.

## Quick example

```bash
./mvnw clean compile
git diff -- target/generated-sources/annotations
```

Expected result: generated diffs should reflect only intentional model or generator changes.

## Required generator behavior

| Area | Requirement |
|---|---|
| Field ordering | Stable ordering. |
| Imports | Stable and minimal imports. |
| Methods | Stable generation order. |
| Metadata | Stable generated values for same input. |
| Headers | No wall-clock timestamps. |

## Reproducible build validation

CI validates full package reproducibility by running two clean Maven builds with
`-Dreproducible-builds=true` and comparing SHA-256 checksums for generated JARs. A checksum drift
means generated sources, bytecode, resources, or packaging metadata are still carrying
non-deterministic data.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Iterating over unsorted sets | Output can differ between runs. | Sort before writing. |
| Including current time | Builds become non-reproducible. | Use stable build metadata only when required. |
| Testing only compile success | Non-deterministic diffs still pass. | Add golden-file or source assertions. |

## Troubleshooting

If generated output changes without source changes, compare ordering of fields, imports, methods, and metadata collections.

## Related documentation

- [Generated Code Lifecycle](../../architecture/generated-code-lifecycle.md)
- [Testing Architecture](../../architecture/testing-architecture.md)
- [Code Generation](../code-generation/)
