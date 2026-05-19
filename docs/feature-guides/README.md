---
title: "Feature Guides"
description: "Use CrudCraft feature guides as the single documentation column for user-facing features."
section: "Feature Guides"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
related:
  - "/features"
  - "/documentation-inventory"
---

# Feature Guides

Feature Guides are the canonical place for CrudCraft feature documentation. They contain both practical examples and exact feature facts, so users do not need a separate reference section.

Use this page as the route map for generated API, runtime, search, projection, export, security, validation, and extension features.

## Feature Areas

| Feature | Use this when |
|---|---|
| [Code Generation](code-generation/) | You need generated DTOs, mappers, repositories, services, controllers, endpoint templates, editable stubs, LOB handling, relationships, and deterministic output. |
| [Runtime Modules](runtime-modules/) | You need to choose or combine runtime modules and starters. |
| [Search](search/) | You need generated search request classes, operators, nested paths, sorting, pagination, or keyset search. |
| [Projection](projection/) | You need focused read models, projection metadata, projection paths, or projection/security behavior. |
| [Export](export/) | You need generated CSV, JSON, or XLSX export endpoints with limits, field selection, search, and security. |
| [Security](security/) | You need endpoint RBAC, field-level security, tenant/client/owner row isolation, or security tests. |
| [Validation](validation/) | You need Jakarta Validation behavior, generated validate endpoints, or generated error responses. |
| [Extensions](extensions/) | You need auditing, soft-delete fields, or relationship helper behavior. |

## Coverage Checklist

The code-derived coverage checklist lives in [CrudCraft Features](../features.md). When a feature is added to code, update that checklist and the relevant Feature Guide in the same change.

## Related documentation

- [CrudCraft Features](../features.md)
- [Documentation Inventory](../documentation-inventory.md)
