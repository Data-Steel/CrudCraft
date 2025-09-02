---
title: DTO Design & Mapping
summary: Understand the generated DTO variants and how MapStruct maps entities to them.
sidebar: concepts
---

# DTO Design & Mapping

CrudCraft generates multiple DTO types to match different use cases. MapStruct mappers convert between entities and these DTOs using compile-time mapping.

## DTO Variants

| DTO | Purpose |
|-----|---------|
| `BookRequestDto` | Input for create, replace, and patch operations |
| `BookResponseDto` | Full representation returned by endpoints |
| `BookRef` | Lightweight reference containing only ID and label fields |
| `BookListDto` | Optional list projection for collections |

## Mapping Strategy

Generated mappers extend `CrudMapper` and rely on MapStruct for field mapping. ID fields are copied directly; complex relations use nested mappers or reference DTOs.

```java
@Mapper(componentModel = "spring")
public interface BookMapper extends CrudMapper<Book, BookResponseDto, BookRequestDto> {
  AuthorRef toRef(Author entity);
}
```

## ID Mapping

- Entity IDs are included in response and reference DTOs.
- Request DTOs omit generated IDs unless marked with `@Request(id = true)` for upserts.

## Nested Mappers

When an entity references another `@CrudCrafted` entity, the mapper uses the referenced mapper to convert nested DTOs. This keeps mapping logic isolated per aggregate.

## Reference DTO Philosophy

Reference DTOs (`BookRef`) provide a compact representation for relations, reducing over-fetching and preventing circular serialization. They typically include only the ID and a human-readable label.

## Next Steps

- Explore relational nuances in [Relationship Management](/concepts/relationship-management.md).
- Learn how search queries are built in [Search Design](/concepts/search-design.md).
- Add custom mapping logic in [Editable Stubs & Customization](/guides/editable-stubs.md).

