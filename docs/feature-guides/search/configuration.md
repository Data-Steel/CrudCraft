---
title: "Search Configuration"
description: "Configure CrudCraft search dependencies and searchable fields for generated APIs."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-search"
related:
  - "/feature-guides/search/operators"
  - "/feature-guides/search"
  - "/quick-start/enable-your-first-runtime-feature"
---

# Search Configuration

Search configuration enables generated search request models and runtime query execution.

Use this page to add search to a generated CrudCraft API.

## Who this page is for

This page is for developers enabling search in a Spring Boot application.

## When to use this page

Use this page before calling generated `/search` endpoints.

## When not to use this page

Do not use this page to choose all operator semantics. Use Operators for that.

## Prerequisites

- A generated CRUD API compiles.
- CrudCraft artifact versions are aligned.
- Fields intended for search are known.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-search</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

## Mark a field searchable

```java
@Dto
@Request
@Searchable
private String email;
```

Expected result: CrudCraft generates search metadata and request fields for `email`.

## Regenerate

```bash
./mvnw clean compile
```

## Depth Configuration

`crudcraft.search.depth` must be positive. Use `1` or `2` for most generated APIs; increase it only
after reviewing query plans and the model paths exposed to clients. Use `Integer.MAX_VALUE` only for
trusted internal tooling that has its own request limits.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Adding the starter without `@Searchable` | No useful search fields are generated. | Mark intended query fields. |
| Marking sensitive fields searchable | Query behavior can reveal data. | Review searchable fields with security requirements. |
| Mixing search runtime versions | Generated and runtime contracts can mismatch. | Use one `${crudcraft.version}`. |
| Setting `crudcraft.search.depth` to `0` or negative | Invalid depth now fails fast instead of disabling validation. | Set a positive depth. |

## Troubleshooting

If the search endpoint is missing, inspect the generated controller and confirm that at least one search-enabled field exists.

## Related documentation

- [Operators](operators.md)
- [Search](../search/)
- [Enable Your First Runtime Feature](../../quick-start/enable-your-first-runtime-feature.md)
