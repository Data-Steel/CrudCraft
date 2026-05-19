---
title: "Export"
description: "Generate and use CrudCraft CSV, JSON, and XLSX export endpoints with field selection, limits, search, and security."
section: "Feature Guides"
category: "Export"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-export"
  - "crudcraft-spring-boot-starter-export"
  - "crudcraft-codegen"
related:
  - "/feature-guides/search/filtering"
  - "/feature-guides/security/field-level-security"
  - "/feature-guides/runtime-modules/export"
  - "/features"
---

# Export

CrudCraft can generate `GET /{resources}/export` endpoints for CSV, JSON, or XLSX. JSON is row-streamed. CSV and XLSX are row-streamed when `includeFields` declares the output schema; without that schema they buffer flattened rows for the request so headers can include the union of fields seen across all rows. Export can reuse generated search filters, apply field-security read filtering, and limit output per format.

Use this page when users need to download generated API data.

## Enable export for one entity

```java
@Entity
@CrudCrafted(includeEndpoints = CrudEndpoint.EXPORT)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto(ref = true)
    private UUID id;

    @Dto
    @Request
    @Searchable
    private String number;

    @Dto
    @Request
    private BigDecimal amount;

    @ExportExclude
    private String internalAccountingCode;
}
```

`CrudTemplate.FULL` does not include export by default. Add `includeEndpoints = CrudEndpoint.EXPORT` when the generated controller should expose `/invoices/export`.

## Call the endpoint

```http
GET /invoices/export?format=csv&limit=500&includeFields=number&includeFields=amount
```

Supported `format` values are `csv`, `json`, and `xlsx`. Values are trimmed and lowercased; anything else returns `400 Bad Request`. Negative limits also return `400`. `limit=0` returns a valid empty export.

## Search-aware export

If the entity has searchable fields, the generated export endpoint accepts the generated search request as model attributes:

```http
GET /invoices/export?format=xlsx&number=INV-2026&numberOp=STARTS_WITH
```

The generated body calls `SearchOperations.search(service, searchRequest, pageable, InvoiceResponseDto.class)` page by page. Use this when exports should match the same filters as the on-screen search results.

## Field selection

`ExportRequest` supports:

| Option | Behavior |
|---|---|
| `includeFields` | If present, only these fields or descendants are exported. |
| `excludeFields` | Always wins over inclusion. Excluding `author` also excludes `author.name`. |
| `maxDepth` | Overrides `crudcraft.export.max-depth`, which defaults to `5`; negative values become `0`. |
| `exportMode` | Defaults to `DTO`; `ENTITY` is disabled by default and requires `crudcraft.export.allow-entity-mode=true`. |

Example:

```http
GET /posts/export?format=json&includeFields=title&includeFields=author.name&excludeFields=author.email&maxDepth=2
```

Use DTO mode for normal API exports. Use entity mode when an admin workflow needs dynamic entity fields and relationship traversal; `@ExportExclude` is enforced by entity export metadata.

## DTO Mode vs Entity Mode

| Mode | Query source | Field security | Relationship behavior | Recommended use |
|---|---|---|---|---|
| `DTO` | Generated service search returning response DTOs. | Applies generated DTO field-security redaction before writing rows. | Exports DTO graph only; field selection filters the serialized DTO shape. | Public or user-facing exports. |
| `ENTITY` | Entity export service using JPA metadata and controlled relationship prefetching. | Enforces `@ExportExclude`; DTO-specific redaction is not part of this path. | Validates `maxDepth` before `JOIN FETCH`; relationships beyond depth are skipped unless explicitly requested, in which case the request fails with `400`. | Trusted internal/admin exports that need dynamic entity fields. |

Treat `includeFields`, `excludeFields`, and cursors as request contracts, not stable file schemas. If an export fails because a requested relationship exceeds `maxDepth`, retry with a lower field selection or a higher `maxDepth` that is still within your production limit.

## Limits

Generated controllers inject these properties:

| Property | Default |
|---|---|
| `crudcraft.export.max-rows` | `-1` (disabled; per-format limits apply) |
| `crudcraft.export.max-csv-rows` | `100000` |
| `crudcraft.export.max-json-rows` | `50000` |
| `crudcraft.export.max-xlsx-rows` | `25000` |
| `crudcraft.api.max-page-size` | `100` |
| `crudcraft.export.max-depth` | `5` |

The requested limit is checked before writing the response. `crudcraft.export.max-rows` is a global
cap across all formats; when it is positive, CrudCraft uses the lower value of the global cap and the
format-specific cap. Rows are fetched page by page and security-filtered before serialization.
JSON writes rows incrementally. CSV and XLSX compute a stable union header from the flattened result
rows before writing, so they may buffer flattened rows up to the configured export limit. `maxDepth`
is enforced in both DTO and entity mode before field traversal starts. Export limits count root
exported rows (for example, one order row with nested line-items counts as one row). Nested
collections remain embedded inside the exported row payload.
When the effective limit is reached, streaming stops at that boundary and returns the truncated
result set up to the allowed row count.

## Security interaction

Endpoint RBAC applies before export starts. Row security applies through the generated service query. Field security applies row-by-row because generated export endpoints pass `FieldSecurityUtil::filterRead` when the model has secured fields.

## Related documentation

- [Search Filtering](../search/filtering.md)
- [Field-Level Security](../security/field-level-security.md)
- [Export Runtime Module](../runtime-modules/export.md)
