# Nullability Contract

CrudCraft uses explicit nullness annotations on runtime public APIs and generated sources where the contract is stable.

## Services

Generated services are singleton Spring beans. Service method parameters are non-null unless documented otherwise. `findById` throws `ResourceNotFoundException` when no visible entity exists; `findByIdOptional` returns `Optional.empty()`. Collection-returning methods return empty collections, never `null`.

## Request DTOs

Request DTO instances passed to generated controllers and services are non-null. Individual fields follow Jakarta validation and CrudCraft DTO annotations. A field annotated with `@NotNull` is required by validation. A nullable field may be absent in PATCH semantics and may be set to `null` in PUT semantics when the mapper allows it.

## Response DTOs

Generated response IDs are non-null after persistence. Other fields may be null when the database value is null, the field was not part of a projection, or field-level security redacted it.

## Search, Projection, Export

Search request objects are mutable command objects and may expose empty metadata collections. Null search requests mean no filter. Export include/exclude sets are normalized: null means unspecified, empty means no paths. Projection metadata must be non-null for generated projection DTOs; invalid or cyclical metadata fails before query execution.
