---
title: "Security Interaction"
description: "Understand how CrudCraft projection output should interact with field security and row isolation."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-projection"
  - "crudcraft-runtime-security"
related:
  - "/feature-guides/security/field-level-security"
  - "/feature-guides/projection/testing"
  - "/architecture/security-model"
---

# Security Interaction

Projection output must respect the same security decisions as full generated responses.

Use this page when combining projection with field security, row isolation, or endpoint authorization.

## Who this page is for

This page is for developers exposing projected read models from secured generated APIs.

## When to use this page

Use this page before exposing projections that include protected, tenant-scoped, or role-specific data.

## When not to use this page

Do not use projection as a way to avoid security configuration.

## Prerequisites

- Security behavior is defined for the entity.
- Projection fields are known.
- Tests can run with allowed and denied principals.

## Quick example

```java
@FieldSecurity(readRoles = "ADMIN")
private String internalNotes;
```

Expected result: a projection that includes `internalNotes` must not expose it to non-admin callers.

## Behavior

Projection reduces field shape. It does not remove the need for endpoint authorization, row isolation, or field filtering.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Assuming projection is automatically safe | A smaller DTO can still contain sensitive fields. | Review each projected field. |
| Testing projection only as admin | Restricted users may see leaked fields. | Test with denied and allowed roles. |
| Applying row isolation after projection query | Rows may be selected before scope restrictions. | Apply row restrictions before projection execution. |

## Troubleshooting

If restricted users see protected projection fields, confirm the projection path uses the same field-security metadata as the full response path.

## Related documentation

- [Field-level Security](../security/field-level-security.md)
- [Testing](testing.md)
- [Security Model](../../architecture/security-model.md)
