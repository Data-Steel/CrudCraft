---
title: "Codegen Architecture"
description: "Understand CrudCraft annotation processors, descriptor extraction, generator ordering, endpoint resolution, and generated artifacts."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
  - "crudcraft-api"
related:
  - "/architecture/system-overview"
  - "/architecture/generated-code-lifecycle"
  - "/architecture/contract-model"
---

# Codegen Architecture

`crudcraft-codegen` is a Java 21 annotation processor module. It converts source-level annotations from `crudcraft-api` into generated Java files and metadata. It must be deterministic: the same source model, annotations, processor options, and CrudCraft version should produce the same output.

## Processors

`module-info.java` exposes two processors:

| Processor | Purpose |
|---|---|
| `CrudCraftProcessor` | Reads `@CrudCrafted` entities and `@Embeddable` value objects, then dispatches generators through `WriterRegistry`. |
| `ProjectionMetadataProcessor` | Processes projection metadata and contributes generated projection registry support used by the projection runtime. |

`CrudCraftProcessor` supports these processor options:

| Option | Used for |
|---|---|
| `crudcraft.insomnia.outputDir` | Output location for generated Insomnia API collections. |
| `crudcraft.dto.generateWithers` | DTO generation mode for record-friendly update helpers. |

## Descriptor Pipeline

The main pipeline is:

```text
RoundEnvironment
  -> processable TypeElement
  -> AnnotationModelReader
  -> ModelPartExtractorRegistry
  -> FieldPartExtractorRegistry
  -> ModelDescriptor
  -> WriterRegistry
  -> Generator.generate(...)
  -> WriteContext.write(...)
  -> Filer-created Java source
```

`AnnotationModelReader` does not directly generate files. It builds a `ModelDescriptor` with four model parts:

| Part | Extractor responsibility |
|---|---|
| `ModelIdentity` | Entity name, package, base package, and field descriptors. |
| `ModelFlags` | Whether the class is a CrudCraft entity, embeddable, editable, or abstract. |
| `EndpointOptions` | `CrudTemplate`, `includeEndpoints`, `omitEndpoints`, and custom `CrudEndpointPolicy`. |
| `ModelSecurity` | `secure`, policy class, generated endpoint expressions, row handlers, and built-in row scopes. |

Each field descriptor is assembled from field parts:

| Field part | Data captured |
|---|---|
| `Identity` | Name, type, id status, LOB status. |
| `DtoOptions` | Request/response/ref/specialized DTO inclusion. |
| `EnumOptions` | Enum string handling. |
| `Relationship` | Association shape and generated relationship metadata. |
| `Validation` | Jakarta validation annotations copied into generated request DTOs. |
| `SearchOptions` | Searchable paths, operators, and sortability. |
| `Security` | Field-level read/write roles and write policy metadata. |

## Generator Dispatch

`WriterRegistry` starts with built-in generators and then adds `ServiceLoader<Generator>` contributions by class name. Generators are split into:

- basic generators: run for all descriptors, including embeddables;
- CRUD generators: run only when `ModelDescriptor.isCrudCraftEntity()` is true.

Within each group, generators run by `Generator.order()`. Built-in generators are:

| Generator | Writes |
|---|---|
| `DtoGenerator` | Request, response, ref, specialized DTOs, enum/value handling, validation metadata. |
| `SearchGenerator` | Search request and specification support when fields are searchable. |
| `RelationshipMetaGenerator` | Entity relationship metadata for runtime utilities and generated layers. |
| `RepositoryGenerator` | Spring Data repository stub. |
| `MapperGenerator` | Mapper contract/implementation integration for request, response, ref, and specialized DTOs. |
| `ServiceGenerator` | Service class extending `AbstractCrudService`, relationship hooks, row-security runtime extensions. |
| `ControllerGenerator` | REST controller methods, OpenAPI annotations, endpoint security, metrics/logging, search/export/bulk/validation endpoints. |
| `InsomniaGenerator` | API collection output based on generated endpoint metadata. |

Service-loaded generators are extension points only for codegen module contributors. They must obey the same deterministic output and public-contract rules as built-ins.

## Endpoint Resolution

`ControllerGenerator` resolves endpoint availability in this order:

1. Start with the selected `CrudTemplate.resolveEndpoints()` or instantiate the custom `CrudEndpointPolicy`.
2. Remove `omitEndpoints`.
3. Add `includeEndpoints`.
4. Add `CrudEndpoint.SEARCH` when the model has searchable fields; remove it when it does not.
5. Build default `EndpointSpec` values from `ControllerEndpoints.defaults(modelDescriptor)`.
6. Attach `@PreAuthorize` when the model is secure.

Security expression resolution is also layered:

1. `@EndpointRbac` / annotation-derived endpoint expressions become a `ResolvedCrudSecurityPolicy`.
2. If `secure = true` has no explicit policy, generated endpoints use `isAuthenticated()`.
3. If a `CrudSecurityPolicy` class is configured, the processor instantiates it and asks for each endpoint expression.

This means endpoint shape and endpoint authorization are compile-time generated decisions. Row and field enforcement still happen at runtime.

## Generated Controller Shape

Generated controllers are not thin wrappers around `AbstractCrudController`. The current controller generator writes methods directly from endpoint providers under `writer/controller/endpoints`. That is why generated controllers also contain shared controller-local behavior:

- base path from pluralized model name;
- endpoint methods for all supported `CrudEndpoint` values;
- page-size clamp from `crudcraft.api.max-page-size`;
- optional export limits from `crudcraft.export.max-csv-rows`, `crudcraft.export.max-json-rows`, `crudcraft.export.max-xlsx-rows`;
- optional `ExportService` construction when `CrudEndpoint.EXPORT` is enabled;
- optional `MeterRegistry` lookup;
- structured log line and timer named `crudcraft.generated.operation`;
- multipart request handling for request DTO fields backed by `@Lob`;
- field security filtering around generated request/response paths when metadata exists.

## Failure Boundaries

| Failure | Where it should be caught |
|---|---|
| Unsupported annotation combination | Processor diagnostic tied to the offending type or field. |
| Uninstantiable endpoint or security policy | Processor diagnostic and compilation failure. |
| Missing runtime dependency for generated imports | Application compilation or Spring Boot startup, depending on the missing contract. |
| Bad generated output order | Golden-file or compile-testing failure. |
| Runtime query/security/export behavior bug | Runtime module or sample app test, not a writer unit test alone. |

## Related Documentation

- [Generated Code Lifecycle](generated-code-lifecycle.md)
- [Contract Model](contract-model.md)
- [Testing Architecture](testing-architecture.md)
