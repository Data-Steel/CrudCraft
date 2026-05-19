---
title: "Testing Validation"
description: "Test CrudCraft generated request validation and generated error behavior."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/validation/request-validation"
  - "/architecture/testing-architecture"
  - "/feature-guides/validation"
---

# Testing Validation

Validation tests prove generated APIs reject invalid request data before mutation logic changes state.

Use this page when adding constraints or changing generated request models.

## Who this page is for

This page is for developers writing tests for generated create, update, patch, or validation endpoints.

## When to use this page

Use this page whenever validation annotations or generated request DTOs change.

## When not to use this page

Do not rely only on entity unit tests for generated API validation.

## Prerequisites

- The generated endpoint starts in a test context.
- Test requests include valid and invalid payloads.
- The test asserts both status and response details when available.

## Quick example

```java
mockMvc.perform(post("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"title\":\"\"}"))
        .andExpect(status().isBadRequest());
```

Expected result: blank title is rejected.

## Required cases

| Case | Expected result |
|---|---|
| Missing required value | Bad request. |
| Blank text for `@NotBlank` | Bad request. |
| Oversized text for `@Size` | Bad request. |
| Invalid email | Bad request. |
| Valid payload | Successful mutation. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Testing only invalid data | The valid path may be broken. | Test valid and invalid payloads. |
| Asserting only status | Clients may depend on error details. | Assert response body when it is part of the contract. |
| Skipping patch validation | Partial updates can bypass constraints. | Test each generated write operation. |

## Troubleshooting

If a validation test passes locally but fails in CI, check whether generated source is stale and run a clean compile.

## Related documentation

- [Request Validation](request-validation.md)
- [Testing Architecture](../../architecture/testing-architecture.md)
- [Validation](../validation/)
