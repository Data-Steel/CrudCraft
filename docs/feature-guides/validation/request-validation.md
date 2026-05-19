---
title: "Request Validation"
description: "Validate CrudCraft generated request DTOs before generated mutation operations execute."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/validation/generated-errors"
  - "/feature-guides/validation"
  - "/architecture/error-model"
---

# Request Validation

Generated request validation rejects invalid create, update, patch, and other write requests before mutation logic persists data.

Use this page when generated endpoints accept request bodies.

## Who this page is for

This page is for developers validating data submitted to generated APIs.

## When to use this page

Use this page after adding Jakarta Validation annotations to request fields.

## When not to use this page

Do not use request validation for access control or tenant isolation.

## Prerequisites

- Validation annotations are present on request fields.
- Spring validation is available in the application.
- Generated controller methods validate request DTOs.

## Quick example

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"\"}"
```

Expected result: the request is rejected because `title` is blank.

## Valid request example

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Effective CrudCraft\"}"
```

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Validating only create | Update and patch can also accept invalid data. | Test every write endpoint. |
| Treating null and blank as the same | Constraints have different semantics. | Use `@NotNull`, `@NotBlank`, or `@NotEmpty` intentionally. |
| Forgetting nested validation | Embedded request objects may need cascading validation. | Use supported nested validation patterns. |

## Troubleshooting

If invalid data is accepted, confirm the generated controller validates the request body and the generated request DTO contains the constraint.

## Related documentation

- [Generated Errors](generated-errors.md)
- [Validation](../validation/)
- [Error Model](../../architecture/error-model.md)
