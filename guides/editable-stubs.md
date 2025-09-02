---
title: Editable Stubs & Customization
summary: Learn which generated files can be modified and how to regenerate them safely.
sidebar: guides
---

# Editable Stubs & Customization

CrudCraft distinguishes between files that may be edited and those that are always regenerated. Understanding this distinction lets you add custom logic without losing changes on rebuild.

## Editable Stubs

When `@CrudCrafted(editable = true)` is applied, CrudCraft generates editable stubs for:

- Mapper interfaces or abstract classes
- Services extending `AbstractCrudService`
- Controllers extending `AbstractCrudController`

Stubs are generated once. On subsequent builds, existing files are left untouched.

### Example

```java
@Service
public class BookService extends AbstractCrudService<Book, UUID, BookResponseDto, BookRequestDto> {
  public BookService(BookRepository repository, BookMapper mapper) {
    super(repository, mapper);
  }

  public List<BookResponseDto> findByAuthor(String author) {
    return mapper.toResponse(repository.findByAuthor(author));
  }
}
```

## Regenerating Stubs

To recreate a stub, delete the file and recompile. CrudCraft will regenerate the base implementation.

## Non-editable Files

DTOs, repositories, and search requests are regenerated on every build and should not be modified. Custom changes belong in separate classes or layers.

## Do’s and Don’ts

| Do | Don’t |
|----|-------|
| Add helper methods in services and controllers | Edit generated DTOs or repositories |
| Extend controllers with extra endpoints | Rely on source modification to block regeneration |
| Keep custom code under version control | Delete stubs inadvertently without source control |

## Next Steps

- Review [Regeneration & Migrations](/guides/migration.md) for strategies when annotations change.
- Explore [Endpoints & Bulk Operations](/guides/endpoints-and-bulk.md) for default behavior.
- Secure customization with [Security Overview](/guides/security/overview.md).

