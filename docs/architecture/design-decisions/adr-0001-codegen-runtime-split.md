---
title: "ADR 0001: Codegen Runtime Split"
description: "Records the decision to keep CrudCraft annotation processing and generated source separate from runtime execution behavior."
section: "Architecture"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/codegen-architecture"
  - "/architecture/runtime-architecture"
  - "/architecture/module-boundaries"
---

# ADR 0001: Codegen Runtime Split

## Status

Accepted.

## Context

CrudCraft has three different execution contexts:

- annotation processing in `crudcraft-codegen` during compilation;
- generated application source compiled into the user's application;
- runtime modules used by generated services/controllers after Spring Boot starts.

These contexts have different dependencies and failure modes. Codegen has access to source-level annotation metadata. Runtime modules have access to Spring beans, repositories, security context, JPA metamodel, request data, and current principal.

## Problem

If runtime behavior is embedded too deeply in generated source, every runtime bug fix requires users to regenerate code. If processors depend on runtime implementation classes, runtime refactors become source-generation compatibility risks. If generated source imports processor internals, user applications accidentally depend on implementation details.

## Decision

Keep compile-time generation in `crudcraft-codegen`. Keep request-time behavior in `crudcraft-runtime-*`. Generated source may import public runtime contracts, but not codegen internals.

Allowed:

```java
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
```

Forbidden:

```java
import nl.datasteel.crudcraft.codegen.writer.stubs.ControllerGenerator;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
```

## Consequences

- Generated source stays reviewable and application-specific.
- Runtime modules can fix behavior without regenerating code when the public contract stays stable.
- Public runtime contracts used by generated source need compatibility tests.
- Codegen sometimes emits calls to runtime contracts it does not execute itself.
- Core runtime uses optional adapter boundaries, such as `ProjectionAdapter`, instead of depending on every feature module.

## Invariants

- `crudcraft-codegen` must not need Spring application beans.
- Generated application source must never import `nl.datasteel.crudcraft.codegen.*`.
- Runtime modules must not scan source annotations as a substitute for generated metadata.
- Runtime bug fixes should not require regeneration unless a public generated/runtime contract changes.

## Tests

Changes touching this decision need at least one of:

- generated source compile/golden tests proving imports remain public;
- runtime tests proving behavior lives behind runtime contracts;
- sample app tests proving generated code and runtime modules still work together.

## Related Documentation

- [Codegen Architecture](../codegen-architecture.md)
- [Runtime Architecture](../runtime-architecture.md)
- [Module Boundaries](../module-boundaries.md)
