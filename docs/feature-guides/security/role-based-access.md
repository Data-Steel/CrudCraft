---
title: "Role-Based Access"
description: "Use CrudCraft role arrays and per-endpoint overrides for generated CRUD APIs."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
related:
  - "/feature-guides/security/authorization"
  - "/feature-guides/security/testing"
---

# Role-Based Access

Role-based access in CrudCraft is endpoint-specific. `@CrudSecurity` maps roles to endpoint groups, while `@EndpointRbac` overrides one concrete `CrudEndpoint`.

Use this page when a generated API should have different roles for list, search, export, write, or delete operations.

## Practical example: support users can read, editors can write, admins export

```java
@Entity
@CrudCrafted(
    template = CrudTemplate.NO_DELETE,
    includeEndpoints = CrudEndpoint.EXPORT,
    secure = true
)
@CrudSecurity(
    readRoles = {"SUPPORT", "EDITOR", "ADMIN"},
    writeRoles = {"EDITOR", "ADMIN"},
    endpoints = {
        @EndpointRbac(endpoint = CrudEndpoint.EXPORT, roles = "ADMIN"),
        @EndpointRbac(endpoint = CrudEndpoint.BULK_UPSERT, roles = "ADMIN")
    }
)
public class CustomerCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto(ref = true)
    private UUID id;

    @Dto
    @Request
    @Searchable
    private String subject;
}
```

This is not generic Spring Security configuration. CrudCraft uses the annotation during generation and writes endpoint methods like:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'SUPPORT')")
@GetMapping
public ResponseEntity<PaginatedResponse<CustomerCaseResponseDto>> getAll(Pageable pageable) { ... }

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/export")
public ResponseEntity<StreamingResponseBody> export(...) { ... }
```

Use this pattern when export carries more risk than normal reads, or when bulk upsert should be stricter than single write.

## Role normalization

CrudCraft normalizes `ROLE_ADMIN` to `ADMIN` while generating expressions. The generated expression still uses `hasRole('ADMIN')`, so both common authority styles work with Spring Security's role prefix behavior.

## Override precedence

`@EndpointRbac` wins over group roles for the same endpoint. For example, `readRoles = "USER"` would normally apply to `EXPORT`, but an `EXPORT` override with `roles = "ADMIN"` makes export admin-only.

## Choosing roles per endpoint

| Endpoint | Typical role choice | Why |
|---|---|---|
| `GET_ALL`, `GET_ONE`, `SEARCH` | Reader, support, user | These return data and must match the audience for normal reads. |
| `COUNT`, `EXISTS` | Usually same as reads | Counts can leak tenant or workflow state. |
| `POST`, `PUT`, `PATCH` | Editor or admin | These mutate persisted data. |
| `BULK_UPSERT`, `BULK_DELETE` | Admin | Bulk operations have higher blast radius. |
| `EXPORT` | Admin or auditor | Export streams many rows and can include selected fields. |
| `VALIDATE` | Same as writes | It validates write DTOs and reveals accepted shape. |

## Related documentation

- [Security Authorization](authorization.md)
- [Testing Security](testing.md)
