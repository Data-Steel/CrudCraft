---
title: "Filtering"
description: "Call CrudCraft generated search endpoints with filters for allowed searchable fields."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/operators"
  - "/feature-guides/search/pagination"
  - "/feature-guides/search"
---

# Filtering

Filtering narrows generated search results by applying supported operators to searchable fields.

Use this page to call a generated search endpoint with filter parameters.

## Who this page is for

This page is for developers integrating generated search endpoints with clients.

## When to use this page

Use this page after search artifacts have been generated.

## When not to use this page

Do not use filtering to enforce authorization. Use security features for access rules.

## Prerequisites

- At least one field is annotated with `@Searchable`.
- The search starter is installed.
- The application has been regenerated.

## Quick example

```bash
curl -i "http://localhost:8080/api/customers/search?email.equals=ada@example.com"
```

Expected result: the response contains only customers whose email equals `ada@example.com`.

## Multiple filters

```bash
curl -i "http://localhost:8080/api/customers/search?name.contains=Ada&email.endsWith=example.com"
```

Generated search applies only allowed fields and operators.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Filtering on a non-searchable field | The generated request does not allow it. | Add `@Searchable` intentionally and regenerate. |
| Assuming parameter names without checking generated code | Names can depend on generated request shape. | Inspect the generated search request. |
| Sending unbounded broad filters | Large result sets can be expensive. | Combine filters with pagination and limits. |

## Troubleshooting

If a filter returns no data, test the same endpoint without filters and confirm the database contains matching values.

## Related documentation

- [Operators](operators.md)
- [Pagination](pagination.md)
- [Search](../search/)
