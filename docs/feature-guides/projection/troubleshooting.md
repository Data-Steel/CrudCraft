---
title: "Projection Troubleshooting"
description: "Diagnose common CrudCraft projection failures in generated metadata, field paths, runtime execution, and security filtering."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-projection"
related:
  - "/feature-guides/projection/configuration"
  - "/feature-guides/projection/testing"
  - "/feature-guides/projection"
---

# Projection Troubleshooting

Projection troubleshooting starts with generated metadata, path validity, runtime configuration, and security filtering.

Use this page when projected responses are missing, invalid, or contain unexpected data.

## Who this page is for

This page is for developers diagnosing generated projection behavior.

## When to use this page

Use this page when projection endpoints fail, fields are null, or output shape is wrong.

## When not to use this page

Do not use this page to design projection models from scratch. Start with Field Selection.

## Prerequisites

- The project compiles.
- You can inspect generated projection metadata.
- You know the expected projection response shape.

## Quick example

```bash
curl -i "http://localhost:8080/api/customers/list"
```

Expected result: the endpoint returns the configured projected fields.

## Troubleshooting table

| Symptom | Likely cause | Fix |
|---|---|---|
| Projection endpoint missing | Projection was not generated or starter missing. | Check generation configuration and dependency setup. |
| Field is always null | Projection path is wrong or source value is null. | Validate path and test data. |
| Compile error on path | Path cannot be resolved. | Use actual Java property names. |
| Protected field appears | Security filtering is not applied. | Review security interaction and tests. |
| Slow projection query | Nested path or collection path is expensive. | Simplify projection or tune database indexes. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Debugging runtime before codegen | Metadata may already be wrong. | Inspect generated projection metadata first. |
| Ignoring default sorting | Tests may be unstable. | Add explicit sort for paged projections. |
| Using projection to hide sensitive fields | Projection is not a security policy. | Configure field security. |

## Related documentation

- [Projection Configuration](configuration.md)
- [Testing Projections](testing.md)
- [Projection](../projection/)
