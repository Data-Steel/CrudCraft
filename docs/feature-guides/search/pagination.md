---
title: "Pagination"
description: "Use pagination with CrudCraft generated list and search endpoints."
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
  - "/feature-guides/search/sorting"
  - "/architecture/runtime-architecture"
---

# Pagination

Pagination limits generated endpoint responses to manageable pages.

Use this page when generated list or search endpoints may return more than a small number of records.

## Who this page is for

This page is for developers building API clients or tuning generated endpoint behavior.

## When to use this page

Use this page when calling collection or search endpoints.

## When not to use this page

Do not use pagination to hide unauthorized records. Apply security rules before pagination.

## Prerequisites

- The endpoint accepts Spring Data `Pageable`.
- The client can send `page`, `size`, and optional `sort` parameters.

## Quick example

```bash
curl -i "http://localhost:8080/api/customers?page=0&size=25"
```

Expected result: the response returns the first page with at most 25 records.

## Search with pagination

```bash
curl -i "http://localhost:8080/api/customers/search?name.contains=A&page=1&size=10"
```

## Behavior

CrudCraft generated controllers may clamp page size to configured safety limits. Clients should not assume unlimited result sizes.

## Keyset Cursors

Keyset pagination returns a cursor for the next page when more rows are available. Treat this value
as opaque API state. The current implementation encodes the last sort value, the entity identifier,
and the sort direction into a Base64 transport token, but that layout is an internal detail and may
change between CrudCraft versions.

Clients should only replay a cursor exactly as returned for the same endpoint, filter set, sort, and
projection. Do not parse the cursor, store derived fields from it, or send it with a different query.
Malformed cursors and cursors from a different query are rejected with a bad-request response that
asks the client to restart pagination from the first page.

### Keyset guarantees

- Cursors are opaque, version-sensitive tokens; clients must not parse or construct them.
- Duplicate sort values are resolved with deterministic ID tie-breaking.
- Cursors are reusable across requests while query shape and sort remain identical.
- Cursors can become invalid after data mutations (for example deletions or sort-field updates);
  restart from the first page when this happens.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Requesting huge pages | Large responses can harm latency and memory use. | Use bounded page sizes. |
| Omitting sort for stable paging | Page contents can shift between requests. | Use deterministic sorting. |
| Treating page index as one-based | Spring Data page indexes are zero-based. | Use `page=0` for the first page. |
| Reusing a keyset cursor with a different filter or sort | The cursor identifies a position in one ordered result set only. | Restart from the first page when the query changes. |

## Troubleshooting

If the response size is smaller than requested, check whether CrudCraft or application configuration clamps maximum page size.

## Related documentation

- [Filtering](filtering.md)
- [Sorting](sorting.md)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
