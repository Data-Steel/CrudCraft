---
title: "Repository Structure"
description: "Understand the top-level CrudCraft repository layout and where source, tests, docs, workflows, scripts, and generated artifacts belong."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/contributor-handbook/module-overview"
  - "/architecture/module-boundaries"
  - "/contributor-handbook/coding-standards"
---

# Repository Structure

CrudCraft is organized around product boundaries, not convenience. Before editing, decide whether the change affects public annotations, compile-time generation, generated source shape, runtime behavior, starter composition, sample integration, repository tooling, or docs.

## Top-Level Areas

| Path | Owns |
|---|---|
| `crudcraft-api/` | Public annotations, enums, policy interfaces, and source-level contracts. |
| `crudcraft-codegen/` | Annotation processors, descriptor extraction, JavaPoet writers, generated metadata, golden fixtures, compile tests. |
| `crudcraft-runtime-core/` | Shared generated-service behavior, response/error models, pagination/keyset support, exception handler, extension chain. |
| `crudcraft-runtime-search/` | Search request contracts, allowed path/operator/sort validation, search delegation. |
| `crudcraft-runtime-projection/` | Projection metadata registry, JPA criteria execution, projection adapter. |
| `crudcraft-runtime-export/` | Export request model, DTO/entity export services, CSV/JSON/XLSX streaming. |
| `crudcraft-runtime-security/` | Endpoint policy helpers, field filtering, row isolation, principal scope access. |
| `crudcraft-runtime-extensions/` | Reusable embeddables and relationship utilities used by generated services. |
| `crudcraft-starter-*/` | Capability-specific Spring Boot starter artifacts and auto-configuration exposure. |
| `crudcraft-starter/` | Umbrella starter aggregating all capability starters. |
| `crudcraft-sample-app/` | Integrated generated API scenarios and TCK-style coverage. |
| `crudcraft-tools/` | Repository maintenance tools and quality helpers. |
| `benchmarks/` | JMH performance coverage used by CI. |
| `docs/` | Canonical written documentation source. |
| `docs-deploy/` | VitePress build/deploy tooling for docs, Javadocs, and source browser. |
| `scripts/` | PowerShell quality, docs, and drift helper scripts. |
| `.github/workflows/` | CI, dependency scan, release, docs dispatch, and reproducible-build automation. |

## Derived Or Generated Output

Do not edit these as source:

- `target/`;
- generated Java under `target/generated-sources/annotations`;
- docs-deploy generated site output;
- generated quality reports;
- downloaded dependency or build artifacts.

Change the source that creates them instead.

## Common Placement Decisions

| Change | Primary location | Usually also touches |
|---|---|---|
| New annotation attribute | `crudcraft-api` | `crudcraft-codegen`, tests, Feature Guide. |
| Generated controller route | `crudcraft-codegen` | golden tests, sample app, docs, compatibility review. |
| Runtime read/write behavior | owning `crudcraft-runtime-*` module | sample app if generated endpoints expose it. |
| Starter dependency | matching `crudcraft-starter-*` | Runtime Modules docs, compatibility review. |
| Documentation navigation | `docs/`, `docs-deploy/scripts/prepare-content.mjs` | link checks and docs build. |
| CI check behavior | `.github/workflows/`, `scripts/` | Maintainer Handbook policy. |

## Related Documentation

- [Module Overview](module-overview.md)
- [Module Boundaries](../architecture/module-boundaries.md)
- [Documentation Inventory](../documentation-inventory.md)
