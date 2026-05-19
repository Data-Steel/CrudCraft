---
title: "Field Selection"
description: "Choose which fields generated CrudCraft projection responses should include."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-runtime-projection"
related:
  - "/feature-guides/projection/projection-paths"
  - "/feature-guides/projection/security-interaction"
  - "/feature-guides/projection"
---

# Field Selection

Field selection defines which values a projection response should return.

Use this page when a generated API should return a smaller read model than the default response DTO.

## Who this page is for

This page is for developers designing read models for generated APIs.

## When to use this page

Use this page when clients need a list or detail view with a specific field set.

## When not to use this page

Do not use field selection to expose fields that security rules should hide.

## Prerequisites

- Projection runtime is configured.
- The target fields exist on the entity or a supported nested path.
- The selected fields are safe to expose.

## Quick example

```java
public record CustomerListView(Long id, String name) {
}
```

Expected result: generated projection support can return a focused read model when configured for the entity.

## Realistic example

```java
public record CustomerSummary(
        Long id,
        String name,
        String email,
        String accountStatus) {
}
```

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Selecting too many fields | Projection loses its purpose. | Keep each projection tied to a view or workflow. |
| Exposing protected fields | Projection can leak sensitive data. | Apply field security rules and tests. |
| Duplicating full response DTOs | It adds maintenance without value. | Use the default response DTO when all fields are needed. |

## Troubleshooting

If a projection field is null unexpectedly, verify the field path and whether field security filtered it.

## Related documentation

- [Projection Paths](projection-paths.md)
- [Security Interaction](security-interaction.md)
- [Projection](../projection/)
