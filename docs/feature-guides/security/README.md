---
title: "Security"
description: "Protect generated CrudCraft APIs with endpoint RBAC, field filtering, row scopes, and Spring Security authentication."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
  - "crudcraft-runtime-security"
  - "crudcraft-spring-boot-starter-security"
related:
  - "/feature-guides/security/authentication"
  - "/feature-guides/security/authorization"
  - "/feature-guides/security/field-level-security"
  - "/feature-guides/security/tenant-isolation"
  - "/architecture/security-model"
  - "/features"
---

# Security

CrudCraft security is a generated-code feature and a runtime feature. The generator writes endpoint authorization and metadata into the generated controllers and DTOs; `crudcraft-runtime-security` enforces field and row rules at runtime.

Use this guide when generated APIs must be protected by roles, tenant/client/owner claims, or per-field read/write rules.

## Security layers

| Layer | Configure with | Enforced by | Use it for |
|---|---|---|---|
| Authentication | Your Spring Security setup | Spring Security | Supplying the principal, roles, and claims CrudCraft reads. |
| Endpoint authorization | `secure = true`, `securityPolicy`, `@CrudSecurity`, `@EndpointRbac` | generated `@PreAuthorize` | Allowing or denying concrete generated endpoints. |
| Field security | `@FieldSecurity` and `WritePolicy` | `FieldSecurityUtil` and generated DTO metadata | Redacting response fields and rejecting or ignoring denied writes. |
| Row security | `@TenantScoped`, `@ClientScoped`, `@OwnedBy`, `@RowSecurity` | `RowSecurityRuntimeExtension` | Restricting list/search/count/read/update/delete/export to rows owned by the current scope. |

These layers are additive. A tenant-scoped endpoint can still require a role, and a visible row can still have fields redacted from its response.

## Isolation guarantees

CrudCraft applies endpoint authorization before generated controller logic and applies row filters inside generated service queries. List, search, count, get-one, update, patch, delete, and export paths all combine generated row filters with any `CrudRuntimeExtension.readFilter(...)` constraints. A row hidden by tenant, client, owner, or custom row security is treated as not visible to the caller.

Field security is applied after mapping and before DTOs leave the generated runtime path. Generated field-security metadata defines which fields are sensitive; the active `FieldSecurityAdapter` is responsible for redaction and write filtering. If you replace the default adapter, add E2E tests that prove denied fields are absent from create, get-one, list, search, projection, and export responses.

Generated `@PreAuthorize` annotations reflect compile-time `@CrudSecurity` and `@EndpointRbac` metadata. If a custom runtime policy is stricter than those annotations, the runtime policy is the effective boundary; keep the annotation metadata and runtime policy aligned so IDE checks, generated OpenAPI descriptions, and production behavior tell the same story.

## Recommended reading order

| Page | Read when |
|---|---|
| [Authentication](authentication.md) | You need to know what CrudCraft expects from Spring Security. |
| [Authorization](authorization.md) | You want endpoint-level rules for generated CRUD endpoints. |
| [Role-Based Access](role-based-access.md) | You want `@CrudSecurity` and per-endpoint `@EndpointRbac` examples. |
| [Tenant Isolation](tenant-isolation.md) | You need tenant, client, owner, or combined row isolation. |
| [Field-Level Security](field-level-security.md) | You need response redaction and write denial behavior. |
| [Configuration](configuration.md) | You need dependencies, generated properties, and runtime beans. |
| [Testing](testing.md) | You need integration tests for roles, scopes, fields, search, and export. |
| [Troubleshooting](troubleshooting.md) | Security behaves differently than expected. |

## Complete example

```java
@Entity
@CrudCrafted(includeEndpoints = CrudEndpoint.EXPORT, secure = true)
@CrudSecurity(
    readRoles = "USER",
    writeRoles = "EDITOR",
    deleteRoles = "ADMIN",
    endpoints = {
        @EndpointRbac(endpoint = CrudEndpoint.EXPORT, roles = "ADMIN"),
        @EndpointRbac(endpoint = CrudEndpoint.BULK_DELETE, roles = "ADMIN")
    }
)
@TenantScoped(field = "tenantId", claim = "tenant_id")
@ClientScoped(field = "clientId", claim = "client_id")
@OwnedBy(field = "ownerId", claim = "sub")
public class WorkspaceDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto(ref = true)
    private UUID id;

    @Dto
    @Request
    @Searchable
    private String title;

    @Dto
    @Request
    private String tenantId;

    @Dto
    @Request
    private String clientId;

    @Dto
    @Request
    private String ownerId;

    @Dto
    @Request
    @FieldSecurity(
        readRoles = {"SUPPORT", "ADMIN"},
        writeRoles = "ADMIN",
        writePolicy = WritePolicy.FAIL_ON_DENIED
    )
    private String legalHoldReason;
}
```

This example does four CrudCraft-specific things:

1. `secure = true` makes generated endpoint methods receive `@PreAuthorize`.
2. `@CrudSecurity` maps broad read/write/delete roles to generated endpoint groups and then overrides `EXPORT` and `BULK_DELETE`.
3. `@TenantScoped`, `@ClientScoped`, and `@OwnedBy` generate claim-based row handlers. CrudCraft combines them with `AND`, so a row must match all three claims.
4. `@FieldSecurity` keeps `legalHoldReason` out of normal responses and rejects non-admin writes instead of silently preserving the old value.

## What CrudCraft does not provide

CrudCraft does not authenticate users, mint JWTs, configure OAuth providers, or decide your password policy. It expects a Spring Security `Authentication` in `SecurityContextHolder` with authorities and claims that match your annotations.

## Related documentation

- [Authorization](authorization.md)
- [Tenant Isolation](tenant-isolation.md)
- [Field-Level Security](field-level-security.md)
- [Security Model](../../architecture/security-model.md)
- [Feature Coverage](../../features.md)
