---
title: "Generated Errors"
description: "Understand how CrudCraft generated APIs report validation failures and bad requests."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/validation/request-validation"
  - "/architecture/error-model"
  - "/feature-guides/validation"
---

# Generated Errors

Generated validation errors explain why a request body or request parameter is invalid.

Use this page when clients need to handle validation failures from CrudCraft generated endpoints.

## Who this page is for

This page is for developers building clients or backend integrations for generated APIs.

## When to use this page

Use this page when documenting or testing invalid request behavior.

## When not to use this page

Do not use this page for not-found or security failures. Use Error Model and Security guides for those cases.

## Prerequisites

- Request validation is configured.
- The generated endpoint runs through the shared exception handling path.

## Quick example

```json
{
  "title": ""
}
```

Expected result: a generated create endpoint rejects the request when `title` has `@NotBlank`.

## Behavior

CrudCraft generated APIs should report invalid input as a bad request, while missing resources should use not-found behavior and security failures should use `401` or `403`.

## Field Paths

Request DTO validation errors include the Spring field path when it is available. Nested and indexed
paths use Spring's usual notation, for example `customer.email` or `items[0].quantity`. Clients can
display these paths directly or map them to UI fields.

If a validation exception does not expose field-level metadata, CrudCraft falls back to the original
exception message and then to `Invalid request`. Generated field paths are therefore best-effort and
depend on the validation source.

## Error categories

| Error | Meaning |
|---|---|
| Validation failure | Request data violates constraints. |
| Bad request | Request shape or parameter value is invalid. |
| Not found | The target record does not exist. |
| Unauthorized | No valid principal is present. |
| Forbidden | The principal is not allowed to perform the operation. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Treating validation as `500` | Invalid user input is not a server crash. | Return or assert bad-request behavior. |
| Hiding field names from client docs | Clients cannot fix the request. | Document validation constraints. |
| Mixing security and validation failures | It confuses clients and tests. | Keep response semantics distinct. |

## Troubleshooting

If invalid input returns `500`, inspect the exception handler configuration and generated controller validation annotations.

## Related documentation

- [Request Validation](request-validation.md)
- [Error Model](../../architecture/error-model.md)
- [Validation](../validation/)
