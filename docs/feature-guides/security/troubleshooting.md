---
title: "Security Troubleshooting"
description: "Diagnose CrudCraft authorization, row security, field filtering, and claim-scope failures."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
related:
  - "/feature-guides/security/configuration"
  - "/feature-guides/security/testing"
---

# Security Troubleshooting

Security failures usually come from the wrong layer: endpoint authorization, missing authentication, missing claims, row mismatch, or field write policy.

Use this page when generated endpoints return 401, 403, 404, missing fields, or unchanged fields.

## Symptoms

| Symptom                               | Likely cause                                            | Check                                                                |
|---------------------------------------|---------------------------------------------------------|----------------------------------------------------------------------|
| 401                                   | No authenticated principal reached Spring Security.     | Filter chain, token parsing, test setup.                             |
| 403 before controller body            | Generated `@PreAuthorize` denied the endpoint.          | `@CrudSecurity`, `@EndpointRbac`, `securityPolicy`, method security. |
| 404 for an existing ID                | Row security filtered the row.                          | Tenant/client/owner claims and entity scope fields.                  |
| Field is `null` in response           | `@FieldSecurity(readRoles = ...)` redacted it.          | Current authorities and generated DTO metadata.                      |
| Write succeeds but field is unchanged | `WritePolicy.SKIP_ON_DENIED`.                           | Use admin role or change to `FAIL_ON_DENIED`.                        |
| Write fails with access denied        | `WritePolicy.FAIL_ON_DENIED` or scope mismatch.         | Field roles, tenant/client/owner request values.                     |
| Export returns fewer rows             | Row security and search filters apply before streaming. | Test same principal on list/search first.                            |

## Debug order

1. Confirm the endpoint exists for the selected `CrudTemplate`, `includeEndpoints`, and `omitEndpoints`.
2. Inspect the generated controller method for `@PreAuthorize`.
3. Confirm `@EnableMethodSecurity` is active.
4. Confirm the current authorities match generated role expressions.
5. For scopes, inspect claims read by `SpringSecurityPrincipalScopeAccessor`.
6. For field issues, inspect generated `fieldSecurityMetadata()` on request and response DTOs.
7. Test list/search/export separately because they exercise different paths.

## Related documentation

- [Security Configuration](configuration.md)
- [Testing Security](testing.md)
