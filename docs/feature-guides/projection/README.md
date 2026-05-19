---
title: "Projection"
description: "Use CrudCraft projection features to return focused read models from generated APIs."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-projection"
  - "crudcraft-spring-boot-starter-projection"
related:
  - "/feature-guides/projection"
  - "/architecture/runtime-architecture"
  - "/feature-guides/projection/configuration"
---

# Projection

CrudCraft projection features help generated APIs return focused read models instead of full response DTOs.

Use this page to choose the right projection guide.

## Who this page is for

This page is for developers who want generated APIs to return selected fields or specialized read models.

## When to use this page

Use this page when full response DTOs contain more data than a use case needs.

## When not to use this page

Do not use projection to bypass field security. Security rules still need to apply.

## Start here

| Need | Read |
|---|---|
| Enable projection runtime | [Configuration](configuration.md) |
| Choose returned fields | [Field Selection](field-selection.md) |
| Map nested paths | [Projection Paths](projection-paths.md) |
| Combine with security | [Security Interaction](security-interaction.md) |
| Test projections | [Testing](testing.md) |
| Diagnose failures | [Troubleshooting](troubleshooting.md) |

## Quick example

```java
@ProjectionField("customer.name")
private String customerName;
```

Expected result: the generated projection metadata maps `customerName` to `customer.name` when the path is valid.

## Null Collections and Nested Paths

Collection-valued projection fields are hydrated after the scalar projection query. When a parent
row has no child rows, generated mutable DTOs receive an empty collection when their accessor allows
hydration. DTOs that intentionally model the collection slot as nullable keep that nullable contract;
client code should not rely on `null` and empty collection having the same meaning.

Nested projection paths are validated at generation time against the entity model. If a nested
relationship or embedded path is optional, the projection value can be `null` even when the path is
valid. Treat `null` as “no related value for this row,” not as a projection engine failure.

Field security is evaluated before selecting projection attributes. A denied nested attribute is
omitted from the projection query and then redacted by generated DTO metadata, so nested projections
follow the same read rules as full response DTOs.

## Collection Hydration Performance

Collection-valued projection fields require a secondary hydration query after the scalar projection
query. CrudCraft logs a warning by default so the path is visible during testing. Set
`crudcraft.projection.warn-on-collection-hydration=false` when a reviewed endpoint intentionally
uses collection projections and has separate performance coverage.

## Related documentation

- [Projection](../projection/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
- [Configuration](configuration.md)
