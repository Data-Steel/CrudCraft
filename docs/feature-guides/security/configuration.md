---
title: "Security Configuration"
description: "Configure the CrudCraft security module, generated security hooks, and required Spring beans."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-security"
  - "crudcraft-runtime-security"
related:
  - "/feature-guides/security/authentication"
  - "/feature-guides/security/authorization"
  - "/feature-guides/security/tenant-isolation"
---

# Security Configuration

CrudCraft security configuration is mostly dependency and generated-code configuration. Your application still owns Spring Security's filter chain and authentication provider.

Use this page when adding CrudCraft security to an application that already has or will have Spring Security.

## Dependency

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-security</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

The security starter provides the runtime field-security adapter, principal scope accessor, policy implementations, and row-security runtime support. The umbrella starter also includes it.

## Generated security switches

```java
@CrudCrafted(secure = true)
```

`secure = true` tells codegen to add `@PreAuthorize` to generated endpoint methods. Without `securityPolicy` or `@CrudSecurity`, the default generated expression is `isAuthenticated()`.

```java
@CrudSecurity(readRoles = "USER", writeRoles = "EDITOR", deleteRoles = "ADMIN")
```

`@CrudSecurity` is annotation-first RBAC. It also makes the model secure even when `secure = true` was not set explicitly.

```java
@TenantScoped(field = "tenantId", claim = "tenant_id")
```

Claim scopes also make the model secure and add row-security runtime extensions to the generated service.

## Required Spring Security state

CrudCraft expects:

- `SecurityContextHolder` contains an `Authentication`;
- authorities contain role names such as `ROLE_ADMIN` or `ADMIN`;
- claims needed by row scopes are available on principal, details, credentials, claims map, or `Authentication.getName()` for `sub`;
- method security is enabled in the application, for example with `@EnableMethodSecurity`.

## Generated properties used by secure endpoints

| Property                         | Default  | Used by                                         |
|----------------------------------|----------|-------------------------------------------------|
| `crudcraft.api.max-page-size`    | `100`    | Generated list/search/export pagination clamps. |
| `crudcraft.export.max-csv-rows`  | `100000` | Generated export endpoint.                      |
| `crudcraft.export.max-json-rows` | `50000`  | Generated export endpoint.                      |
| `crudcraft.export.max-xlsx-rows` | `25000`  | Generated export endpoint.                      |

## Related documentation

- [Authentication](authentication.md)
- [Security Authorization](authorization.md)
- [Tenant Isolation](tenant-isolation.md)
