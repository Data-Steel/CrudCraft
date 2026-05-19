---
title: "Extensions Runtime Module"
description: "Use CrudCraft runtime extensions for optional reusable generated API behavior."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-extensions"
  - "crudcraft-spring-boot-starter-extensions"
related:
  - "/architecture/extension-points"
  - "/feature-guides/runtime-modules"
  - "/architecture/runtime-architecture"
---

# Extensions Runtime Module

The extensions runtime module contains optional reusable behavior that can support generated APIs.

Use this page when your generated API relies on documented extension behavior.

## Who this page is for

This page is for developers enabling optional runtime extensions.

## When to use this page

Use this page when a documented feature requires `crudcraft-runtime-extensions`.

## When not to use this page

Do not use this module as a place for application-specific business logic.

## Prerequisites

- Core runtime is available.
- The specific extension behavior is documented.
- Tests cover the extension path.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-extensions</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: documented extension runtime beans are available when auto-configuration conditions match.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Treating internals as extension APIs | Internals can change. | Use documented extension points. |
| Adding extensions without a feature need | It expands runtime surface unnecessarily. | Add only when required. |
| Skipping integration tests | Extension behavior is request-time behavior. | Test the observable endpoint result. |

## Related documentation

- [Extension Points](../../architecture/extension-points.md)
- [Runtime Modules](../runtime-modules/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
