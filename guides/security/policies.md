---
title: Endpoint Policies
summary: Apply CrudSecurityPolicy to generate @PreAuthorize rules for each endpoint.
sidebar: guides
---

# Endpoint Policies

Endpoint policies determine who can call generated controller methods. CrudCraft derives Spring Security expressions from a `CrudSecurityPolicy` associated with each entity.

## Policy Types

| Policy | Description |
|--------|-------------|
| `PermitAll` | No restrictions; generates no `@PreAuthorize`. |
| `AdminOnly` | Requires `ROLE_ADMIN`. |
| `RoleBased` | Accepts a list of roles for read and write operations. |

## Declaring a Policy

Specify the policy in `@CrudCrafted`:

```java
@CrudCrafted(securityPolicy = @CrudSecurityPolicy(type = CrudSecurityPolicy.Type.ROLE_BASED,
  readRoles = {"ROLE_USER", "ROLE_ADMIN"}, writeRoles = "ROLE_ADMIN"))
public class Book { }
```

Generated controller methods use matching `@PreAuthorize` expressions.

## Mixed Roles Example

`RoleBased` policies may grant different roles per operation:

```java
@CrudSecurityPolicy(
  type = CrudSecurityPolicy.Type.ROLE_BASED,
  readRoles = {"ROLE_USER", "ROLE_EDITOR"},
  writeRoles = {"ROLE_EDITOR"}
)
```

This allows users with `ROLE_USER` to read while only editors may write.

## Testing Policies

Use MockMvc or WebTestClient to verify that unauthorized roles receive `403` responses:

```java
mockMvc.perform(get("/books")).andExpect(status().isForbidden());
```

## Next Steps

- Protect sensitive fields with [Field Security](/guides/security/field-security.md).
- Combine with [Row-Level Security](/guides/security/row-security.md) for tenant isolation.
- Review [Security Overview](/guides/security/overview.md) to plan your strategy.

