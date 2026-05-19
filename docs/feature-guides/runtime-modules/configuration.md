---
title: "Runtime Module Configuration"
description: "Configure CrudCraft runtime starters and keep runtime dependencies aligned with generated code."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
related:
  - "/feature-guides/runtime-modules/compatibility"
  - "/feature-guides/runtime-modules"
  - "/quick-start/choose-a-starter"
---

# Runtime Module Configuration

Runtime module configuration adds the Spring Boot starters that generated APIs need at request time.

Use this page when adding CrudCraft runtime dependencies.

## Who this page is for

This page is for developers wiring CrudCraft modules into a Spring Boot application.

## When to use this page

Use this page when a generated artifact imports runtime contracts or a feature guide requires a starter.

## When not to use this page

Do not use this page for annotation processor setup. Use Code Generation guides.

## Prerequisites

- You know which generated features are enabled.
- You know the CrudCraft version used by the project.
- Maven dependency management is available.

## Quick example

```xml
<properties>
    <crudcraft.version>2.0.0</crudcraft.version>
</properties>
```

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-core</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: all CrudCraft artifacts use the same version value.

## Add optional modules

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-search</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Mixing artifact versions | Runtime and generated contracts can drift. | Use one property for all CrudCraft versions. |
| Adding optional starters without generated features | It expands the app unnecessarily. | Add starters when features are enabled. |
| Missing required starter | Generated code may fail at runtime. | Match starters to generated imports. |

## Related documentation

- [Compatibility](compatibility.md)
- [Runtime Modules](../runtime-modules/)
- [Choose a Starter](../../quick-start/choose-a-starter.md)
