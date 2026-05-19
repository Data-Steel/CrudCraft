---
title: "Export Runtime Module"
description: "Use the CrudCraft export runtime module for generated CSV, JSON, or XLSX export behavior."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-export"
  - "crudcraft-spring-boot-starter-export"
related:
  - "/feature-guides/runtime-modules"
  - "/architecture/runtime-architecture"
  - "/feature-guides/runtime-modules/security"
---

# Export Runtime Module

The export runtime module supports generated export endpoints for selected data formats.

Use this page when generated APIs need CSV, JSON, or XLSX export.

## Who this page is for

This page is for developers adding export behavior to generated APIs.

## When to use this page

Use this page before exposing generated export endpoints.

## When not to use this page

Do not expose export endpoints without reviewing row and field security.

## Prerequisites

- Core runtime is available.
- Export endpoint generation is enabled.
- Exported fields and limits are reviewed.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-export</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated export paths can use export runtime support.

## Limits and Retry Behavior

Export row limits fail with HTTP `413 Payload Too Large`. The response message identifies the
configured limit and tells the client to narrow the query, lower the requested row count, or page the
export request. Treat this differently from malformed filters (`400`) and unsupported formats
(`415`): the request is syntactically valid, but too large for the configured export policy.

Large binary `@Lob` fields should not be exported as inline payloads unless the endpoint owner has
reviewed memory and transport costs. Prefer storing large file content outside DTO export rows and
exporting stable metadata or download links instead.

## Request Body Size

CrudCraft does not override the web container's request-size limits for JSON bulk or export request
bodies. Set Spring Boot and servlet container limits such as `spring.servlet.multipart.max-request-size`
and your reverse proxy body-size policy when clients can send large include/exclude field lists or
bulk export criteria.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Treating export like a small read | Export can return many records. | Apply limits and security review. |
| Exporting protected fields | Sensitive data can leave the API in bulk. | Apply field security and exclude rules. |
| Missing tests for formats | One format can fail while another works. | Test each enabled format. |

## Troubleshooting

If export fails at runtime, confirm the export starter is installed and the generated endpoint uses the expected export service.

## Related documentation

- [Runtime Modules](../runtime-modules/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
- [Security Runtime Module](security.md)
