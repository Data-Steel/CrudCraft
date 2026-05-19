---
title: "Core Runtime Module"
description: "Use the CrudCraft core runtime module required by generated CRUD APIs."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
  - "crudcraft-spring-boot-starter-core"
related:
  - "/feature-guides/runtime-modules/configuration"
  - "/feature-guides/runtime-modules"
  - "/architecture/runtime-architecture"
---

# Core Runtime Module

The core runtime module provides the baseline services, response models, pagination support, and exception handling used by generated CRUD APIs.

Use this page when setting up the smallest generated CrudCraft API.

## Who this page is for

This page is for developers who want generated CRUD endpoints without optional capabilities.

## When to use this page

Use this page for every CrudCraft application.

## When not to use this page

Do not use core alone when generated code uses search, export, projection, or security runtime contracts.

## Prerequisites

- A Spring Boot application.
- `crudcraft-api` and `crudcraft-codegen` configured for generation.
- One or more `@CrudCrafted` entities.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-core</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated basic CRUD controllers and services can start.

## What core does

Core supports generated CRUD service behavior, shared response models, pagination, exception translation, and extension-chain contracts used by generated code.

## What core does not do

Core does not provide search, projection, export, or security runtime behavior by itself.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Omitting core | Generated CRUD code lacks baseline runtime contracts. | Add the core starter or umbrella starter. |
| Expecting search from core | Search is optional. | Add the search starter. |
| Mixing versions | Generated code and runtime can mismatch. | Align all CrudCraft versions. |

## Related documentation

- [Configuration](configuration.md)
- [Runtime Modules](../runtime-modules/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
