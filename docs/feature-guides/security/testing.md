---
title: "Testing Security"
description: "Test CrudCraft endpoint RBAC, row scopes, field filtering, and export security."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
  - "crudcraft-codegen"
related:
  - "/feature-guides/security/authorization"
  - "/feature-guides/security/tenant-isolation"
  - "/feature-guides/security/field-level-security"
---

# Testing Security

CrudCraft security tests should cover generated endpoints, service-level row filters, field redaction, denied writes, search, and export. Testing only one controller method is not enough because the generator applies rules across endpoint families.

Use this page when adding or reviewing security behavior.

## Endpoint RBAC test

```java
mockMvc.perform(get("/customercases")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPPORT"))))
    .andExpect(status().isOk());

mockMvc.perform(get("/customercases/export").param("format", "csv")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUPPORT"))))
    .andExpect(status().isForbidden());
```

This verifies an `@EndpointRbac(endpoint = EXPORT, roles = "ADMIN")` override instead of only the broad `readRoles`.

## Row-scope test

```java
mockMvc.perform(get("/privatenotes/search")
        .param("title", "roadmap")
        .with(jwt().jwt(jwt -> jwt
            .claim("tenant_id", "tenant-a")
            .claim("sub", "user-1"))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.content[*].tenantId").value(everyItem(is("tenant-a"))))
    .andExpect(jsonPath("$.content[*].ownerId").value(everyItem(is("user-1"))));
```

Search is important because CrudCraft combines the generated search specification with row security filters in the service.

## Field write policy test

```java
mockMvc.perform(patch("/users/{id}", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"role":"ADMIN"}
            """)
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
    .andExpect(status().isForbidden());
```

Use this for fields with `WritePolicy.FAIL_ON_DENIED`. For `SKIP_ON_DENIED`, assert the response succeeds and the persisted field remains unchanged.

## Export test

```java
mockMvc.perform(get("/privatenotes/export")
        .param("format", "csv")
        .param("limit", "100")
        .with(jwt().jwt(jwt -> jwt.claim("tenant_id", "tenant-a").claim("sub", "user-1"))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
    .andExpect(status().isOk())
    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".csv")));
```

Export should be tested because it streams multiple rows and uses the generated service query plus field-security filter.

## Related documentation

- [Security Authorization](authorization.md)
- [Tenant Isolation](tenant-isolation.md)
- [Field-Level Security](field-level-security.md)
