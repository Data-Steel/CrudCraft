---
title: "Testing Search"
description: "Test CrudCraft generated search fields, operators, sorting, pagination, and nested paths."
section: "Feature Guides"
category: "Search"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-search"
related:
  - "/feature-guides/search/filtering"
  - "/feature-guides/search/nested-fields"
  - "/architecture/testing-architecture"
---

# Testing Search

Search tests prove generated query behavior returns the intended records and rejects unsupported input.

Use this page when adding or changing searchable fields, operators, sorting, pagination, or nested paths.

## Who this page is for

This page is for developers writing tests for generated search behavior.

## When to use this page

Use this page whenever `@Searchable` annotations or search runtime behavior changes.

## When not to use this page

Do not rely on repository unit tests alone for generated HTTP search behavior.

## Prerequisites

- The generated search endpoint starts in a test context.
- Test data includes matching and non-matching records.
- The test asserts both response status and response contents.

## Quick example

```java
mockMvc.perform(get("/api/customers/search")
        .param("email.equals", "ada@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].email").value("ada@example.com"));
```

## Required cases

| Case | Expected result |
|---|---|
| Allowed field and operator | Matching records are returned. |
| Unsupported field | Request is rejected. |
| Unsupported operator | Request is rejected. |
| Pagination | Page size and index are respected or clamped. |
| Sorting | Result order is deterministic. |
| Nested path | Only documented nested paths are accepted. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Testing only one record | False positives are likely. | Seed matching and non-matching records. |
| Ignoring rejected requests | Unsupported input may be accepted by mistake. | Test invalid fields and operators. |
| Skipping sort assertions | Paging can become unstable. | Assert deterministic order where relevant. |

## Troubleshooting

If a search test is flaky, add explicit sorting and use stable test data.

## Related documentation

- [Filtering](filtering.md)
- [Nested Fields](nested-fields.md)
- [Testing Architecture](../../architecture/testing-architecture.md)
