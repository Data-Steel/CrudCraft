---
title: "Generated Code Lifecycle"
description: "Understand CrudCraft generated file ownership, regeneration, editable stubs, deterministic output, and review rules."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/codegen-architecture"
  - "/architecture/contract-model"
  - "/architecture/design-decisions/adr-0003-generated-code-ownership"
---

# Generated Code Lifecycle

Generated CrudCraft source is application source produced during compilation. Users compile against it, tests exercise it, and public generated shapes become compatibility-sensitive.

## Where Files Are Written

During a Maven build, annotation processors normally write Java files under:

```text
target/generated-sources/annotations
```

The compiler then compiles those generated files with the rest of the application. IDEs may show the same generated source as a generated source root.

## Lifecycle Flow

```text
entity or annotation changes
  -> compile starts annotation processing
  -> descriptors are rebuilt from current source
  -> strict generated files are written through Filer
  -> editable stubs are created only if absent
  -> generated and handwritten source compile together
```

Compilation is the source of truth. If a generated file looks stale, run a clean compile before debugging runtime behavior.

## File Ownership

CrudCraft has two ownership modes:

| Mode | Owner after generation | Behavior |
|---|---|---|
| Strict generated files | CrudCraft | May be overwritten whenever the processor runs. |
| Editable stubs | Application | Created when absent and then preserved by the processor. |

Strict generated files are for deterministic contracts such as DTOs, metadata, generated search artifacts, repositories, and other source that must track annotations exactly.

Editable stubs are for application customization where CrudCraft intentionally leaves a subclass or Spring component under application ownership. The current service generator writes a service class extending `AbstractCrudService`; that class has generated hooks like `postSave(...)` and `preDelete(...)` for relationship utilities. Treat the file header and feature docs as the authority for whether a file is safe to edit.

## Generated Artifact Families

| Artifact family | Why it exists | Ownership expectation |
|---|---|---|
| Request DTOs | HTTP write contract, validation, field-security write metadata. | Strict. |
| Response/ref/specialized DTOs | HTTP read contract and projection targets. | Strict. |
| Mapper | Entity/DTO conversion contract used by services. | Strict unless explicitly generated as editable. |
| Repository | Spring Data access point. | Usually generated application component; review header before editing. |
| Service | Entity-specific runtime wiring, row-security extensions, relationship hooks. | Often intended customization surface; review header and docs. |
| Controller | Entity-specific route surface, security expressions, metrics/logging, endpoint providers. | Strict unless a selected template documents editability. |
| Search request/specification | `@Searchable` query contract. | Strict. |
| Projection metadata registry | Runtime projection metadata. | Strict. |
| Relationship metadata | Runtime relationship helper metadata. | Strict. |
| Insomnia collection | Generated API client collection. | Regenerated output. |

## Determinism Rules

Writers must avoid unstable output:

- sort fields, methods, imports, endpoint lists, and metadata entries when input order is not guaranteed;
- use reproducible build timestamp data for generated headers and Insomnia exports instead of wall-clock time;
- keep generator output independent from HashMap iteration, file system order, locale-sensitive formatting, and environment-specific absolute paths.

When output changes, reviewers should be able to answer which annotation, descriptor, template, or runtime contract caused the diff.

## Regeneration Review

Regeneration is safe when:

- changed files match the annotation/model change being made;
- generated public routes, DTO record components, request fields, search fields, and security annotations are intentionally changed;
- golden tests or compile tests capture the new shape;
- docs and compatibility notes cover user-visible changes.

Regeneration is suspicious when:

- only ordering changed;
- generated files include local paths or current timestamps;
- generated source imports `nl.datasteel.crudcraft.codegen.*`;
- optional feature imports appear without an annotation or endpoint configuration that enables that feature;
- manual edits inside strict files are needed to make the application compile.

## Failure Modes

| Failure | Cause | Fix |
|---|---|---|
| Manual change disappears | A strict generated file was edited. | Move custom logic to an editable stub or application-owned class. |
| Generated source is stale | Build cache or generated source root was not refreshed. | Run `./mvnw clean compile` or the Windows equivalent. |
| Duplicate class error | A handwritten class uses a generated name. | Rename the handwritten class or change generation configuration where supported. |
| Runtime `NoSuchMethodError` | Generated code and runtime modules are from different CrudCraft versions. | Align all CrudCraft artifacts to one version. |
| Generated public API changed silently | Writer changed shape without contract review. | Add golden/compile tests and update docs. |

## Related Documentation

- [Codegen Architecture](codegen-architecture.md)
- [Contract Model](contract-model.md)
- [ADR 0003: Generated Code Ownership](design-decisions/adr-0003-generated-code-ownership.md)
