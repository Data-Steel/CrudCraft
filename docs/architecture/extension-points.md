---
title: "Extension Points"
description: "Understand the public CrudCraft customization surfaces and the internal implementation details applications must not depend on."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/module-boundaries"
  - "/architecture/contract-model"
  - "/feature-guides/extensions"
---

# Extension Points

CrudCraft extension points are explicit contracts. If a hook is not documented here or in a Feature Guide, treat it as internal even when it is public Java for module accessibility.

Types annotated with `@InternalOnly` are deliberately outside the public contract. They may remain
public because JPMS exports, generated code, or compatibility facades require visibility, but
applications should use the documented replacement named in the annotation or in the feature guide.

## Public Extension Surfaces

| Surface | Examples | Use when |
|---|---|---|
| Generation annotations | `@CrudCrafted`, `@Dto`, `@Request`, `@Searchable`, `@ProjectionField`, `@FieldSecurity`, `@EndpointRbac`, row-scope annotations | You want to change generated files or generated metadata. |
| Endpoint policy classes | `CrudEndpointPolicy`, `CrudTemplate`, `includeEndpoints`, `omitEndpoints` | You want to decide which generated endpoints exist. |
| Security policy classes | `CrudSecurityPolicy`, runtime policy helpers, annotation-derived endpoint RBAC | You want generated endpoints to receive endpoint-specific `@PreAuthorize` expressions. |
| Row security handlers | `RowSecurityHandler<T>`, `ClaimScopedRowSecurityHandler`, `PrincipalScopeAccessor` | You want read filters and write/delete guards for rows. |
| Field security adapters | `FieldSecurityAdapter`, `FieldSecurityRuntimeExtension`, generated field metadata | You want read/write field filtering outside basic endpoint authorization. |
| Runtime extension beans | `CrudRuntimeExtension<T, U>` | You want global or application-level before/after hooks in generated service paths. |
| Projection adapter | `ProjectionAdapter` | You want runtime-core to use a projection execution strategy for non-default DTOs. |
| Editable stubs | Generated service/controller stubs marked editable by ownership docs/header | You need entity-specific application code in a generated layer. |
| Spring configuration/properties | Runtime module properties and application beans consumed by auto-configuration | You need to configure runtime behavior without changing generated source. |
| Codegen SPI | `Generator`, `PredicateGeneratorProvider` via `ServiceLoader` | You are extending CrudCraft codegen itself, not normal application behavior. |

## Extension Chain Semantics

`CrudRuntimeExtension<T, U>` has a precise position in `AbstractCrudService`:

| Hook | Called during | Can do |
|---|---|---|
| `readFilter(Class<T>)` | All read paths, including find, count, exists, search, keyset, and visible update/delete loads. | Add a JPA `Specification` that limits visible rows. |
| `beforeCreate(U)` | Before mapping create requests. | Validate or transform request DTOs. |
| `beforeUpdate(U, T)` | Before mapping update, patch, or upsert requests with current entity state. | Validate or transform write DTOs. |
| `beforeSave(T)` | Before repository save. | Enforce row write rules or mutate entity state. |
| `beforeDelete(T)` | Before repository delete. | Enforce delete rules. |
| `afterRead(P)` | After mapping/projection and before returning DTOs. | Filter read fields or transform DTO output. |

Application context extensions run before extensions returned by the generated service's `runtimeExtensions()` override.

## What Is Not An Extension Point

Do not depend on:

- private generated method names;
- generated local variable names;
- JavaPoet formatting;
- writer helper classes under `nl.datasteel.crudcraft.codegen.writer.*`;
- descriptor internals unless you are changing codegen itself;
- endpoint provider implementation classes as application APIs;
- package-private runtime helpers;
- public classes annotated with `@InternalOnly`;
- strict generated files as customization locations;
- sample app classes as reusable contracts.

These details can change as long as documented behavior and public contracts stay compatible.

## Choosing A Hook

| Goal | Use |
|---|---|
| Remove `DELETE` for one entity | `@CrudCrafted(omitEndpoints = CrudEndpoint.DELETE)` or a `CrudEndpointPolicy`. |
| Require `ADMIN` only for export | `@EndpointRbac(endpoint = CrudEndpoint.EXPORT, expression = "hasRole('ADMIN')")`. |
| Limit rows to the current tenant | Built-in row scope annotation or a `RowSecurityHandler<T>`. |
| Hide `internalNotes` on reads and handle illegal writes | `@FieldSecurity` plus the documented field-security write policy. |
| Add global audit logic before saves | A `CrudRuntimeExtension` bean, if it applies across generated services. |
| Fix entity-specific relationship state | Editable service hook when the generated service is the documented owner. |
| Change projection execution | A `ProjectionAdapter` bean or projection runtime configuration. |
| Add a new generated artifact type | `Generator` SPI inside a CrudCraft extension module. |

## Review Rules For New Extension Points

Before exposing a new hook:

- define whether it is compile-time, generated-source, runtime, or starter configuration;
- document lifecycle ordering and failure behavior;
- add tests that use the hook from outside the implementation package;
- decide whether it belongs in API, core, a feature runtime module, or a starter;
- avoid exposing implementation classes when a smaller interface would be stable.

## Related Documentation

- [Runtime Architecture](runtime-architecture.md)
- [Generated Code Lifecycle](generated-code-lifecycle.md)
- [Extensions Feature Guide](../feature-guides/extensions/)
