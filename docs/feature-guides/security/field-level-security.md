---
title: "Field-Level Security"
description: "Redact CrudCraft response fields and control denied writes with FieldSecurity and WritePolicy."
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
  - "/feature-guides/security/tenant-isolation"
  - "/feature-guides/security/testing"
---

# Field-Level Security

Field-level security controls individual DTO properties. CrudCraft generates `FieldSecurityMetadata` into request, response, ref, and named DTOs, and generated controllers call `FieldSecurityUtil` around reads and writes when any field is secured.

Use this page when some fields are more sensitive than the endpoint itself.

## Read filtering

```java
@Dto
@FieldSecurity(readRoles = {"SUPPORT", "ADMIN"})
private String customerEmail;
```

When the current principal lacks those roles, CrudCraft redacts the field in responses. Object fields become `null`; primitive fields are reset to their JVM default because primitives cannot be null. Nested DTOs, arrays, and collections are filtered recursively, with cycle detection.

For collection-valued fields, secure the collection field itself when cardinality is sensitive. If
only item fields are secured, CrudCraft preserves the collection shape and filters each item, so a
caller may still infer how many related items exist. When item count is sensitive, protect the parent
collection field with `@FieldSecurity` or enforce the rule at row-security/query level.

## Write filtering with skip behavior

```java
@Dto
@Request
@FieldSecurity(
    readRoles = "ADMIN",
    writeRoles = "ADMIN",
    writePolicy = WritePolicy.SKIP_ON_DENIED
)
private String internalNote;
```

`SKIP_ON_DENIED` preserves the existing value on update and patch when a caller is not allowed to write the field. On create, where no existing value exists, the denied field is redacted before mapping. Use this for optional fields where the rest of a partial update should still succeed.

## Write filtering with fail behavior

```java
@Dto
@Request
@FieldSecurity(
    readRoles = "ADMIN",
    writeRoles = "ADMIN",
    writePolicy = WritePolicy.FAIL_ON_DENIED
)
private String role;
```

`FAIL_ON_DENIED` throws `AccessDeniedException` when a denied caller supplies the field. Use this for integrity-critical values such as roles, owner IDs, tenant IDs, approval states, prices, balances, and legal flags.

## Interaction with endpoint security and row security

Field security does not decide whether the endpoint can be called and does not decide whether a row is visible. The usual order is:

1. Generated endpoint `@PreAuthorize` runs.
2. Service row filters restrict visible rows.
3. Generated controller and runtime extension apply field write filtering before mapping request data.
4. Generated controller and runtime extension apply read filtering before returning DTOs or writing export rows.

## Export and projections

Generated export endpoints pass `FieldSecurityUtil::filterRead` into `ExportService` when the model has field security. Projection results are also passed through read filtering when they use generated DTO metadata. Do not treat projection as a security feature; secure the field itself.

Nested projections cascade the same field-security metadata as full DTO reads. A secured nested
property is excluded or redacted consistently whether it appears on the full response DTO, a named
DTO variant, a projection DTO, or an export row.

When generated metadata exists but does not contain a rule for a requested field, CrudCraft treats
that field as readable. Generated metadata should list secured fields; missing rules must not hide
unsecured projection or export fields by accident.

## Concurrent filtering contract

`FieldSecurityAdapter` beans are Spring singletons. Implementations must be stateless or read
request-specific roles, tenants, and claims from the active security context or a request-scoped
collaborator. Do not store the current principal, denied-field list, or last filtered DTO in adapter
instance fields.

Read filtering runs once per DTO after mapping/projection and before response emission. The returned
DTO is the value that continues through the service chain. If a denied field is present, return an
immutable copy or otherwise ensure the denied value is removed for that caller only. One concurrent
request's read decision must not affect another request's filtering result.

Set `crudcraft.security.field.assert-filtered=true` in integration tests or development runs to make
`FieldSecurityRuntimeExtension` verify that fields denied by `canReadField` were actually redacted
by `filterRead`.

## Common mistakes

| Mistake                                                 | Result                                                                                | Fix                                                |
|---------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------|
| Using field security instead of endpoint authorization  | Users can still call endpoints and infer state from status codes.                     | Combine `@FieldSecurity` with `@CrudSecurity`.     |
| Using `SKIP_ON_DENIED` for ownership or role fields     | Attack attempts may be silently ignored.                                              | Use `FAIL_ON_DENIED` for high-integrity fields.    |
| Expecting unauthenticated users to pass protected rules | Missing authentication fails closed unless the role is `ALL`.                         | Authenticate or use endpoint policy intentionally. |
| Securing only the response DTO mentally                 | CrudCraft generates metadata for request DTOs too when the source field is annotated. | Test create, update, patch, and bulk writes.       |

## Related documentation

- [Tenant Isolation](tenant-isolation.md)
- [Testing Security](testing.md)
