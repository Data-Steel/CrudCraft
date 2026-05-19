---
title: "Extensions"
description: "Use CrudCraft runtime extensions for auditing, soft delete fields, and relationship helper behavior."
section: "Feature Guides"
category: "Extensions"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-extensions"
  - "crudcraft-spring-boot-starter-extensions"
related:
  - "/feature-guides/runtime-modules/extensions"
  - "/feature-guides/code-generation/generated-layers"
  - "/features"
---

# Extensions

`crudcraft-runtime-extensions` contains reusable building blocks that generated and application code can use around generated CRUD models.

Use this page when you need auditing timestamps, soft-delete fields, or relationship utility behavior.

## Auditable embeddable

```java
@Entity
public class Article {
    @Embedded
    private AuditableExtension audit = new AuditableExtension();
}
```

`AuditableExtension` is an embeddable with `created_at` and `updated_at`. JPA lifecycle callbacks set both timestamps on persist and update `updated_at` on update. Use it when entities need consistent timestamp fields without duplicating lifecycle methods.

## Soft delete embeddable

```java
@Entity
public class Article {
    @Embedded
    private SoftDeleteExtension deletion = new SoftDeleteExtension();
}
```

`SoftDeleteExtension` stores `deleted` and `deleted_at`. Calling `setDeleted(true)` sets `deleted_at` if it was empty; calling `setDeleted(false)` clears it.

This embeddable stores state; it does not automatically change generated `DELETE` endpoints into soft deletes. Add service logic or a custom runtime extension if delete behavior must be replaced.

## Relationship utilities

Generated mappers and services use relationship helper logic for relation ID mapping and collection updates. Keep relationship customization in editable service or mapper stubs when application-specific behavior is needed.

## Writing custom runtime extensions

Implement `CrudRuntimeExtension<T, U>` when behavior must run for every generated service call. Keep extension beans stateless and thread-safe; generated services are Spring singletons and reuse the same extension instance across request threads.

Extension hooks are part of the service transaction path:

| Hook | Runs before/after | Contract |
|---|---|---|
| `beforeCreate` / `beforeUpdate` | mapper writes | Return the request that should be mapped. Throw `BadRequestException`, `ForbiddenException`, or another CrudCraft runtime exception for client-actionable failures. |
| `beforeSave` / `beforeDelete` | repository mutation | Validate entity state or apply neutral lifecycle changes. Avoid remote calls here unless they are idempotent. |
| `readFilter` | list/search/read/count/export queries | Return a JPA `Specification` that narrows visible rows. Return `null` when the extension does not apply. |
| `afterRead` | DTO response emission | Redact or adapt response DTOs. Never reintroduce fields denied by field-level security. |

Treat exceptions as API contracts. Use `BadRequestException` for invalid caller input, `ForbiddenException` for authorization failures, `ResourceNotFoundException` only when the requested visible row is absent, and reserve unchecked infrastructure exceptions for genuine server failures.

## Security and performance rules

Custom extensions must preserve row and field security. If an extension adds a `readFilter`, combine it with existing constraints instead of replacing them. If an extension returns a modified DTO from `afterRead`, keep denied fields redacted and validate that behavior with sample-app style E2E tests.

Avoid unbounded per-row work in `afterRead` and `beforeSave`. Expensive lookups should be cached in thread-safe structures, bounded by tenant/application scope, and invalidated explicitly. Search and export extensions should respect configured search depth, export depth, and page-size limits to avoid N+1 query amplification.

## Related documentation

- [Extensions Runtime Module](../runtime-modules/extensions.md)
- [Generated Layers](../code-generation/generated-layers.md)
- [Security](../security/README.md)
