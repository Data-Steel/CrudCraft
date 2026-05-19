---
title: "ADR 0002: Runtime Module Boundaries"
description: "Records the decision to keep CrudCraft runtime capabilities independently selectable through separate modules and starters."
section: "Architecture"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/runtime-architecture"
  - "/architecture/module-boundaries"
  - "/feature-guides/runtime-modules"
---

# ADR 0002: Runtime Module Boundaries

## Status

Accepted.

## Context

CrudCraft runtime behavior is not one feature. Core CRUD, search, projection, export, security, and reusable extensions have different dependencies and different risk profiles.

For example:

- search needs Spring Data `Specification` request validation;
- projection needs JPA criteria execution and generated metadata;
- export needs CSV/JSON/XLSX streaming libraries;
- security needs Spring Security and principal/field/row contracts;
- extensions include embeddables and relationship utilities.

## Problem

If optional runtime capabilities move into core, every generated API application receives unnecessary dependencies and possible behavior changes. If feature modules depend on each other casually, selecting one feature can pull in another feature without the user choosing it.

## Decision

Keep `crudcraft-runtime-core` focused on shared generated API behavior. Keep capability-specific behavior in dedicated runtime modules and expose them through matching starters:

```text
crudcraft-runtime-core
crudcraft-runtime-search
crudcraft-runtime-projection
crudcraft-runtime-export
crudcraft-runtime-security
crudcraft-runtime-extensions

crudcraft-spring-boot-starter-core
crudcraft-spring-boot-starter-search
crudcraft-spring-boot-starter-projection
crudcraft-spring-boot-starter-export
crudcraft-spring-boot-starter-security
crudcraft-spring-boot-starter-extensions
crudcraft-spring-boot-starter
```

The umbrella starter may aggregate everything. Feature starters must remain useful on their own.

## Consequences

- Applications can keep dependency surface aligned with generated features.
- Generated source must import optional runtime contracts only when the feature is generated.
- Runtime-core needs small abstraction points for optional collaborators, such as `CrudRuntimeExtension` and `ProjectionAdapter`.
- Starter dependency changes are compatibility-sensitive because they change what applications get at runtime.

## Invariants

- Core must not depend on feature runtime implementation modules.
- Feature runtimes may depend on core and API, but not on generated application packages.
- Starters compose modules; they do not own feature behavior.
- JPMS descriptors and Maven dependencies must be updated together.
- Optional features must not become mandatory without a documented breaking change.

## Tests

Changes touching this decision should include:

- dependency/module descriptor review;
- starter composition tests where available;
- sample app coverage when generated source imports a feature runtime;
- docs updates in [Runtime Modules](../../feature-guides/runtime-modules/).

## Related Documentation

- [Runtime Architecture](../runtime-architecture.md)
- [Module Boundaries](../module-boundaries.md)
- [Runtime Modules](../../feature-guides/runtime-modules/)
