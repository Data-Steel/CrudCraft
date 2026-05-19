---
title: "Sorting"
description: "Sort CrudCraft generated list and search results with Spring Data pageable parameters."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/filtering"
  - "/feature-guides/search/pagination"
  - "/feature-guides/search"
---

# Sorting

Sorting orders generated list or search results through Spring Data pageable parameters.

Use this page when clients need predictable result order.

## Who this page is for

This page is for developers building clients for generated endpoints.

## When to use this page

Use this page when result order matters for display, pagination, or repeatable tests.

## When not to use this page

Do not use sorting as a substitute for filtering or authorization.

## Prerequisites

- The endpoint accepts `Pageable`.
- The sort field is safe and supported by the generated API.
- The database can sort the field efficiently enough for the use case.

## Quick example

```bash
curl -i "http://localhost:8080/api/customers?page=0&size=20&sort=name,asc"
```

Expected result: results are ordered by `name` ascending.

## Search with sorting

```bash
curl -i "http://localhost:8080/api/customers/search?name.contains=A&sort=email,desc"
```

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Relying on default database order | Order can change between requests. | Send an explicit `sort` parameter. |
| Sorting by a sensitive or unsupported path | It can reveal or fail on fields not meant for clients. | Restrict sortable fields according to API design. |
| Sorting large unindexed columns | Queries can become slow. | Add indexes or choose safer sort fields. |

## Troubleshooting

If sorting fails, inspect the generated controller and repository path to confirm that the requested property exists and is allowed.

## Related documentation

- [Filtering](filtering.md)
- [Pagination](pagination.md)
- [Search](../search/)
