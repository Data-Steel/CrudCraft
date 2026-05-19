---
title: "Search"
description: "Use CrudCraft search to generate query request models and searchable endpoints for selected entity fields."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
  - "crudcraft-spring-boot-starter-search"
related:
  - "/feature-guides/search"
  - "/architecture/runtime-architecture"
  - "/feature-guides/search/configuration"
---

# Search

CrudCraft search generates request models and runtime query support for fields explicitly marked as searchable.

Use this page to choose the right search guide.

## Who this page is for

This page is for developers who want generated APIs to support filtering, sorting, and searchable query paths.

## When to use this page

Use this page when adding search to a generated CRUD API.

## When not to use this page

Do not use search as a security boundary. Apply security rules separately.

## Start here

| Need | Read |
|---|---|
| Install search runtime | [Configuration](configuration.md) |
| Choose operators | [Operators](operators.md) |
| Filter records | [Filtering](filtering.md) |
| Sort results | [Sorting](sorting.md) |
| Page results | [Pagination](pagination.md) |
| Search related data | [Nested Fields](nested-fields.md) |
| Test search behavior | [Testing](testing.md) |
| Diagnose search failures | [Troubleshooting](troubleshooting.md) |

## Quick example

```java
@Dto
@Request
@Searchable
private String name;
```

Expected result: generated search artifacts include query support for `name`.

## Search Depth and Performance

Nested `@Searchable` paths are generated only up to the configured search
depth. Keep the depth as low as the API needs: every extra relationship hop can
add joins, widen the SQL result set, and increase the chance of duplicate rows
that require `distinct`.

Treat recursive entity graphs as opt-in. CrudCraft skips cyclical searchable
paths when it detects them during generation, and runtime search builders reject
cyclical path input with a bad-request error. Prefer explicit shallow fields for
hot endpoints and add deeper search paths only when tests cover the query plan.

## Related documentation

- [Search](../search/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
- [Configuration](configuration.md)
