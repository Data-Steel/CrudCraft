---
title: Security Overview
summary: Understand CrudCraft's layered security model and choose the right approach.
sidebar: guides
---

# Security Overview

CrudCraft applies security at three distinct layers: endpoints, fields, and rows. Each layer addresses different concerns and can be combined for comprehensive protection.

## Endpoint Policies

Endpoint policies restrict access to whole controller methods using `@PreAuthorize`. The generated controller consults `CrudSecurityPolicy` to derive expressions. Policies range from `PermitAll` to `RoleBased`.

## Field Security

Field-level rules govern read and write access within DTOs. The `@FieldSecurity` annotation lets you specify roles that may view or modify a property. Filters like `filterRead` and `filterWrite` can mutate DTOs before serialization or persistence.

## Row-Level Security

Row security limits which entity instances a user may interact with. Implement a `RowSecurityHandler` to provide JPA `Specification` or QueryDSL predicates for reads and writes. These predicates are enforced automatically by repositories and services.

## Choosing a Layer

| Scenario | Recommended Layer |
|----------|------------------|
| Block an endpoint for unauthorized users | Endpoint policy |
| Hide or protect individual fields | Field security |
| Restrict data to tenant or owner | Row security |

Most applications combine all three. For example, a controller might be limited to admins, while certain fields are visible to editors and rows filtered by tenant.

## Next Steps

- Configure policies in [Endpoint Policies](/guides/security/policies.md).
- Apply field-level restrictions via [Field Security](/guides/security/field-security.md).
- Enforce tenant isolation with [Row-Level Security](/guides/security/row-security.md).

