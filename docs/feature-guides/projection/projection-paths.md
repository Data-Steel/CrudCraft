---
title: "Projection Paths"
description: "Map CrudCraft projection fields to direct, embedded, or related entity paths."
section: "Feature Guides"
category: "Projection"
audience:
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-runtime-projection"
related:
  - "/feature-guides/projection/field-selection"
  - "/feature-guides/projection/testing"
  - "/architecture/codegen-architecture"
---

# Projection Paths

Projection paths map a projection field to a source property on the entity model.

Use this page when a projection field does not directly match an entity property.

## Who this page is for

This page is for developers mapping projection read models to entity fields.

## When to use this page

Use this page when projection fields refer to nested or renamed source properties.

## When not to use this page

Do not use projection paths to query arbitrary relationships. Use only documented and validated paths.

## Prerequisites

- The source path exists on the entity model.
- The path is readable by CrudCraft codegen.
- The selected path is safe and performant.

## Quick example

```java
@ProjectionField("customer.name")
private String customerName;
```

Expected result: `customerName` maps to the nested `customer.name` property.

## Behavior

CrudCraft validates projection paths during generation when possible. Invalid paths should fail at compile time instead of producing a runtime-only surprise.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Guessing nested path names | Generated metadata may reject the path. | Use actual Java property names. |
| Mapping through large collections casually | It can be expensive or ambiguous. | Prefer direct or well-bounded paths. |
| Ignoring security rules | Projection can still expose protected data. | Test with restricted principals. |

## Troubleshooting

If compilation fails on a projection path, inspect the entity getters and field names used in the path.

## Related documentation

- [Field Selection](field-selection.md)
- [Testing](testing.md)
- [Codegen Architecture](../../architecture/codegen-architecture.md)
