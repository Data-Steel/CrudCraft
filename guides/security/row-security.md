---
title: Row-Level Security
summary: Restrict which entity instances users can access with @RowSecurity and custom handlers.
sidebar: guides
---

# Row-Level Security

Row security limits data access to records a user is permitted to see or modify. CrudCraft evaluates a `RowSecurityHandler` for each request to obtain filtering predicates.

## Declaring Row Security

Annotate the entity with `@RowSecurity` and reference a handler:

```java
@CrudCrafted
@RowSecurity(handler = TenantRowSecurity.class)
public class Book { ... }
```

## Implementing RowSecurityHandler

A handler returns a JPA `Specification` or QueryDSL predicate describing allowed rows:

```java
public class TenantRowSecurity implements RowSecurityHandler<Book> {
  @Override
  public Specification<Book> readPredicate(Authentication auth) {
    UUID tenantId = ((UserPrincipal) auth.getPrincipal()).tenantId();
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  @Override
  public Specification<Book> writePredicate(Authentication auth) {
    return readPredicate(auth);
  }
}
```

`readPredicate` filters queries; `writePredicate` validates entities on create or update. Returning `null` means no restriction.

## Enforcement

Repositories apply the read predicate automatically on find operations. Services invoke `apply()` before persisting changes to enforce the write predicate. If a write predicate rejects an entity, `AccessDeniedException` is thrown.

## Multitenancy Example

Combining tenant and ownership checks:

```java
return (root, query, cb) -> cb.and(
  cb.equal(root.get("tenantId"), tenantId),
  cb.equal(root.get("ownerId"), currentUserId)
);
```

## Next Steps

- Secure fields with [Field Security](/guides/security/field-security.md).
- Configure endpoint restrictions via [Endpoint Policies](/guides/security/policies.md).
- Review [Security Overview](/guides/security/overview.md) for the big picture.

