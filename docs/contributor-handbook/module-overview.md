---
title: "Module Overview"
description: "Choose the correct CrudCraft Maven module for API, codegen, runtime, starter, sample app, tools, or benchmark changes."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/architecture/module-boundaries"
  - "/feature-guides/runtime-modules"
  - "/contributor-handbook/repository-structure"
---

# Module Overview

The root reactor contains these modules in order: API, runtime modules, codegen, starters, sample app, and tools. That order matters because generated code must call public runtime contracts without making runtime modules depend on generated application classes.

## Ownership Matrix

| Module | Add code here when | Do not add |
|---|---|---|
| `crudcraft-api` | Application source needs a new annotation, enum, or public policy interface. | Processor implementation, Spring beans, runtime behavior. |
| `crudcraft-codegen` | Source annotations must change generated DTOs, mappers, services, controllers, metadata, search artifacts, or Insomnia output. | Request-time persistence, security decisions, or Spring Boot wiring. |
| `crudcraft-runtime-core` | Every generated API can use the behavior: CRUD flow, response wrappers, exceptions, keyset, projection adapter contract, neutral extension chain. | Feature-specific search/projection/export/security implementation. |
| `crudcraft-runtime-search` | Search requests, allowed fields/operators/sorts, and search execution helpers change. | Endpoint generation or starter composition. |
| `crudcraft-runtime-projection` | Projection metadata execution, registry lookup, JPA criteria projection, or projection adapter implementation changes. | DTO generation and annotation extraction. |
| `crudcraft-runtime-export` | Export request validation, row limits, DTO/entity export, streaming formats, or export serialization changes. | Deciding whether `/export` is generated. |
| `crudcraft-runtime-security` | Endpoint policy helpers, field security, row security, or principal scope access changes. | Authentication provider configuration. |
| `crudcraft-runtime-extensions` | Reusable embeddables or generated-service relationship helpers change. | Application-specific business logic. |
| `crudcraft-starter-core` | Core starter dependencies or auto-configuration exposure changes. | Feature implementation. |
| `crudcraft-starter-search/export/projection/security/extensions` | Capability starter composition changes. | Runtime feature logic. |
| `crudcraft-starter` | Umbrella starter aggregation changes. | Feature-specific defaults hidden from capability starters. |
| `crudcraft-sample-app` | Cross-module generated behavior needs integration coverage. | Sole proof for library-module behavior. |
| `crudcraft-tools` | Repository quality or maintenance tooling changes. | Runtime contracts. |
| `benchmarks` | Performance-sensitive behavior needs JMH coverage. | Functional tests. |

## Dependency Rule

If you need to import a class from a module that should be optional, stop and check [Module Boundaries](../architecture/module-boundaries.md). Moving one helper into the wrong module is how optional features become mandatory.

## Examples

| Feature request | Modules |
|---|---|
| Add endpoint-specific RBAC annotation behavior | `crudcraft-api`, `crudcraft-codegen`, `crudcraft-runtime-security`, `crudcraft-sample-app`, security docs. |
| Change export limit handling | `crudcraft-codegen` if generated controller properties change, `crudcraft-runtime-export` if streaming behavior changes, export/security docs. |
| Add projection metadata field support | `crudcraft-api` if annotation contract changes, `crudcraft-codegen`, `crudcraft-runtime-projection`, sample app, projection docs. |

## Related Documentation

- [Module Boundaries](../architecture/module-boundaries.md)
- [Runtime Modules](../feature-guides/runtime-modules/)
- [Repository Structure](repository-structure.md)
