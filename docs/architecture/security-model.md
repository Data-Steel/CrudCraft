---
title: "Security Model"
description: "Understand how CrudCraft layers generated endpoint authorization, row isolation, field security, generated metadata, and application-owned authentication."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
  - "crudcraft-starter-security"
related:
  - "/architecture/runtime-architecture"
  - "/architecture/error-model"
  - "/feature-guides/security"
---

# Security Model

CrudCraft security is layered. It does not authenticate users. It generates endpoint checks and provides runtime hooks that use the principal, authorities, claims, rows, and fields your application defines.

## Security Layers

```text
Application authentication
  -> Spring Security principal and authorities
  -> generated endpoint @PreAuthorize expression
  -> runtime row read filter and write/delete guard
  -> runtime field write filter
  -> mapper / repository / projection / export
  -> runtime field read filter
  -> response
```

Each layer has a different owner.

| Layer | Owner | CrudCraft mechanism |
|---|---|---|
| Authentication | Application | Spring Security configuration, token validation, user loading. |
| Endpoint authorization | Generated controller + Spring Security | `secure`, `securityPolicy`, `@CrudSecurity`, `@EndpointRbac`, `CrudSecurityPolicy`. |
| Row isolation | Generated service + runtime security | `RowSecurityHandler`, built-in row scopes, `ClaimScopedRowSecurityHandler`, `RowSecurityRuntimeExtension`. |
| Field write filtering | Runtime security before mapping | `FieldSecurityAdapter.filterWrite(...)`, write policy behavior. |
| Field read filtering | Runtime security after mapping/projection/export row mapping | `FieldSecurityAdapter.filterRead(...)`, generated field metadata, export security filter. |
| Custom endpoints | Application | Application must call security contracts explicitly. |

## Endpoint Authorization

Endpoint authorization is compiled into generated controller methods as `@PreAuthorize`.

Expression precedence:

1. Annotation-derived endpoint RBAC expressions (`@EndpointRbac`) become a resolved policy map.
2. A configured `CrudSecurityPolicy` class supplies expressions per `CrudEndpoint`.
3. `secure = true` without an explicit policy falls back to `isAuthenticated()`.

This is endpoint-specific. A generated `GET_ALL` can have a different expression than `EXPORT`, `PATCH`, or `BULK_DELETE`.

Endpoint authorization decides whether a request may enter the endpoint. It does not replace row or field controls.

## Row Isolation

Row isolation is applied as a JPA `Specification` through `CrudRuntimeExtension.readFilter(...)`. In runtime-core, that filter is combined with:

- regular find/list specifications;
- `findById`, `findByIds`, `existsById`, and `count`;
- search specifications;
- keyset cursor specifications;
- visible entity loads before update, patch, upsert, and delete.

For writes and deletes, `RowSecurityRuntimeExtension` also calls `RowSecurityHandler.apply(entity)` before save/delete. Built-in tenant/client/owner scope annotations generate `ClaimScopedRowSecurityHandler` construction in the generated service, using `PrincipalScopeAccessor`.

The architecture consequence is important: a hidden row usually behaves like a missing row because `AbstractCrudService.loadEntity(...)` cannot find it after row filters are applied.

## Field Security

Field security is read/write filtering, not endpoint selection. Generated metadata tells runtime security which DTO fields have field rules.

Write path:

```text
request DTO
  -> FieldSecurityRuntimeExtension.beforeCreate/beforeUpdate
  -> FieldSecurityAdapter.filterWrite(request, existing)
  -> mapper
  -> row write guard / save
```

Read path:

```text
entity or projection result
  -> mapper/projection/export row mapper
  -> FieldSecurityRuntimeExtension.afterRead
  -> FieldSecurityAdapter.filterRead(dto)
```

Export must use the same read-filtering semantics because export is another read surface. Projection must account for field security before selecting or returning protected fields.

## What Security Does Not Cover Automatically

CrudCraft-generated security applies to generated controllers and services. It does not automatically secure:

- handwritten controllers;
- direct repository access;
- custom services that bypass generated service methods;
- application-owned export code;
- scheduled jobs or messaging handlers;
- database views or native queries;
- fields marked searchable/exportable without a security review.

When custom code exposes generated DTOs or protected entities, it must call the same runtime security utilities or enforce equivalent application policy.

## Failure Modes

| Failure | Cause | Fix |
|---|---|---|
| `@PreAuthorize` is missing | Model is not `secure` and has no security annotation/policy. | Enable generated security on the model or endpoint. |
| Tenant data crosses boundaries | Row scope claim mapping or handler is wrong. | Test generated read, search, update, delete, and export paths with two tenants. |
| Field is hidden on normal reads but visible in export | Export mapper/filter path skipped field security. | Route export rows through `filterRead(...)`. |
| User can write a protected field | Write filter or write policy is not configured/tested. | Add field-security write tests for create, update, patch, bulk, and upsert. |
| Custom endpoint leaks data | It bypasses generated service/filter path. | Call generated service methods or explicit runtime security utilities. |

## Related Documentation

- [Security Feature Guides](../feature-guides/security/)
- [Runtime Architecture](runtime-architecture.md)
- [Error Model](error-model.md)
