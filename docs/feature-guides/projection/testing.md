---
title: "Testing Projections"
description: "Test CrudCraft projection fields, paths, security interaction, and generated response shapes."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-projection"
related:
  - "/feature-guides/projection/projection-paths"
  - "/feature-guides/projection/security-interaction"
  - "/architecture/testing-architecture"
---

# Testing Projections

Projection tests verify generated response shape and source-field mapping.

Use this page when adding or changing projection models.

## Who this page is for

This page is for developers writing tests for projected generated responses.

## When to use this page

Use this page when projection fields, paths, or security behavior changes.

## When not to use this page

Do not rely only on compile success for projection behavior.

## Prerequisites

- Projection runtime is available in the test context.
- Test data contains values for every projected field.
- Security-sensitive projections are tested with different principals.

## Quick example

```java
mockMvc.perform(get("/api/customers/list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").exists());
```

Expected result: the projected field exists and maps to the expected source value.

## Required cases

| Case | Expected result |
|---|---|
| Direct field projection | Value appears in output. |
| Nested path projection | Value maps from the correct nested property. |
| Missing path | Compile-time or request-time failure is clear. |
| Protected field without role | Field is absent or redacted. |
| Tenant-scoped projection | Only allowed rows are returned. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Asserting only status | Field mapping can still be wrong. | Assert response values. |
| Testing only full response DTOs | Projection route can differ. | Call projection-specific endpoints. |
| Skipping security scenarios | Projection can leak protected fields. | Test restricted principals. |

## Troubleshooting

If projection tests fail only in CI, check database ordering and add explicit sort parameters.

## Related documentation

- [Projection Paths](projection-paths.md)
- [Security Interaction](security-interaction.md)
- [Testing Architecture](../../architecture/testing-architecture.md)
