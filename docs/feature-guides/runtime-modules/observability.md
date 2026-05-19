---
title: "Observability Runtime Module"
description: "Export CrudCraft runtime operations as Micrometer observations and OpenTelemetry spans."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
  - "Operators"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-observability"
related:
  - "/feature-guides/runtime-modules"
  - "/architecture/runtime-architecture"
  - "/maintainer-handbook/quality-gates"
---

# Observability Runtime Module

The observability module auto-configures CrudCraft operation observations when Micrometer
Observation is present.

## Quick Setup

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-runtime-observability</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

With an OpenTelemetry bridge on the application classpath, Micrometer exports these observations as
spans.

## Span Contract

CrudCraft observations use stable, low-cardinality names and tags:

| Item | Meaning |
|---|---|
| `crudcraft.operation` | Operation wrapper span name. |
| `crudcraft.entity` | Entity or generated resource name. |
| `crudcraft.endpoint` | Logical endpoint or service operation. |
| `crudcraft.outcome` | `success` or `error`. |

Do not attach request IDs, entity IDs, search terms, or user data as low-cardinality tags. Use logs
or high-cardinality tracing attributes outside CrudCraft helpers when an application needs those.

## Customization

Declare your own `CrudCraftObservationSupport` bean to override the default auto-configured helper.
Keep span names and tags stable when dashboards or alerts depend on them.
