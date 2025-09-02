---
title: Performance & Tuning
summary: Optimize generated CRUD APIs through paging strategies, indexes, and JVM tuning.
sidebar: guides
---

# Performance & Tuning

CrudCraft aims for efficiency, but production workloads still require tuning. This guide highlights common areas to review when optimizing generated APIs.

## Paging Strategies

- Favor smaller page sizes for interactive clients; large pages increase memory use.
- Set `crudcraft.api.max-page-size` to prevent excessive requests.

## Database Indexes

- Add indexes for frequently filtered fields annotated with `@Searchable`.
- Composite indexes help when combining tenant and ownership predicates.

## Mapper Overhead

MapStruct mappers are fast, but deep object graphs can add cost. Limit nested mappings or mark relations with `@Dto(ref = true)` to avoid fetching entire structures.

## Export Streaming

Exports stream results to reduce heap usage. For huge datasets, increase JDBC fetch size and ensure the database supports streaming queries.

## JVM and DB Tuning Checklist

| Area | Tip |
|------|-----|
| JVM | Use G1GC and size heap according to load. |
| Connection Pool | Monitor pool usage; adjust max connections. |
| Database | Analyze slow queries and add indexes. |
| Batch Operations | Tune batch size and disable second-level cache if not needed. |

## Next Steps

- Learn how exports work in [Data Export](/guides/exporting.md).
- Review [Search Design](/concepts/search-design.md) for query internals.
- Secure your API with [Security Overview](/guides/security/overview.md).

