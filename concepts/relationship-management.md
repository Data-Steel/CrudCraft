---
title: Relationship Management
summary: Manage entity associations using generated metadata and utility helpers.
sidebar: concepts
---

# Relationship Management

CrudCraft assists with maintaining JPA relationships through generated metadata and utilities.

## RelationshipMeta

For each association, `RelationshipMeta` describes cardinality and owning side. Generated mappers and services consult this metadata to decide how to handle references.

```java
RelationshipMeta meta = RelationshipMeta.manyToOne("author");
```

## RelationshipUtils

`RelationshipUtils.fix` sets up bidirectional links when mapping DTOs to entities, ensuring both sides are synchronized:

```java
RelationshipUtils.fix(book.getAuthor(), book);
```

`RelationshipUtils.clear` removes associations, useful when deleting or reassigning relationships to avoid orphaned references.

## Bidirectional Integrity

CrudCraft relies on JPA cascades for persistence but uses `RelationshipUtils` to maintain in-memory consistency. When using `orphanRemoval = true`, clearing relationships ensures proper deletion of child entities.

## Example

```java
public void assignAuthor(Book book, Author author) {
  book.setAuthor(author);
  RelationshipUtils.fix(author, book);
}
```

## Next Steps

- Learn mapping strategies in [DTO Design & Mapping](/concepts/dto-mapping.md).
- Investigate query construction in [Search Design](/concepts/search-design.md).
- Implement custom logic with [Extensibility & Hooks](/concepts/extensibility.md).

