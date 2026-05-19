---
title: "Tenant Isolation"
description: "Apply CrudCraft row security with tenant, client, owner, and custom row handlers."
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
related:
  - "/feature-guides/security/authorization"
  - "/feature-guides/security/field-level-security"
  - "/feature-guides/security/testing"
---

# Tenant Isolation

Tenant isolation in CrudCraft is row-level security. Generated services add `ClaimScopedRowSecurityHandler` instances for `@TenantScoped`, `@ClientScoped`, and `@OwnedBy`, then combine their filters with `AND`.

Use this page for multi-tenant SaaS data, agency/client portals, user-owned records, or combinations of those scopes.

## What row security affects

Row security is applied in the service layer, so it affects generated and custom controller methods that call the generated service:

| Operation                                          | Behavior                                                                 |
|----------------------------------------------------|--------------------------------------------------------------------------|
| list, ref list, search, count, exists, find by IDs | Adds row filters before querying.                                        |
| get one                                            | Returns not found when the row exists but is outside scope.              |
| create                                             | Sets an unset scope field from the current claim, or rejects mismatches. |
| update, patch, upsert                              | Loads only visible rows and rejects scope mismatches before saving.      |
| delete, bulk delete                                | Deletes only visible rows and applies scope guards before deletion.      |
| export                                             | Streams only rows visible through the service query.                     |

## Tenant plus owner example

```java
@Entity
@CrudCrafted(secure = true, includeEndpoints = CrudEndpoint.EXPORT)
@CrudSecurity(readRoles = "USER", writeRoles = "USER", deleteRoles = "ADMIN")
@TenantScoped(field = "tenantId", claim = "tenant_id")
@OwnedBy(field = "ownerId", claim = "sub")
public class PrivateNote {
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
    private String ownerId;
}
```

This is useful for personal records inside a tenant. A user can only see rows where `tenantId == tenant_id` and `ownerId == sub`. On create, CrudCraft fills unset `tenantId` and `ownerId` from the claims. If a client sends another tenant or owner, the runtime throws `AccessDeniedException`.

## Tenant plus client example

```java
@TenantScoped(field = "tenantId", claim = "tenant_id")
@ClientScoped(field = "clientId", claim = "client_id")
public class ClientInvoice { ... }
```

Use this when one authenticated principal can belong to a tenant but should only access one client workspace inside it. This fits partner portals, agency dashboards, B2B customer accounts, or white-label applications.

## Custom row handler

```java
@RowSecurity(handlers = RegionRowSecurity.class)
public class SalesAccount { ... }
```

Generated services receive handler beans through their constructor and add them to the same `RowSecurityRuntimeExtension` as claim scopes. Use this when the filter cannot be expressed as one field matching one claim.

## Claim sources

`SpringSecurityPrincipalScopeAccessor` reads claims from:

- `Authentication.getPrincipal()` when it is a `Map`, `Principal`, `UserDetails`, or has `getClaim`, `getClaimAsString`, or `getClaims`;
- `Authentication.getDetails()`;
- `Authentication.getCredentials()`;
- `Authentication.getName()` when the requested claim is `sub`.

## Common mistakes

| Mistake                                                 | Result                                                | Fix                                                               |
|---------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------------------|
| Trusting request body scope values                      | Clients can attempt cross-tenant writes.              | Let CrudCraft fill unset scope fields and reject mismatches.      |
| Omitting scope fields from request DTOs without setters | The handler cannot set the scope value.               | Ensure the entity property has readable/writable JavaBean access. |
| Using only endpoint RBAC for tenant data                | Authorized users can see all rows for that endpoint.  | Add row scopes.                                                   |
| Forgetting export                                       | Export uses the service, but it can stream many rows. | Include export in tests for scoped users.                         |

## Related documentation

- [Security Authorization](authorization.md)
- [Field-Level Security](field-level-security.md)
- [Testing Security](testing.md)
