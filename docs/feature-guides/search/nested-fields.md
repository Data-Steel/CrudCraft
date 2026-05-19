---
title: "Nested Fields"
description: "Configure CrudCraft searchable paths across related or embedded fields without exposing unbounded joins."
section: "Feature Guides"
category: "Search"
audience:
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/operators"
  - "/feature-guides/search/testing"
  - "/feature-guides/search"
---

# Nested Fields

Nested field search lets generated APIs filter by selected related or embedded properties.

Use this page when a query needs a path such as `customer.name` instead of only a direct field.

## Who this page is for

This page is for developers designing search over relationships or embedded value objects.

## When to use this page

Use this page when a direct field is not enough for a search use case.

## When not to use this page

Do not expose deep nested search paths without performance and security review.

## Prerequisites

- The relationship or embedded field is modeled in the entity.
- The target path is safe to expose.
- The allowed depth is intentional.

## Quick example

```java
@Searchable(depth = 1)
private Customer customer;
```

Expected result: generated search can allow paths one level below `customer`, such as `customer.name`, when supported by the model.

## Behavior

CrudCraft bounds nested search paths so generated APIs do not create unbounded joins from arbitrary request parameters.

Generated search metadata skips cyclical `@Searchable` paths when a relationship graph revisits a
type already on the current traversal path. Runtime programmatic builders also reject dotted paths
that revisit the same path segment, such as `posts.author.posts`, before a specification can be
executed. Prefer explicit shallow search fields for recursive schemas and add deeper paths only
after reviewing the resulting SQL.

## Performance and Configuration

Each additional depth level can multiply joins. For most schemas, keep `crudcraft.search.depth` at
`1` or `2`; use `3` only for endpoints with measured query plans and indexes that match the nested
filters. Avoid higher depths on many-to-many or collection-heavy graphs because they can produce
large intermediate result sets and require `distinct` cleanup.

`crudcraft.search.depth` must be positive. CrudCraft rejects `0` and negative values at
configuration time or when path validation runs, because those values would otherwise disable the
depth guard.

Profile representative filters with SQL logging or database query plans before increasing depth.
Projection is usually a better way to reduce response shape; search depth should model only the
fields clients need to filter or sort by.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Setting depth too high | Queries can become expensive or expose too much model structure. | Start with the smallest required depth. |
| Searching through sensitive relationships | Path names and values may reveal protected data. | Review nested paths with security requirements. |
| Assuming all nested paths are valid | Some relationships or field types may be unsupported. | Check generated search metadata. |

## Troubleshooting

If a nested filter is rejected, inspect the generated search request for the allowed path list.

## Related documentation

- [Operators](operators.md)
- [Testing](testing.md)
- [Search](../search/)
