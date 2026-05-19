---
title: "Runtime Architecture"
description: "Understand how generated CrudCraft services use runtime core, optional modules, adapters, extensions, and starters at request time."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-core"
  - "crudcraft-runtime-search"
  - "crudcraft-runtime-projection"
  - "crudcraft-runtime-export"
  - "crudcraft-runtime-security"
  - "crudcraft-runtime-extensions"
related:
  - "/architecture/system-overview"
  - "/architecture/module-boundaries"
  - "/feature-guides/runtime-modules"
---

# Runtime Architecture

Runtime modules are the request-time contracts generated code calls after the application has compiled. The generated source owns entity-specific HTTP and mapping shape; runtime modules own reusable behavior.

## Core Service Flow

Generated services extend:

```java
AbstractCrudService<T, U, R, F, ID>
```

The type parameters are entity, request DTO, response DTO, ref DTO, and id type. The generated constructor passes the repository, mapper, entity class, response DTO class, and ref DTO class.

For reads, `AbstractCrudService` composes:

```text
caller Specification
  + runtime extension readFilter(...)
  + id/search/keyset constraints
  -> QueryExecutionStrategy
  -> mapper or ProjectionAdapter
  -> runtime extension afterRead(...)
```

For writes, it uses:

```text
request DTO
  -> runtime extension beforeCreate(...) or beforeUpdate(...)
  -> mapper.fromRequest / mapper.update / mapper.patch
  -> runtime extension beforeSave(...)
  -> generated service preSave(...)
  -> repository.save(...)
  -> generated service postSave(...)
  -> mapper.toResponse(...)
  -> runtime extension afterRead(...)
```

Deletes load only visible entities, run runtime extension `beforeDelete(...)`, generated `preDelete(...)`, repository delete, then generated `postDelete(...)`.

## Optional Collaborators

`AbstractCrudService` implements `ApplicationContextAware` and resolves optional collaborators through `ServiceCollaborators`:

| Collaborator | How it is found | Effect |
|---|---|---|
| `CrudRuntimeExtension` beans | `ApplicationContext.getBeansOfType(CrudRuntimeExtension.class)` | Adds global read/write/read-filter hooks. |
| `ProjectionAdapter` bean | `ApplicationContext.getBean(ProjectionAdapter.class)` when present | Executes non-default projections without mapping full entities first. |

Generated services may also override `runtimeExtensions()` for model-specific runtime hooks. Row security generated from annotations uses that path: `ServiceGenerator` constructs `RowSecurityRuntimeExtension` with configured row handlers and generated `ClaimScopedRowSecurityHandler` instances.

## Runtime Modules

| Module | Public runtime role | Important classes |
|---|---|---|
| `crudcraft-runtime-core` | CRUD operations, bulk operations, patch/upsert, keyset pagination, response/error models, extension chain, projection adapter contract. | `AbstractCrudService`, `CrudService`, `CrudQueryOperations`, `KeysetPage`, `CrudRuntimeExtension`, `ProjectionAdapter`, `CrudCraftExceptionHandler` |
| `crudcraft-runtime-search` | Search request contract and validation before delegating to core query operations. | `SearchRequest`, `SearchOperations`, `CrudCraftSearchProperties` |
| `crudcraft-runtime-projection` | Metadata-backed JPA criteria projection execution and core adapter. | `ProjectionAutoConfiguration`, `ProjectionMetadataRegistry`, `JpaProjectionExecutor`, `JpaCriteriaProjectionAdapter` |
| `crudcraft-runtime-export` | Streaming export for DTO and entity modes. | `ExportRequest`, `ExportService`, `EntityExportService`, `EntityExportConfiguration` |
| `crudcraft-runtime-security` | Field filtering, row filtering/write guards, principal scope access, reusable endpoint policies. | `FieldSecurityRuntimeExtension`, `FieldSecurityUtil`, `RowSecurityRuntimeExtension`, `ClaimScopedRowSecurityHandler`, `PrincipalScopeAccessor` |
| `crudcraft-runtime-extensions` | Reusable embeddables and relationship helpers used by generated services. | `AuditableExtension`, `SoftDeleteExtension`, `RelationshipUtils` |

## Search Runtime

Generated search request classes implement the runtime `SearchRequest<T>` contract for `SearchOperations`. The search runtime validates:

- requested search paths against `allowedSearchPaths()`;
- operators per path against `allowedSearchOperators()`;
- sort fields against `allowedSortPaths()`.

After validation it delegates to `CrudQueryOperations.findAll(...)` or `findAllKeyset(...)`. Core still applies row filters, projection adapters, and `afterRead(...)` filters.

`AbstractCrudService` also has a reflection fallback for generated search request classes exposing `toSpecification()` without forcing runtime-core to depend on runtime-search. That keeps core usable without the search module.

## Projection Runtime

Projection support is optional. When installed, `ProjectionAutoConfiguration` creates:

1. a `ProjectionMetadataRegistry`, usually from generated `GeneratedProjectionMetadataRegistry`;
2. a metadata-aware criteria builder;
3. a `JpaProjectionExecutor` when an `EntityManager` bean exists;
4. a primary `ProjectionAdapter` used by runtime-core.

If no registry exists, the projection runtime falls back to the configured `crudcraft.projection.registry-fqcn`. `none` or `noop` intentionally selects an empty registry.

Field security can participate in projection because `ProjectionAutoConfiguration` accepts an optional `FieldSecurityAdapter`.

## Export Runtime

Generated export endpoints construct `ExportService` with controller-local limits:

- `crudcraft.export.max-csv-rows`, default `100000`;
- `crudcraft.export.max-json-rows`, default `50000`;
- `crudcraft.export.max-xlsx-rows`, default `25000`;
- `crudcraft.api.max-page-size`, default `100`.

`ExportService` validates the format (`csv`, `json`, `xlsx`), rejects negative limits, returns a
valid empty stream for limit `0`, pages through results, and applies the supplied security filter to
every DTO. JSON is written incrementally. CSV and XLSX derive a stable union header from the
flattened rows before writing, so they may buffer flattened rows up to the configured export limit.

## Security Runtime

Endpoint authorization is generated into controller methods with Spring Security annotations. Runtime security then works through extensions:

- `FieldSecurityRuntimeExtension` calls `FieldSecurityAdapter.filterWrite(...)` before mapping writes and `filterRead(...)` after reading DTOs.
- `RowSecurityRuntimeExtension` composes `RowSecurityHandler.rowFilter()` into read specifications and calls `RowSecurityHandler.apply(...)` before save/delete.
- `ClaimScopedRowSecurityHandler` bridges built-in tenant/client/owner scopes to the current principal through `PrincipalScopeAccessor`.

Custom controllers and custom services outside the generated service path must call the same contracts explicitly if they expose protected data.

## Extension Runtime

`crudcraft-runtime-extensions` is not a generic business logic bucket. It contains reusable runtime pieces CrudCraft-generated services rely on:

- `RelationshipUtils.fixBidirectional(entity)` from generated `postSave(...)`;
- `RelationshipUtils.clearBidirectional(entity)` from generated `preDelete(...)`;
- `AuditableExtension` and `SoftDeleteExtension` embeddables for common JPA model concerns.

Application-specific behavior belongs in application-owned services, editable stubs, or documented runtime extension beans.

## Related Documentation

- [Runtime Modules](../feature-guides/runtime-modules/)
- [Module Boundaries](module-boundaries.md)
- [Extension Points](extension-points.md)
