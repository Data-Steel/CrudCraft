---
title: "CrudCraft Features"
description: "Code-derived checklist of CrudCraft features and the feature-guide pages that document them."
section: "Documentation"
audience:
  - "Application developers"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/feature-guides"
  - "/documentation-inventory"
---

# CrudCraft Features

This checklist is derived from the public annotations, annotation processor, runtime modules, starters, golden tests, and sample application. A checked item has a canonical Feature Guide page.

Use this page to audit documentation coverage. Feature details belong in [Feature Guides](feature-guides/), not in a separate feature reference section. The checklist is not a maturity guarantee; rows with bounded behavior call out that caveat in the feature name and guide.

## Feature Coverage

| Covered | Feature | Canonical documentation |
|---|---|---|
| [x] | Starter selection and module composition | [Choose a Starter](quick-start/choose-a-starter.md), [Runtime Modules](feature-guides/runtime-modules/) |
| [x] | `@CrudCrafted` entity generation | [Annotations](feature-guides/code-generation/annotations.md) |
| [x] | Generated DTOs: request, response, ref, and named variants from `@Dto("List")` | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Request DTO inclusion with `@Request` | [Annotations](feature-guides/code-generation/annotations.md), [Validation](feature-guides/validation/) |
| [x] | Endpoint templates: `FULL`, `READ_ONLY`, `IMMUTABLE_WRITE`, `PATCH_ONLY`, `NO_DELETE`, `NO_BATCH`, `CREATE_ONLY`, `SEARCH_ONLY`, `META_ONLY`, `LIGHT_PUBLIC`, `SECURE_INTERNAL`, `VALIDATION_ONLY` | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Endpoint overlays with `includeEndpoints`, `omitEndpoints`, and custom `CrudEndpointPolicy` | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Generated CRUD endpoints: list, ref list, get one, create, update, patch, delete | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Generated bulk endpoints: create, update, patch, upsert, delete, find by IDs | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Generated support endpoints: exists, count, validate | [Generated Layers](feature-guides/code-generation/generated-layers.md), [Request Validation](feature-guides/validation/request-validation.md) |
| [x] | Editable generated stubs and omitted endpoint comments | [Editable Stubs](feature-guides/code-generation/editable-stubs.md) |
| [x] | Deterministic generated output and golden tests | [Deterministic Output](feature-guides/code-generation/deterministic-output.md), [Code Generation Testing](feature-guides/code-generation/testing.md) |
| [x] | MapStruct mapper generation, relation ID mapping, withers, and builders | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | JPA relationship handling: many-to-one, one-to-one, one-to-many, many-to-many | [Associate Your First Entities](quick-start/associate-your-first-entities.md), [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Embeddable DTO generation | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | JPA inheritance handling for abstract `@CrudCrafted` entities | [Generated Layers](feature-guides/code-generation/generated-layers.md) |
| [x] | Multipart request generation for writable `@Lob` fields and LOB collections | [Generated Layers](feature-guides/code-generation/generated-layers.md), [Export](feature-guides/export/) |
| [x] | `@EnumString` enum value metadata | [Annotations](feature-guides/code-generation/annotations.md) |
| [x] | Search field generation with `@Searchable` | [Search](feature-guides/search/) |
| [x] | Search operators: equality, text, regex, ranges, dates, collections, maps, size operators | [Search Operators](feature-guides/search/operators.md) |
| [x] | Search path/operator validation and sort-path validation | [Filtering](feature-guides/search/filtering.md), [Sorting](feature-guides/search/sorting.md) |
| [x] | Nested search depth and `crudcraft.search.depth` | [Nested Fields](feature-guides/search/nested-fields.md), [Search Configuration](feature-guides/search/configuration.md) |
| [x] | Offset pagination and generated page-size clamp | [Pagination](feature-guides/search/pagination.md), [Runtime Core](feature-guides/runtime-modules/core.md) |
| [x] | Keyset pagination through `SearchOperations.searchKeyset` and core service support | [Pagination](feature-guides/search/pagination.md) |
| [x] | Projection metadata generation and registry | [Projection Configuration](feature-guides/projection/configuration.md) |
| [x] | Projection field selection and `@ProjectionField` path mapping | [Field Selection](feature-guides/projection/field-selection.md), [Projection Paths](feature-guides/projection/projection-paths.md) |
| [x] | Projection runtime execution, JPA criteria adapter, collection hydration | [Projection Runtime Module](feature-guides/runtime-modules/projection.md) |
| [x] | Projection and field-security interaction | [Projection Security Interaction](feature-guides/projection/security-interaction.md), [Field-Level Security](feature-guides/security/field-level-security.md) |
| [x] | Export endpoint generation with `includeEndpoints = CrudEndpoint.EXPORT` | [Export](feature-guides/export/) |
| [x] | Streaming JSON export; bounded CSV/XLSX export with row streaming when an explicit field schema is supplied | [Export](feature-guides/export/) |
| [x] | Export limits: `crudcraft.export.max-csv-rows`, `max-json-rows`, `max-xlsx-rows` (counting root exported rows) | [Export](feature-guides/export/), [Security Configuration](feature-guides/security/configuration.md) |
| [x] | Export field include/exclude, nested dot paths, `maxDepth`, DTO mode, entity mode | [Export](feature-guides/export/) |
| [x] | `@ExportExclude` for entity export metadata | [Export](feature-guides/export/) |
| [x] | Endpoint authorization with `secure = true` and `CrudSecurityPolicy` | [Security Authorization](feature-guides/security/authorization.md) |
| [x] | Annotation-first RBAC with `@CrudSecurity` | [Role-Based Access](feature-guides/security/role-based-access.md) |
| [x] | Per-generated-endpoint RBAC with `@EndpointRbac` | [Role-Based Access](feature-guides/security/role-based-access.md) |
| [x] | Built-in security policies: admin-only, authenticated, deny-all, permit-all, read-public/write-admin, role map, write-only | [Security Authorization](feature-guides/security/authorization.md) |
| [x] | Field-level read redaction with `@FieldSecurity(readRoles = ...)` | [Field-Level Security](feature-guides/security/field-level-security.md) |
| [x] | Field-level write filtering with `writeRoles` and `WritePolicy.SKIP_ON_DENIED` | [Field-Level Security](feature-guides/security/field-level-security.md) |
| [x] | Field-level write rejection with `WritePolicy.FAIL_ON_DENIED` | [Field-Level Security](feature-guides/security/field-level-security.md) |
| [x] | Generated `FieldSecurityMetadata` and recursive DTO filtering | [Field-Level Security](feature-guides/security/field-level-security.md) |
| [x] | Tenant row security with `@TenantScoped` | [Tenant Isolation](feature-guides/security/tenant-isolation.md) |
| [x] | Client row security with `@ClientScoped` | [Tenant Isolation](feature-guides/security/tenant-isolation.md) |
| [x] | Owner row security with `@OwnedBy` and `OwnerBasedRowSecurity` | [Tenant Isolation](feature-guides/security/tenant-isolation.md) |
| [x] | Custom row handlers with `@RowSecurity` and `RowSecurityHandler` | [Tenant Isolation](feature-guides/security/tenant-isolation.md) |
| [x] | Principal claim access from Spring Security authentication | [Authentication](feature-guides/security/authentication.md), [Tenant Isolation](feature-guides/security/tenant-isolation.md) |
| [x] | Jakarta Bean Validation on generated request DTOs and validate endpoint | [Jakarta Validation](feature-guides/validation/jakarta-validation.md), [Request Validation](feature-guides/validation/request-validation.md) |
| [x] | Generated validation error translation | [Generated Errors](feature-guides/validation/generated-errors.md) |
| [x] | Core runtime service operations, extension chain, exception handling, metadata introspection | [Runtime Core](feature-guides/runtime-modules/core.md) |
| [x] | Soft delete embeddable extension | [Extensions](feature-guides/extensions/) |
| [x] | Auditable embeddable extension | [Extensions](feature-guides/extensions/) |
| [x] | Relationship utility helpers | [Extensions](feature-guides/extensions/) |
| [x] | Controller metrics/logging for generated operations | [Runtime Core](feature-guides/runtime-modules/core.md) |
| [x] | Generated Insomnia collection artifacts | [Generated Layers](feature-guides/code-generation/generated-layers.md) |

## Removed Documentation Columns

Separate old reference columns are gone. Exact technical facts now live in the most relevant Feature Guide page, and project/contributor policy stays in the contributor and maintainer handbooks.

## Related documentation

- [Feature Guides](feature-guides/)
- [Documentation Inventory](documentation-inventory.md)
