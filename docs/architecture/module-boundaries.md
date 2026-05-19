---
title: "Module Boundaries"
description: "Understand the allowed dependency directions between CrudCraft API, codegen, runtime modules, starters, sample app, tools, and generated source."
section: "Architecture"
audience:
  - "Contributors"
  - "Maintainers"
  - "Advanced users"
status: "stable"
related:
  - "/architecture/system-overview"
  - "/architecture/runtime-architecture"
  - "/architecture/design-decisions/adr-0002-runtime-module-boundaries"
---

# Module Boundaries

CrudCraft uses Maven modules and JPMS descriptors to keep optional features optional and generated contracts stable. When a dependency moves, review both the Maven graph and `module-info.java`.

## Dependency Direction

Allowed direction:

```text
crudcraft-api
  <- crudcraft-codegen
  <- generated application source
  -> crudcraft-runtime-core
  -> optional crudcraft-runtime-* modules
  -> crudcraft-starter-* modules
```

Generated application source is special: it is produced by codegen, but it compiles as part of the user application and imports public runtime contracts.

## Module Ownership

| Module | Owns | Must not own |
|---|---|---|
| `crudcraft-api` | Public annotations, enums, public policy interfaces, security annotation contracts. | Runtime implementation, processors, Spring Boot auto-configuration. |
| `crudcraft-codegen` | Annotation processors, descriptor model, extractors, generators, generated metadata writers, Insomnia writer. | Runtime execution, Spring beans, database access, application-specific decisions. |
| `crudcraft-runtime-core` | Shared CRUD services, response/error models, query strategy, extension chain, projection adapter contract, CrudCraft/Spring MVC exception handling. | Search validation contracts, projection JPA implementation, export streaming, Spring Security exception mapping or security-specific enforcement. |
| `crudcraft-runtime-search` | Search request contract, search validation, search delegation helpers, search properties. | Generated search source writing, generic CRUD persistence. |
| `crudcraft-runtime-projection` | Projection metadata registry, JPA criteria projection execution, `ProjectionAdapter` implementation. | DTO generation, endpoint route shape. |
| `crudcraft-runtime-export` | Export request model, CSV/JSON/XLSX streaming, entity export services. | Endpoint inclusion decisions, security policy ownership. |
| `crudcraft-runtime-security` | Field security filtering, row security handlers, principal scope access, endpoint policy helpers. | Authentication provider setup, token validation, custom controller security. |
| `crudcraft-runtime-extensions` | Reusable embeddables and generated-service relationship helpers. | Application business logic. |
| `crudcraft-starter-*` | Dependency composition, capability auto-configuration, and framework adapters for one capability. | Feature logic that belongs in runtime modules. |
| `crudcraft-starter` | Umbrella starter that aggregates all feature starters. | Hiding incompatible versions or changing optional features silently. |
| `crudcraft-sample-app` | Integrated generated API examples and end-to-end coverage. | Contracts consumed by production code. |
| `crudcraft-tools` | Repository maintenance utilities and quality gate support. | Runtime behavior or generated API contracts. |

## Starter Composition

The starter artifacts are intentionally capability-aligned:

| Starter artifact | Pulls in |
|---|---|
| `crudcraft-spring-boot-starter-core` | `crudcraft-api`, `crudcraft-runtime-core`, MapStruct, Jakarta annotation API, Springdoc WebMVC UI. |
| `crudcraft-spring-boot-starter-security` | `crudcraft-api`, `crudcraft-runtime-security`, `crudcraft-runtime-core`, Spring Boot autoconfigure. |
| `crudcraft-spring-boot-starter-search` | `crudcraft-runtime-search`. |
| `crudcraft-spring-boot-starter-export` | `crudcraft-runtime-export`. |
| `crudcraft-spring-boot-starter-projection` | `crudcraft-runtime-projection`. |
| `crudcraft-spring-boot-starter-extensions` | `crudcraft-runtime-extensions`. |
| `crudcraft-spring-boot-starter` | All capability starters. |

If a generated controller imports an optional runtime contract, the application must install the matching starter or runtime module. Core should not silently pull optional features into every application.

## JPMS Rules

Published modules declare `module-info.java` and `Automatic-Module-Name` metadata through the build. Keep these aligned with Maven dependencies:

- `crudcraft-api` exports only annotation packages.
- `crudcraft-codegen` exports processor entry points, descriptor/SPI contracts used by
  service-loaded extensions, and documented utility contracts. Writer implementation subpackages
  are internal and must stay unexported unless they are promoted to documented SPI.
- Runtime modules export public runtime packages, not internal test fixtures.
- Capability modules must avoid JPMS split packages. Shared contracts belong in core or API; feature behavior belongs in the owning runtime module.
- Starter modules should expose starter packages and require the runtime modules they compose.

Generated services call their generated relationship metadata classes directly. The
`runtime-core` `RelationshipUtils` reflection path remains only as a compatibility fallback for
older editable stubs or downstream code that still calls it.

## Forbidden Coupling

| Forbidden dependency | Why it is wrong |
|---|---|
| `crudcraft-api` -> any CrudCraft implementation module | Makes user source contracts depend on implementation. |
| `crudcraft-runtime-core` -> optional runtime module | Makes optional features mandatory. |
| generated source -> `nl.datasteel.crudcraft.codegen.*` | Leaks processor internals into user applications. |
| runtime module -> generated application package | Runtime jars must work without a specific generated project. |
| starter -> sample app | Sample app is a consumer, not a dependency provider. |
| feature runtime -> unrelated feature implementation | Creates transitive behavior users did not select. |

## Violation Examples

Bad:

```java
// runtime-core class importing runtime-security implementation
import nl.datasteel.crudcraft.runtime.security.FieldSecurityRuntimeExtension;
```

Good:

```java
// runtime-core depends only on neutral extension contract
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
```

Bad:

```java
// generated controller importing codegen internals
import nl.datasteel.crudcraft.codegen.writer.TemplateUtil;
```

Good:

```java
// generated controller importing runtime public API
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
```

## Review Checklist

Before accepting a boundary change:

- Check the changed `pom.xml` files and matching `module-info.java`.
- Confirm generated source still imports only public API/runtime contracts.
- Confirm optional feature dependencies are introduced only in the feature runtime or starter that owns them.
- Add or update tests that would fail if the dependency drifts again.
- Update [Runtime Modules](../feature-guides/runtime-modules/) when an application dependency decision changes.

## Related Documentation

- [System Overview](system-overview.md)
- [Runtime Architecture](runtime-architecture.md)
- [ADR 0002: Runtime Module Boundaries](design-decisions/adr-0002-runtime-module-boundaries.md)
