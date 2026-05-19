---
title: "Choose a Starter"
description: "Choose the CrudCraft Spring Boot starter that matches the first API you want to generate."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-core"
  - "crudcraft-spring-boot-starter"
related:
  - "/quick-start/generate-your-first-api"
  - "/feature-guides"
  - "/features"
---

# Choose a Starter

CrudCraft starters add the runtime support that generated Spring Boot APIs need.

Use this page to choose the smallest starter that supports your first generated API.

## Who this page is for

This page is for developers starting a new CrudCraft application or adding CrudCraft to an existing Spring Boot application.

## When to use this page

Use this page before adding dependencies to your project.

## When not to use this page

Do not use this page to compare every advanced runtime feature. Use Feature Guides when you need a full compatibility matrix.

## Goal

By the end of this page, you will know which CrudCraft starter to add first.

## Before you start

You need:

- A Spring Boot application.
- Maven dependency management configured for your project.
- A decision about whether your first API needs only CRUD or also optional features.

## Step 1: Start with core for the smallest API

Use `crudcraft-spring-boot-starter-core` when your first API only needs generated CRUD endpoints.

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-core</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Replace `${crudcraft.version}` with the CrudCraft version used by your project.

## Step 2: Use the umbrella starter for a first exploration

Use `crudcraft-spring-boot-starter` when you want all optional runtime modules available while learning.

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

The umbrella starter is convenient for a first trial. For production applications, prefer the smallest set of capability starters that your API actually uses.

## Step 3: Choose optional starters deliberately

| Need | Starter |
|---|---|
| Generated CRUD endpoints only | `crudcraft-spring-boot-starter-core` |
| Search requests and filtering | `crudcraft-spring-boot-starter-search` |
| CSV, JSON, or XLSX export | `crudcraft-spring-boot-starter-export` |
| Projection queries | `crudcraft-spring-boot-starter-projection` |
| Field, endpoint, or row security helpers | `crudcraft-spring-boot-starter-security` |
| Reusable runtime extensions | `crudcraft-spring-boot-starter-extensions` |
| First exploration with all features | `crudcraft-spring-boot-starter` |

## Expected result

You should have selected one of these paths:

- Core starter for the smallest generated CRUD API.
- Umbrella starter for a first exploration.
- Core starter plus one or more optional capability starters for a specific feature.

## Common mistakes

| Mistake | Why it causes problems | Correct approach |
|---|---|---|
| Adding every starter by default | It makes the runtime surface larger than the API needs. | Start with core, then add capability starters when the feature is used. |
| Adding a runtime starter but no annotation processor | Runtime support exists, but no code is generated. | Add the annotation processor in the next Quick Start page. |
| Mixing different CrudCraft versions | Generated code and runtime contracts can drift. | Use the same `${crudcraft.version}` for all CrudCraft artifacts. |

## Next step

Continue with [Generate Your First API](generate-your-first-api.md).

## Related documentation

- [Generate Your First API](generate-your-first-api.md)
- [Feature Guides](../feature-guides/)
- [CrudCraft Feature Map](../features.md)
