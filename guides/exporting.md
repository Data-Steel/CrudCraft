---
title: Data Export (CSV/JSON/XLSX)
summary: Stream query results to CSV, JSON, or Excel files with configurable limits.
sidebar: guides
---

# Data Export (CSV/JSON/XLSX)

CrudCraft offers export endpoints to download search results in common formats. Exports stream directly from the database to reduce memory usage.

## Endpoint

```
GET /books/export?format=csv
```

Parameters:

| Name | Description | Default |
|------|-------------|---------|
| `format` | `csv`, `json`, or `xlsx` | `csv` |
| `filename` | Base name for the exported file | entity name |
| `...searchParams` | Any search or paging parameter | — |

## Streaming Behavior

Exports stream row by row using the same search pipeline as list endpoints. Large result sets are delivered incrementally to the client.

## Size Limits

Maximum rows per format are configurable to prevent excessive load. When the limit is exceeded, the export fails with HTTP `413 Payload Too Large`.

## Configuration Properties

```properties
crudcraft.export.max-csv-rows=100000
crudcraft.export.max-json-rows=50000
crudcraft.export.max-xlsx-rows=25000
```

## Content Types and Filenames

| Format | Content Type | Extension |
|--------|--------------|-----------|
| CSV | `text/csv` | `.csv` |
| JSON | `application/json` | `.json` |
| XLSX | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `.xlsx` |

The `filename` parameter controls the download name: `?filename=inventory` results in `inventory.csv`.

## Next Steps

- Combine exports with [Searching & Filtering](/guides/search-and-filtering.md).
- Tune export limits with [Performance & Tuning](/guides/performance.md).
- Protect downloads using [Security Overview](/guides/security/overview.md).

