---
title: "Search Runtime Module"
description: "Use the CrudCraft search runtime module for generated filtering and search request execution."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
  - "crudcraft-spring-boot-starter-search"
related:
  - "/feature-guides/search"
  - "/feature-guides/search"
  - "/architecture/runtime-architecture"
---

# Search Runtime Module

The search runtime module converts generated search requests into runtime query behavior.

Use this page when generated APIs include searchable fields.

## Who this page is for

This page is for developers adding search runtime support.

## When to use this page

Use this page after adding `@Searchable` fields.

## When not to use this page

Do not add search runtime if no generated API uses search.

## Prerequisites

- Core runtime is available.
- Search fields are annotated intentionally.
- Generated search artifacts exist.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-search</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated search endpoints can execute supported filters.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Runtime without searchable fields | Nothing useful is generated. | Add `@Searchable` where needed. |
| Search fields without runtime | Generated search code may lack runtime support. | Add the search starter. |
| Treating search as security | Search only narrows query results. | Use security runtime for access control. |

## Related documentation

- [Search Guides](../search/)
- [Search](../search/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
