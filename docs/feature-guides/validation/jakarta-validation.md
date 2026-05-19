---
title: "Jakarta Validation"
description: "Use Jakarta Validation annotations with CrudCraft generated request DTOs."
section: "Feature Guides"
category: "Validation"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
related:
  - "/feature-guides/validation/request-validation"
  - "/feature-guides/validation"
  - "/architecture/codegen-architecture"
---

# Jakarta Validation

CrudCraft can copy supported Jakarta Validation annotations from entity fields to generated request models.

Use this page when field constraints should be enforced on generated API requests.

## Who this page is for

This page is for developers defining validation constraints on CrudCrafted entities.

## When to use this page

Use this page before relying on generated request validation.

## When not to use this page

Do not use this page for database constraints only. Database constraints and API validation are related but separate.

## Prerequisites

- The application uses Jakarta Validation.
- The field is included in generated request DTOs.
- The constraint is supported by CrudCraft generation.

## Quick example

```java
@Request
@NotBlank
@Size(max = 120)
private String title;
```

Expected result: generated request DTOs reject blank titles and titles longer than 120 characters.

## Realistic example

```java
@Request
@Email
@Size(max = 320)
private String email;
```

## Behavior

CrudCraft reads supported validation annotations during code generation and emits them on generated request model components or fields where applicable.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Placing constraints on fields not included in requests | The generated request DTO may not contain them. | Add `@Request` when clients can write the field. |
| Assuming all custom constraints are copied | Codegen may support only documented constraints. | Check Validation. |
| Relying only on database constraints | Users get late or unclear failures. | Add request validation for user-facing input. |

## Troubleshooting

If validation is missing from generated requests, inspect the generated DTO and confirm the annotation is supported and placed on a request field.

## Related documentation

- [Request Validation](request-validation.md)
- [Validation](../validation/)
- [Codegen Architecture](../../architecture/codegen-architecture.md)
