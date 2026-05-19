---
title: "Search Operators"
description: "Choose supported CrudCraft search operators for strings, numbers, dates, collections, maps, and sizes."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/filtering"
  - "/feature-guides/search"
  - "/architecture/contract-model"
---

# Search Operators

Search operators define how generated search requests compare request values to entity fields.

Use this page when deciding which operations a searchable field should support.

## Who this page is for

This page is for developers designing query behavior for generated APIs.

## When to use this page

Use this page before exposing a field through `@Searchable`.

## When not to use this page

Do not use this page for pagination or sorting behavior.

## Prerequisites

- Search runtime is configured.
- The field type is known.
- The query behavior is safe to expose.

## Quick example

```java
@Searchable(operators = {SearchOperator.EQUALS, SearchOperator.CONTAINS})
private String name;
```

Expected result: generated search supports exact and contains matching for `name`.

## Predicate logic

Generated search requests combine multiple active predicates left-to-right with `SearchLogic`.
`OR` matches when any predicate matches, so three fields are evaluated as
`(F1 = ?) OR (F2 = ?) OR (F3 = ?)`. `AND` requires every predicate to match and evaluates the same
fields as `(F1 = ?) AND (F2 = ?) AND (F3 = ?)`.

## Operator groups

| Group | Typical use |
|---|---|
| Equality | Exact matching and exclusion. |
| Text | Contains, starts-with, ends-with, or pattern-like matching when supported. |
| Range | Greater-than, less-than, and between comparisons. |
| Collection | Contains or size checks on supported collection fields. |
| Map | Key or value checks on supported map fields. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Enabling broad text operators on large fields | Queries may be expensive. | Limit operators to real use cases. |
| Allowing unsupported operators for a type | Generation or runtime validation may fail. | Match operators to field type. |
| Treating regex as harmless | Regex can be expensive or unsafe. | Avoid unless explicitly required and tested. |

## Troubleshooting

If an operator is rejected, check the generated search request and the Search for supported type/operator combinations.

## Related documentation

- [Filtering](filtering.md)
- [Search](../search/)
- [Contract Model](../../architecture/contract-model.md)
