---
title: Annotate Your Entities
summary: Use CrudCraft annotations to control generated CRUD APIs and DTOs.
sidebar: guides
---

# Annotate Your Entities

CrudCraft inspects JPA entities and generates API layers based on annotations. This guide explains the most important annotations for controlling exposure, input, and searchability.

## @CrudCrafted — Enable Generation

Apply `@CrudCrafted` at the entity class level to trigger code generation.

```java
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;

@Entity
@CrudCrafted
public class Book { }
```

Attributes such as `editable = true` create editable stubs for service, controller, and mapper classes. Stubs are generated once and can be customized.

## @Dto — Expose Fields

`@Dto` marks fields that should appear in response DTOs. The generated `BookResponseDto` contains any property flagged with `@Dto`.

```java
@Dto
private String title;
```

Use `@Dto(ref = true)` to generate a lightweight reference DTO for relationships.

## @Request — Accept Input

`@Request` indicates that clients may set a field via create or update requests. It influences the generated request DTO.

```java
@Request
private String author;
```

By default, a field with both `@Dto` and `@Request` is readable and writable.

## @Searchable — Filter and Sort

`@Searchable` exposes the field in the generated `BookSearchRequest` for filtering and sorting.

```java
@Searchable
private String genre;
```

The search request supports equals, contains, range, and sort operations depending on the field type.

## Validation

CrudCraft preserves Jakarta Validation annotations placed alongside CrudCraft annotations. Generated request DTOs carry constraints such as `@NotBlank` or `@Size`, and Spring validates them automatically.

```java
@NotBlank
@Dto
@Request
private String isbn;
```

## Cheat Sheet

| Annotation | Purpose | Common Attributes |
|------------|---------|-------------------|
| `@CrudCrafted` | Enables generation for the entity | `editable` |
| `@Dto` | Include field in response DTO | `ref` |
| `@Request` | Include field in request DTO | — |
| `@Searchable` | Expose field in search requests | — |
| Jakarta Validation | Enforce constraints | depends on annotation |

## Next Steps

- Review [Generated Layers](/guides/generated-layers.md) to see how annotations translate into code.
- Learn about [Endpoints & Bulk Operations](/guides/endpoints-and-bulk.md).
- Secure fields by reading [Security Overview](/guides/security/overview.md).

