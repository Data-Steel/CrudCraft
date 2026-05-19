# Configuration Reference

This page is the authoritative reference for CrudCraft Spring and processor properties.

| Property | Type | Default | Scope | Description |
|---|---:|---:|---|---|
| `crudcraft.api.max-page-size` | integer | `200` | runtime-core | Maximum pageable size accepted by generated controllers. Requests above the limit fail with 400. |
| `crudcraft.export.max-rows` | integer | `-1` | runtime-export | Optional global maximum rows for every export format before HTTP 413. Non-positive disables the global cap. |
| `crudcraft.export.max-csv-rows` | integer | `100000` | runtime-export | Maximum rows for CSV export before HTTP 413. |
| `crudcraft.export.max-json-rows` | integer | `50000` | runtime-export | Maximum rows for JSON export before HTTP 413. |
| `crudcraft.export.max-xlsx-rows` | integer | `25000` | runtime-export | Maximum rows for XLSX export before HTTP 413. |
| `crudcraft.export.max-page-size` | integer | `1000` | runtime-export | Internal page size used while streaming export results. |
| `crudcraft.export.max-depth` | integer | `5` | runtime-export | Default maximum entity relationship depth when `ExportRequest.maxDepth` is omitted. `0` excludes relationships. |
| `crudcraft.export.must-fetch` | boolean | `false` | runtime-export | Failed entity-mode relationship prefetching throws instead of warning and falling back to lazy loading. |
| `crudcraft.search.depth` | integer | `1` | codegen/search | Default maximum generated nested searchable path depth. Must be positive; `0` and negative values fail fast. |
| `crudcraft.projection.max-depth` | integer | `5` | runtime-projection | Maximum nested projection metadata depth accepted by the JPA executor. |
| `crudcraft.projection.warn-on-collection-hydration` | boolean | `true` | runtime-projection | Logs a performance warning when a projection hydrates collection attributes with a secondary query. |
| `crudcraft.embeddable.maxDepth` | integer | `5` | codegen | Maximum nested embeddable depth accepted by the annotation processor. |
| `crudcraft.dto.generateWithers` | boolean | `false` | codegen | Generates DTO wither methods when enabled. |
| `crudcraft.insomnia.outputDir` | path | unset | codegen | Optional directory for generated Insomnia request collections. |

Invalid values fail fast during request handling or annotation processing. Prefer configuring row and page limits in production rather than relying on defaults.
