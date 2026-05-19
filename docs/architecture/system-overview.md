---
title: "System Overview"
description: "Understand how CrudCraft moves from annotated JPA entities to generated Spring Boot CRUD APIs and runtime module behavior."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/codegen-architecture"
  - "/architecture/runtime-architecture"
  - "/architecture/module-boundaries"
---

# System Overview

CrudCraft generates Spring Boot CRUD APIs from annotated JPA models. The important architecture decision is that generation and request-time behavior are separate:

```text
JPA entity + crudcraft-api annotations
  -> javac annotation processing
  -> ModelDescriptor and FieldDescriptor graph
  -> generated DTOs, mapper, repository, service, controller, metadata, search helpers
  -> compiled application
  -> runtime-core and optional runtime modules at request time
```

The generated source belongs to the application build. Runtime modules are normal dependencies used by that generated source.

## Module Families

| Module family | Main responsibility | Examples |
|---|---|---|
| API | Stable annotations and public contracts used in application source. | `@CrudCrafted`, `@Dto`, `@Request`, `@Searchable`, `@FieldSecurity`, `CrudEndpoint`, `CrudTemplate` |
| Codegen | Annotation processors, descriptor extraction, JavaPoet writers, generated metadata, Insomnia export generation. | `CrudCraftProcessor`, `ProjectionMetadataProcessor`, `WriterRegistry`, `Generator` |
| Generated application source | Entity-specific Spring API layer. | `BookRequestDto`, `BookResponseDto`, `BookMapper`, `BookRepository`, `BookService`, `BookController` |
| Runtime core | Shared CRUD service behavior, response models, exception translation, query/projection adapter contracts, extension chain. | `AbstractCrudService`, `CrudRuntimeExtension`, `PaginatedResponse`, `CrudCraftExceptionHandler` |
| Optional runtime modules | Capability-specific request-time behavior. | Search validation/execution, projection execution, export streaming, row/field security, reusable embeddables |
| Starters | Dependency composition and Spring Boot auto-configuration exposure. | `crudcraft-spring-boot-starter-core`, `crudcraft-spring-boot-starter`, feature starters |
| Sample app | Integrated generated API coverage against real Spring Boot, persistence, security, export, search, and projection paths. | `crudcraft-sample-app` |
| Tools | Repository maintenance and quality gate utilities. | `crudcraft-tools` |

## Compile-Time Flow

`CrudCraftProcessor` declares support for CrudCraft model/field annotations and `@Embeddable`, but the classes it turns into descriptors are `@CrudCrafted` classes and `@Embeddable` classes. For every processable class it calls `AnnotationModelReader.parse(...)`. The reader builds a `ModelDescriptor` from part extractors:

- `ModelIdentity`: name, package, base package, fields.
- `ModelFlags`: CRUD entity, editable, embeddable, abstract.
- `EndpointOptions`: template, include/omit endpoints, custom endpoint policy.
- `ModelSecurity`: secure flag, security policy, endpoint RBAC expressions, row handlers, built-in row scopes.

Each field becomes a `FieldDescriptor` composed from identity, DTO options, enum options, relationship data, validation metadata, search options, and security metadata.

`WriterRegistry` then dispatches built-in and service-loaded `Generator` implementations in stable order. Generators that do not require a CrudCraft entity also run for embeddables; CRUD generators only run when `ModelDescriptor.isCrudCraftEntity()` is true.

## Runtime Request Flow

For a generated REST endpoint, the normal request path is:

```text
HTTP request
  -> generated controller method
  -> generated service
  -> AbstractCrudService
  -> runtime extension read/write hooks
  -> repository, JPA specification, projection adapter, or export service
  -> mapper / projection result
  -> runtime extension read filters
  -> generated response
```

Generated controllers own the HTTP surface: route names, method mappings, request DTOs, status shape, OpenAPI annotations, controller metrics/logging, endpoint-specific `@PreAuthorize`, page-size clamping, multipart LOB parameters, search endpoints, bulk endpoints, validation endpoints, and export endpoints.

Runtime modules own reusable execution behavior. For example, `AbstractCrudService` applies row filters through `CrudRuntimeExtension.readFilter(...)`, applies field filtering through `afterRead(...)` and `beforeCreate(...)` / `beforeUpdate(...)`, delegates projections to an optional `ProjectionAdapter`, and validates keyset pagination.

## Optional Capability Composition

Optional features are intentionally not folded into core:

| Capability | Compile-time signal | Runtime path |
|---|---|---|
| Search | `@Searchable` fields generate search request/specification artifacts and enable `CrudEndpoint.SEARCH`. | `SearchOperations` validates allowed fields/operators/sorts and delegates to `CrudQueryOperations`. |
| Projection | DTO/projection annotations generate metadata registries. | `ProjectionAutoConfiguration` exposes a `ProjectionAdapter` backed by metadata and JPA criteria execution. |
| Export | `CrudEndpoint.EXPORT` from template, include list, or policy generates `/export`. | Generated controller creates `ExportService` with row limits and streams CSV, JSON, or XLSX. |
| Security | `secure`, `securityPolicy`, `@CrudSecurity`, `@EndpointRbac`, row scopes, and field security annotations alter generated code/metadata. | Spring Security checks endpoint expressions; runtime extensions apply row and field rules. |
| Extensions | Generated services call relationship hooks and runtime extension chain. | `crudcraft-runtime-extensions` provides reusable embeddables and relationship utilities. |

## What CrudCraft Does Not Own

CrudCraft does not choose a database schema strategy, authentication provider, token format, deployment topology, domain-specific authorization policy, or application business workflow. Generated code gives a consistent CRUD surface; the application still owns product-specific design.

## Related Documentation

- [Codegen Architecture](codegen-architecture.md)
- [Runtime Architecture](runtime-architecture.md)
- [Module Boundaries](module-boundaries.md)
