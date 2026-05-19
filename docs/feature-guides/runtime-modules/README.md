---
title: "Runtime Modules"
description: "Choose and combine CrudCraft runtime modules for generated API behavior."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
related:
  - "/feature-guides/runtime-modules"
  - "/architecture/runtime-architecture"
  - "/feature-guides/runtime-modules/configuration"
---

# Runtime Modules

CrudCraft runtime modules provide shared behavior used by generated APIs at request time.

Use this page to choose the runtime module guide that matches the feature you need.

## Who this page is for

This page is for developers selecting CrudCraft runtime dependencies.

## When to use this page

Use this page when adding, removing, or reviewing CrudCraft starters.

## When not to use this page

Do not use this page for annotation processor setup. Use Code Generation for compile-time setup.

## Start here

| Module guide | Use it for |
|---|---|
| [Core](core.md) | Required generated CRUD API behavior. |
| [Search](search.md) | Generated search requests and filtering. |
| [Projection](projection.md) | Focused read models and projection execution. |
| [Export](export.md) | CSV, JSON, or XLSX export. |
| [Security](security.md) | Field security, row isolation, and endpoint security helpers. |
| [Extensions](extensions.md) | Optional reusable runtime extension behavior. |
| [Observability](observability.md) | Micrometer observations and OpenTelemetry span export. |
| [Configuration](configuration.md) | Dependency and Spring Boot setup. |
| [Compatibility](compatibility.md) | Safe module combinations and version alignment. |

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-core</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated core CRUD APIs have the runtime support they need.

## Deployment Topology

CrudCraft modules are ordinary Spring Boot dependencies. In a monolith, include the starters for
the generated APIs that run in that application and keep annotation processing in the same build.
This is the simplest topology and is the recommended default.

In a microservice deployment, generate each service's CRUD artifacts from the entities owned by that
service only. Do not share generated controllers across service boundaries. Share `crudcraft-api`
annotations and common DTO contracts through versioned libraries when needed, but keep runtime
modules local to the service that executes the generated endpoints.

For split read/write systems, place search, projection, and export modules only in the service that
serves those read paths. Core and security usually belong everywhere generated endpoints run.

## Related documentation

- [Runtime Modules](../runtime-modules/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
- [Configuration](configuration.md)
