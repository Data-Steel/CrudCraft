---
title: "Architecture"
description: "Understand CrudCraft's compile-time generator, generated source, runtime modules, starters, contracts, and design constraints."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/system-overview"
  - "/architecture/codegen-architecture"
  - "/architecture/runtime-architecture"
---

# Architecture

CrudCraft is split across three phases:

1. Application code declares entities and generation intent with `crudcraft-api`.
2. `crudcraft-codegen` reads that source during compilation and writes generated Java source.
3. Generated controllers and services call `crudcraft-runtime-*` contracts at request time.

This section documents that split. Feature behavior belongs in [Feature Guides](../feature-guides/); architecture pages explain where the behavior lives, which contracts are stable, and which coupling is forbidden.

## Read In This Order

| Page | What it answers |
|---|---|
| [System Overview](system-overview.md) | Which modules exist and how a request moves from entity annotations to a running endpoint. |
| [Codegen Architecture](codegen-architecture.md) | How `CrudCraftProcessor`, descriptor extraction, generator ordering, endpoint resolution, and file writing work. |
| [Runtime Architecture](runtime-architecture.md) | How generated services use `AbstractCrudService`, optional collaborators, projection, search, export, security, and extensions. |
| [Module Boundaries](module-boundaries.md) | Which Maven and JPMS dependencies are allowed between API, codegen, runtime modules, starters, sample app, and tools. |
| [Generated Code Lifecycle](generated-code-lifecycle.md) | Which generated files are overwritten, which stubs are application-owned, and how regeneration should be reviewed. |
| [Extension Points](extension-points.md) | Which customization hooks are public contracts and which internals must not be used by applications. |
| [Contract Model](contract-model.md) | What counts as a public contract: annotations, generated DTOs/routes, runtime interfaces, properties, starters, and docs. |
| [Error Model](error-model.md) | Where errors are detected and how `CrudCraftExceptionHandler` maps runtime failures to HTTP responses. |
| [Security Model](security-model.md) | How endpoint authorization, row isolation, and field filtering are layered without owning authentication. |
| [Testing Architecture](testing-architecture.md) | Which test layer should prove annotation, generator, runtime, starter, sample, and documentation changes. |
| [Design Decisions](design-decisions/) | Accepted ADRs for the codegen/runtime split, runtime module boundaries, and generated file ownership. |

## Non-Negotiable Invariants

- `crudcraft-api` is the source-level contract. It must not depend on codegen, runtime modules, starters, sample code, or tools.
- `crudcraft-codegen` may read API annotations and may generate imports for public runtime contracts, but generated application code must never import processor internals.
- `crudcraft-runtime-core` owns shared request-time behavior. Search, projection, export, security, and extension behavior stay in optional runtime modules.
- Generated strict files are CrudCraft-owned and may be overwritten. Editable stubs are application-owned after first creation.
- A change to generated public type names, record components, routes, request payloads, status codes, starter artifacts, configuration keys, or documented behavior is a contract change.

## Related Documentation

- [Feature Guides](../feature-guides/)
- [Documentation Inventory](../documentation-inventory.md)
- [Contributor Handbook](../contributor-handbook/)
