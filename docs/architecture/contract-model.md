---
title: "Contract Model"
description: "Understand CrudCraft public contracts across annotations, generated source, runtime modules, starters, configuration, and documentation."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/generated-code-lifecycle"
  - "/architecture/module-boundaries"
  - "/architecture/error-model"
---

# Contract Model

CrudCraft contracts exist in more than one place. A change can break users even if every CrudCraft module still compiles, because users compile against generated source and call generated HTTP routes.

## Contract Levels

| Level | Stable contract | Examples |
|---|---|---|
| Source API | Public annotations, enums, and interfaces in `crudcraft-api`. | `@CrudCrafted`, `CrudEndpoint`, `CrudTemplate`, `CrudEndpointPolicy`, `CrudSecurityPolicy`, `FieldSecurityAdapter`, `RowSecurityHandler` |
| Generated Java API | Public generated classes and members that application code can compile against. | `BookRequestDto`, `BookResponseDto`, record components, mapper/service/controller class names, repository type |
| Generated HTTP API | Routes, HTTP methods, request bodies, query parameters, response wrappers, status behavior. | `GET /books`, `POST /books/search`, `GET /books/export`, bulk endpoints |
| Runtime API | Public runtime classes and interfaces generated code imports. | `AbstractCrudService`, `PaginatedResponse`, `SearchRequest`, `ExportRequest`, `ProjectionAdapter`, `CrudRuntimeExtension` |
| Starter API | Maven artifact names and Spring Boot auto-configuration behavior. | `crudcraft-spring-boot-starter-search`, projection auto-configuration |
| Configuration API | Documented properties and defaults. | `crudcraft.api.max-page-size`, `crudcraft.export.max-csv-rows`, `crudcraft.projection.registry-fqcn`, `crudcraft.search.*` |
| Documentation API | Behavior promised in Feature Guides and architecture pages. | Feature examples, endpoint semantics, compatibility rules |
| Internal implementation | Can change when contracts remain intact. | Private writer helpers, generated local variables, JavaPoet formatting |

## Generated Contract Examples

Generated Code Contract is the rule that public generated source, generated
routes, and generated metadata are user-facing compatibility surfaces.

A generated request DTO is a contract because applications can construct it, tests can serialize it,
and clients can send the matching JSON payload. Current 1.x DTOs are mutable JavaBean classes for
Jackson and Bean Validation compatibility:

```java
public class BookRequestDto {
    private String title;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
```

A generated route is a contract even if the method body changes:

```java
@GetMapping("/{id}")
public ResponseEntity<BookResponseDto> getOne(@PathVariable UUID id) {
    ...
}
```

Generated controller implementation details are not contracts: local variable names, exact logging statement placement, and JavaPoet formatting may change.

## Compatibility Rules

Treat these as compatibility events:

- renaming annotations, enum constants, policy interfaces, or runtime classes;
- changing generated package names, class names, DTO component names, method signatures, route paths, query parameters, or request payload shape;
- changing default endpoint sets for a `CrudTemplate`;
- changing endpoint resolution order for template, include, omit, search, export, or policy decisions;
- changing exception-to-status mappings;
- changing starter transitive dependencies in a way applications observe;
- changing documented configuration keys or defaults;
- changing field, row, search, projection, or export behavior promised by a Feature Guide.

Internal refactors are safe only when tests prove the public behavior and generated shape stay compatible.

## Same-Release Alignment

Generated code and runtime modules are designed for the same CrudCraft release line. Mixing versions can fail in two ways:

| Mismatch | Example failure |
|---|---|
| New codegen, old runtime | Generated controller imports a runtime class or method that does not exist. |
| Old codegen, new runtime | Runtime expects metadata or DTO shape old generated code does not provide. |

Feature docs should tell users to align every CrudCraft artifact to one `${crudcraft.version}`. Architecture docs should explain why.

## Contract Review Checklist

For every public or generated behavior change:

- identify the contract level affected;
- update golden or compile tests for generated source changes;
- update runtime tests for request-time behavior changes;
- update sample app tests when generated and runtime behavior interact;
- update Feature Guides for user-facing feature behavior;
- update architecture pages only when boundaries, lifecycle, or contracts change.

## 2.0 Planning Items

Before a 2.0 compatibility reset, review generated validation annotations such as
`jakarta.validation.Valid`, the reproducible project build timestamp, and any generated API shape
that users may have compiled against. Response, reference, and named read DTOs should migrate to
Java records in 2.x so read contracts become immutable values. Request DTOs may stay mutable for
Jackson deserialization or move to an explicit builder/constructor strategy after compatibility
testing.

## Non-Contracts

These are intentionally not stable:

- private helper methods in generated controllers;
- generated local variable names;
- writer helper class names;
- descriptor implementation layout;
- exact import ordering unless covered by deterministic output tests;
- sample data values;
- manually edited strict generated files.

## Related Documentation

- [Generated Code Lifecycle](generated-code-lifecycle.md)
- [Module Boundaries](module-boundaries.md)
- [Testing Architecture](testing-architecture.md)
