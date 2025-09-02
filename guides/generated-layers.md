---
title: Generated Layers
summary: Discover the classes CrudCraft generates from an annotated entity and how they fit together.
sidebar: guides
---

# Generated Layers

When CrudCraft processes an entity, it emits a full set of CRUD building blocks. Understanding these components helps you navigate and extend your generated API.

## DTOs

For each entity, CrudCraft generates request and response DTOs plus a reference DTO used for relationships:

- `BookRequestDto`
- `BookResponseDto`
- `BookRef`

These DTOs mirror your `@Dto` and `@Request` annotations and carry Jakarta Validation constraints.

## Mapper

A MapStruct mapper converts between entity and DTO types. The generated mapper extends `CrudMapper` and is named after the entity:

```java
@Mapper
public interface BookMapper extends CrudMapper<Book, BookResponseDto, BookRequestDto> {}
```

If `editable = true`, the mapper is generated once as an interface you can implement or customize.

## Repository

CrudCraft produces a Spring Data repository extending `JpaRepository` and, when searching is enabled, `JpaSpecificationExecutor`.

```java
public interface BookRepository extends JpaRepository<Book, UUID> {}
```

Repositories are regenerated on every build and should not be edited.

## Service

The service orchestrates persistence and mapping. Generated services extend `AbstractCrudService`:

```java
@Service
public class BookService extends AbstractCrudService<Book, UUID, BookResponseDto, BookRequestDto> {
  public BookService(BookRepository repository, BookMapper mapper) {
    super(repository, mapper);
  }
}
```

Services become editable stubs when `editable = true`.

## Controller

REST endpoints live in a generated controller extending `AbstractCrudController`. It wires the service and exposes CRUD operations and search endpoints.

```java
@RestController
@RequestMapping("/books")
public class BookController extends AbstractCrudController<Book, UUID, BookResponseDto, BookRequestDto, BookSearchRequest> {}
```

Controllers are editable stubs when requested.

## SearchRequest

For entities with `@Searchable` fields, CrudCraft generates a `BookSearchRequest` that binds HTTP parameters to search criteria.

## Editable vs Regenerated Files

Editable stubs (mapper, service, controller) are generated once and then ignored if present on disk. To regenerate them, delete the file and recompile. Non-editable classes (DTOs, repository, search request) are overwritten on each build and should not be modified.

## Next Steps

- Learn about available [Endpoints & Bulk Operations](/guides/endpoints-and-bulk.md).
- Discover how to customize stubs in [Editable Stubs & Customization](/guides/editable-stubs.md).
- Read [Security Overview](/guides/security/overview.md) to secure generated layers.

