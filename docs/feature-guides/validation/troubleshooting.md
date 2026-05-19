---
title: "Validation Troubleshooting"
description: "Diagnose common CrudCraft validation problems in generated request DTOs, controller validation, and error handling."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/validation/jakarta-validation"
  - "/feature-guides/validation/testing"
  - "/feature-guides/validation"
---

# Validation Troubleshooting

Validation troubleshooting starts by checking constraints on the entity, generated request DTOs, controller validation, and error handling.

Use this page when invalid data is accepted or valid data is rejected.

## Who this page is for

This page is for developers diagnosing generated validation behavior.

## When to use this page

Use this page when validation errors are missing, unexpected, or reported with the wrong status.

## When not to use this page

Do not use this page to design domain validation rules from scratch.

## Prerequisites

- You can reproduce the request.
- You can inspect generated request DTOs.
- You know which field constraint should apply.

## Quick example

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"\"}"
```

Expected result: a blank title is rejected when the generated request field has `@NotBlank`.

## Troubleshooting table

| Symptom | Likely cause | Fix |
|---|---|---|
| Invalid data accepted | Constraint not copied or validation not triggered. | Inspect generated request DTO and controller. |
| Valid data rejected | Constraint is too strict or applied to wrong field. | Review entity annotations. |
| Error returns `500` | Exception handler is not translating validation failure. | Check runtime core exception handling. |
| Nested object not validated | Cascading validation is missing. | Use supported nested validation configuration. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Checking only entity source | Generated DTO may differ. | Inspect generated request source. |
| Forgetting clean generation | Stale generated code can hide changes. | Run `./mvnw clean compile`. |
| Treating database errors as API validation | The user gets late feedback. | Add request validation where appropriate. |

## Related documentation

- [Jakarta Validation](jakarta-validation.md)
- [Testing Validation](testing.md)
- [Validation](../validation/)
