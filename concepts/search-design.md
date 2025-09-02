---
title: Search Design
summary: Internal design of SearchRequest and how CrudCraft builds database queries.
sidebar: concepts
---

# Search Design

CrudCraft translates HTTP parameters into database queries through a `SearchRequest` abstraction. This section outlines the internals and extension points.

## SearchRequest Contract

Each entity with `@Searchable` fields receives a `SearchRequest` class. The contract exposes type-safe getters for filter operators and paging information.

```java
public class BookSearchRequest extends BaseSearchRequest {
  private final SearchField<String> title = new SearchField<>();
  // ...
}
```

## Specification vs QueryDSL

By default, CrudCraft builds JPA `Specification` predicates. When QueryDSL is on the classpath, it creates Boolean expressions instead. Both implementations honor the same `SearchRequest` contract.

## HTTP Binding Strategy

Spring binds query parameters to the `SearchRequest` using property paths and operator suffixes (`title.contains`). This keeps controllers free from manual parsing logic.

## Trade-offs

- **Specification**: Works with standard JPA; limited to APIs supported by the criteria API.
- **QueryDSL**: Offers type-safe queries and advanced joins but requires generated Q-types.

## Extensibility

Custom operators can be added by extending `SearchField` and providing your own `Specification` or QueryDSL expression builder.

## Next Steps

- Implement filters in [Searching & Filtering](/guides/search-and-filtering.md).
- Tune performance with [Performance & Tuning](/guides/performance.md).
- Integrate security via [Security Design](/concepts/security-design.md).

