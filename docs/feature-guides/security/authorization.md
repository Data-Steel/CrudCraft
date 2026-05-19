---
title: "Security Authorization"
description: "Authorize generated CrudCraft endpoint methods with security policies and annotation-first RBAC."
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
  - "/feature-guides/security/role-based-access"
  - "/feature-guides/security/tenant-isolation"
  - "/feature-guides/security/testing"
---

# Security Authorization

CrudCraft authorization protects generated controller methods. The generator resolves a Spring Security expression for each generated `CrudEndpoint` and writes it as `@PreAuthorize`.

Use this page when roles should control generated reads, writes, deletes, search, count, exists, validation, or export.

## Generated endpoint groups

`@CrudSecurity` starts with three groups:

| Group | Endpoint scope |
|---|---|
| `readRoles` | `GET_ALL`, `GET_ALL_REF`, `GET_ONE`, `FIND_BY_IDS`, `EXISTS`, `COUNT`, `SEARCH`, `EXPORT` |
| `writeRoles` | `POST`, `PUT`, `PATCH`, `BULK_CREATE`, `BULK_UPDATE`, `BULK_PATCH`, `BULK_UPSERT`, `VALIDATE` |
| `deleteRoles` | `DELETE`, `BULK_DELETE` |

Empty role arrays compile to `denyAll()`. Role names may be written as `ADMIN` or `ROLE_ADMIN`; CrudCraft normalizes the `ROLE_` prefix before writing `hasRole`.

## Minimal policy example

```java
@Entity
@CrudCrafted(secure = true, securityPolicy = AdminOnlySecurityPolicy.class)
public class UserAccount {
    @Id
    @Dto(ref = true)
    private UUID id;
}
```

This uses a runtime policy class from `crudcraft-runtime-security`. Every generated endpoint receives `@PreAuthorize("hasRole('ADMIN')")`. Use this when an entire generated API has one rule.

## Annotation-first endpoint RBAC

```java
@Entity
@CrudCrafted(secure = true)
@CrudSecurity(
    readRoles = {"SUPPORT", "ADMIN"},
    writeRoles = "EDITOR",
    deleteRoles = "ADMIN",
    endpoints = {
        @EndpointRbac(endpoint = CrudEndpoint.EXPORT, roles = "ADMIN"),
        @EndpointRbac(endpoint = CrudEndpoint.VALIDATE, roles = {"EDITOR", "ADMIN"})
    }
)
public class Ticket {
    @Id
    @Dto(ref = true)
    private UUID id;
}
```

Here `@CrudSecurity` is the source of truth. Generated read endpoints require support or admin, writes require editor, deletes require admin, and two endpoints get explicit overrides. Use this when generated APIs need more than simple read/write split.

If the same endpoint appears twice in `endpoints`, generation fails. That is intentional because duplicate endpoint rules would make the generated security contract ambiguous.

## Custom policy class

```java
public final class StaffDocumentPolicy implements CrudSecurityPolicy {
    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case GET_ALL, GET_ONE, SEARCH -> "hasAnyRole('SUPPORT', 'ADMIN')";
            case EXPORT, DELETE, BULK_DELETE -> "hasRole('ADMIN')";
            default -> "hasRole('EDITOR')";
        };
    }
}
```

```java
@CrudCrafted(secure = true, securityPolicy = StaffDocumentPolicy.class)
public class StaffDocument { ... }
```

Use a custom `CrudSecurityPolicy` when the expression cannot be represented as role arrays, for example when you need `hasAuthority`, `@bean.method(authentication, #id)`, or a special policy for export.

## How authorization combines with templates

Endpoint authorization is only generated for endpoints that exist. `template`, `includeEndpoints`, `omitEndpoints`, and `endpointPolicy` decide the endpoint set first; security expressions are then applied to the generated methods.

Example: `CrudTemplate.READ_ONLY` with `@CrudSecurity(writeRoles = "ADMIN")` still has no `POST`, `PUT`, or `PATCH` methods unless they are included explicitly.

## Common mistakes

| Mistake | Result | Fix |
|---|---|---|
| Setting `secure = true` without a policy or `@CrudSecurity` | Every generated endpoint defaults to `isAuthenticated()`. | Use this only when authentication is enough. |
| Protecting writes but not reads | List, search, count, exists, and export can still expose data. | Set `readRoles` and test every read-like endpoint. |
| Expecting row scopes to replace endpoint roles | A scoped row can still be reached by any authorized endpoint caller. | Combine endpoint roles with row scopes. |
| Using `@CrudSecurity` with an empty role group | That group becomes `denyAll()`. | Provide explicit roles for every endpoint group you generate. |

## Related documentation

- [Role-Based Access](role-based-access.md)
- [Tenant Isolation](tenant-isolation.md)
- [Testing Security](testing.md)
