---
title: Field Security
summary: Control who can read or modify DTO properties using @FieldSecurity.
sidebar: guides
---

# Field Security

Field security protects individual DTO properties. Use `@FieldSecurity` to define who may read or write a field and how unauthorized writes are handled.

## Basic Usage

Annotate an entity field or DTO property with `@FieldSecurity`:

```java
@Dto
@Request
@FieldSecurity(readRoles = {"ROLE_ADMIN", "ROLE_EDITOR"}, writeRoles = "ROLE_ADMIN")
private String internalNotes;
```

- Only admins and editors may view `internalNotes`.
- Only admins may set or update the field.

## Write Policies

`writePolicy` dictates behavior when a caller lacks write permission:

- `FAIL` (default) — rejects the request with `403 Forbidden`.
- `SKIP` — silently ignores the field and continues processing.

```java
@FieldSecurity(writeRoles = "ROLE_EDITOR", writePolicy = WritePolicy.SKIP)
private String reviewerComment;
```

In this example, non-editors can submit a request containing `reviewerComment` but the value is dropped.

## Filtering Hooks

`filterRead` and `filterWrite` allow you to mutate values when access is granted:

```java
@FieldSecurity(filterRead = MaskingFilter.class)
private String ssn;
```

Implement `MaskingFilter` to mask sensitive data before serialization.

## Nested DTOs

Field security applies recursively to nested DTOs. Ensure reference DTOs also declare `@FieldSecurity` where required; otherwise nested fields may leak sensitive data.

## Common Pitfalls

- Omitting `@Dto` on secured fields means the value never leaves the entity, so `@FieldSecurity` is unnecessary.
- Misconfigured role names result in open access; always test security rules.

## Next Steps

- Enforce per-row access with [Row-Level Security](/guides/security/row-security.md).
- Review high-level options in [Security Overview](/guides/security/overview.md).
- Test security rules as shown in [Testing Generated APIs](/guides/testing.md).

