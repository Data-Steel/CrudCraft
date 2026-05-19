---
title: "Search Troubleshooting"
description: "Diagnose common CrudCraft search problems in generated request models, operators, filters, sorting, pagination, and nested paths."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/configuration"
  - "/feature-guides/search/testing"
  - "/feature-guides/search"
---

# Search Troubleshooting

Search troubleshooting starts by checking generation, allowed fields, allowed operators, and runtime query behavior separately.

Use this page when generated search does not return the expected records.

## Who this page is for

This page is for developers diagnosing generated search endpoints.

## When to use this page

Use this page when a search endpoint is missing, rejects a request, returns empty results, or performs poorly.

## When not to use this page

Do not use this page to design new search behavior. Use the other search guides first.

## Prerequisites

- The application compiles.
- You can inspect generated search request source.
- You have a request URL that reproduces the issue.

## Quick example

```bash
curl -i "http://localhost:8080/api/customers/search?name.contains=Ada"
```

Expected result: the endpoint returns records whose generated search behavior matches `name.contains`.

## Troubleshooting table

| Symptom | Likely cause | Fix |
|---|---|---|
| Search endpoint missing | No searchable fields or search generation disabled. | Add `@Searchable` and regenerate. |
| Field rejected | Field is not allowed. | Check generated search request metadata. |
| Operator rejected | Operator is not supported for the field type. | Use a supported operator. |
| Empty results | Data does not match or filter path is wrong. | Test with a simpler exact filter. |
| Slow query | Broad operator, nested path, or missing index. | Reduce operators, depth, or add database indexes. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Debugging runtime before checking generated source | The request may be invalid by contract. | Inspect generated search artifacts first. |
| Assuming all DTO fields are searchable | Search is opt-in. | Add `@Searchable` only where intended. |
| Ignoring database indexes | Generated queries still run against real tables. | Index frequently searched fields. |

## Related documentation

- [Search Configuration](configuration.md)
- [Testing Search](testing.md)
- [Search](../search/)
