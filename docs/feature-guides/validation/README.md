---
title: "Validation"
description: "Use CrudCraft validation behavior to carry entity constraints into generated request models and API errors."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/validation"
  - "/architecture/error-model"
  - "/feature-guides/validation/jakarta-validation"
---

# Validation

CrudCraft validation support carries supported constraints into generated request models and exposes invalid requests through consistent API failures.

Use this page to choose the right validation guide.

## Who this page is for

This page is for developers who want generated APIs to reject invalid request data.

## When to use this page

Use this page when entity or request constraints affect generated DTOs and endpoint behavior.

## When not to use this page

Do not use validation as authorization. Validation checks data shape and business constraints, not caller permissions.

## Start here

| Need | Read |
|---|---|
| Copy Jakarta constraints | [Jakarta Validation](jakarta-validation.md) |
| Validate generated request DTOs | [Request Validation](request-validation.md) |
| Understand error responses | [Generated Errors](generated-errors.md) |
| Test invalid input | [Testing](testing.md) |
| Diagnose validation failures | [Troubleshooting](troubleshooting.md) |

## Quick example

```java
@Request
@NotBlank
private String name;
```

Expected result: generated request validation rejects blank `name` values.

## Related documentation

- [Validation](../validation/)
- [Error Model](../../architecture/error-model.md)
- [Jakarta Validation](jakarta-validation.md)
