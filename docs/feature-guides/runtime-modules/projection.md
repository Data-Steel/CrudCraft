---
title: "Projection Runtime Module"
description: "Use the CrudCraft projection runtime module for focused generated read models."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-projection"
  - "crudcraft-spring-boot-starter-projection"
related:
  - "/feature-guides/projection"
  - "/feature-guides/projection"
  - "/architecture/runtime-architecture"
---

# Projection Runtime Module

The projection runtime module executes generated projection metadata and focused read models.

Use this page when generated APIs return projection-specific responses.

## Who this page is for

This page is for developers adding projection support to generated APIs.

## When to use this page

Use this page when projection metadata or projection endpoints are generated.

## When not to use this page

Do not use projection runtime to filter unauthorized data.

## Prerequisites

- Core runtime is available.
- Projection metadata exists.
- Projection paths are valid.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-projection</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated projection endpoints can use projection runtime execution.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Adding runtime without generated metadata | No projection behavior is available. | Generate projection metadata first. |
| Bypassing field security | Projection can still expose protected data. | Test projection with security rules. |
| Using projection for every response | It adds unnecessary complexity. | Use full DTOs when they fit the use case. |

## Related documentation

- [Projection Guides](../projection/)
- [Projection](../projection/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
