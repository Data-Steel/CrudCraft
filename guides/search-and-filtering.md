---
title: Searching & Filtering
summary: Use generated SearchRequest objects to filter, sort, and page through resources.
sidebar: guides
---

# Searching & Filtering

List endpoints accept query parameters that map to a generated `SearchRequest`. Filters, ranges, and sorting are translated into a `Specification` or QueryDSL predicate.

## Binding Query Parameters

Each field annotated with `@Searchable` produces getters in `BookSearchRequest`. Parameters bind using property names:

```
GET /books?title=Spring
```

Collections use repeated parameters: `?author=Craig&author=Josh`.

## Composing Filters

`SearchRequest` combines individual filters with logical AND. Fields support type-aware operators:

- Strings: equals, contains (`title.contains=Cloud`)
- Numbers: equals, greaterThan, lessThan
- Dates: before, after, between (`publishedDate.after=2023-01-01`)

Multiple operators on the same field are combined.

## Nested Searches

When a relationship field is marked as `@Searchable`, you can filter by nested properties using bracket notation:

```
GET /posts?author[name]=John&author[nameOp]=EQUALS
```

This allows searching for posts by author properties. The `[nameOp]` parameter specifies the search operator (EQUALS, CONTAINS, etc.).

**Note**: CrudCraft automatically configures Tomcat to accept square brackets in query parameter names for nested searches. Only square brackets are allowed for security - query parameters are safely bound to SearchRequest DTOs and used in parameterized queries.

## Paging and Sorting

Pagination uses `page` and `size` parameters. Sorting uses `sort=property,asc|desc` and can be repeated.

```
GET /books?page=0&size=20&sort=title,asc&sort=publishedDate,desc
```

## Realistic Examples

1. Books with “Spring” in the title published after 2020, sorted by author:

```
GET /books?title.contains=Spring&publishedDate.after=2020-01-01&sort=author,asc
```

2. Books by multiple authors with paging:

```
GET /books?author=Craig&author=Joshua&page=1&size=10
```

3. Range query on page count:

```
GET /books?pageCount.between=100,400
```

4. Search for posts by nested author name:

```
GET /posts?author[name]=John%20Doe&author[nameOp]=EQUALS&limit=10
```

## Tips

- Omitted parameters are ignored; default paging applies.
- Use URL encoding for spaces and special characters.
- Validate queries with the `/books/bulk/validate` endpoint for complex criteria.

## Next Steps

- Learn about [Data Export](/guides/exporting.md) to download search results.
- Review [Endpoints & Bulk Operations](/guides/endpoints-and-bulk.md) for batch processing.
- Add security with [Security Overview](/guides/security/overview.md).

