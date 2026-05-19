---
title: "Generated Layers"
description: "Understand CrudCraft generated DTOs, mappers, repositories, services, controllers, endpoint templates, search, projection, security, export, LOB, and relationship artifacts."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
  - "crudcraft-runtime-core"
related:
  - "/feature-guides/code-generation/annotations"
  - "/feature-guides/code-generation/editable-stubs"
  - "/feature-guides/security/authorization"
  - "/feature-guides/export"
  - "/features"
---

# Generated Layers

CrudCraft generates a Spring Boot CRUD stack around each concrete `@CrudCrafted` JPA entity. The generated source is deterministic and feature-aware: fields, relationships, endpoint templates, security metadata, search, projection, export, and LOB handling all affect the generated files.

Use this page after changing annotations or inspecting generated output.

## Generated artifacts

| Artifact | Generated when | What it does |
|---|---|---|
| `dto/request/*RequestDto` | Fields use `@Request` | Carries create, update, patch, validate, and bulk request bodies. |
| `dto/response/*ResponseDto` | Fields use `@Dto` | Full read model returned by normal read endpoints. |
| `dto/ref/*Ref` | A field uses `@Dto(ref = true)` | Lightweight reference model for relationship pickers and `/ref`. |
| Named response DTOs, for example `PostListResponseDto` | Fields use `@Dto({"List"})` | Focused read variants with generated `/list` and `/list/{id}` style endpoints. |
| Mapper | Entity is generated | Converts request DTOs to entities and entities to response/ref/named DTOs, including relationship IDs. |
| Repository | Entity is concrete | Extends Spring Data JPA repository contracts. |
| Service | Entity is concrete | Extends `AbstractCrudService`, adds relationship hooks, row-security extensions, and editable override points. |
| Controller | Entity is concrete | Exposes generated HTTP methods for the effective endpoint set. |
| Search request/specification | At least one field is `@Searchable` | Converts query parameters into validated JPA specifications. |
| Projection metadata registry | DTO/projection metadata exists | Lets projection runtime map DTO fields to entity paths. |
| Relationship metadata | Entity has relationships | Describes relationship fields for generated mapping and helper logic. |
| Field-security metadata | A field uses `@FieldSecurity` | Allows runtime read/write filtering without repeated annotation scanning. |
| Insomnia collection | Processor output includes API collection generation | Gives a generated API client collection for endpoint inspection. |

Abstract `@CrudCrafted` entities participate in descriptor inheritance but do not get concrete controllers/services. This supports JPA inheritance models where subclasses generate APIs.

Named response DTO variants such as `@Dto("List")` use the same mutable JavaBean contract as the
default response DTO. CrudCraft copies collection and map values when generating DTO conversion
logic, but the DTO instance itself is not immutable. Treat generated DTOs as request/response
objects, not as shared domain state.

Inheritance support is intentionally conservative. Single-table inheritance is covered by tests.
Joined and table-per-class strategies can work when the JPA provider exposes compatible metamodel
metadata, but they are not a guaranteed CrudCraft contract yet; add application-level integration
tests before relying on generated endpoints for those strategies.

## Endpoint templates

`@CrudCrafted(template = ...)` starts from one of these endpoint bundles:

| Template | Generated endpoint intent |
|---|---|
| `FULL` | Core CRUD, bulk, find-by-IDs, exists, count, ref list, validate. |
| `READ_ONLY` | List, ref list, get one, find by IDs, exists, count. |
| `IMMUTABLE_WRITE` | Reads plus create and bulk create; no update, patch, or delete. |
| `PATCH_ONLY` | Reads plus single and bulk patch; no create, replace, or delete. |
| `NO_DELETE` | Full core API without delete and bulk delete. |
| `NO_BATCH` | Single-entity endpoints without bulk endpoints. |
| `CREATE_ONLY` | Create, bulk create, bulk upsert. |
| `SEARCH_ONLY` | Search only, if searchable fields exist. |
| `META_ONLY` | Count and exists only. |
| `LIGHT_PUBLIC` | Ref list and get one. |
| `SECURE_INTERNAL` | Single-entity internal API and validation; no bulk/search/export. |
| `VALIDATION_ONLY` | Validate endpoint only. |

Then CrudCraft applies `omitEndpoints`, `includeEndpoints`, and optional custom `CrudEndpointPolicy`. Search is special: if the model has searchable fields, `SEARCH` is added; if it has none, `SEARCH` is removed. Export is generated only when included by template or `includeEndpoints`.

## Endpoint routes

| `CrudEndpoint` | Route shape |
|---|---|
| `GET_ALL` | `GET /{resources}` |
| `GET_ALL_REF` | `GET /{resources}/ref` |
| `GET_ONE` | `GET /{resources}/{id}` |
| `POST` | `POST /{resources}` |
| `PUT` | `PUT /{resources}/{id}` |
| `PATCH` | `PATCH /{resources}/{id}` |
| `DELETE` | `DELETE /{resources}/{id}` |
| `BULK_CREATE` | `POST /{resources}/batch` |
| `BULK_UPDATE` | `PUT /{resources}/batch` |
| `BULK_PATCH` | `PATCH /{resources}/batch` |
| `BULK_UPSERT` | `POST /{resources}/batch/upsert` |
| `BULK_DELETE` | `DELETE /{resources}/batch/delete` |
| `FIND_BY_IDS` | `POST /{resources}/batch/ids` |
| `EXISTS` | `HEAD` and `GET /{resources}/exists/{id}` |
| `COUNT` | `GET /{resources}/count` |
| `SEARCH` | `GET /{resources}/search` |
| `VALIDATE` | `POST /{resources}/validate` |
| `EXPORT` | `GET /{resources}/export` |

Generated controllers clamp page sizes with `crudcraft.api.max-page-size` and record operation logs/metrics under `crudcraft.generated.operation`.

## Relationship and LOB behavior

CrudCraft reads JPA relationship annotations and generates mapping support for relation IDs, nested DTOs, and collection updates. For writable `@Lob` fields, generated create/update/patch methods use multipart form data: one `data` part for the request DTO and one file part per writable LOB field. Collection-typed LOB fields receive `List<MultipartFile>`.

Use multipart LOB generation for binary attachments that belong to the entity. Use export for downloading tabular or JSON row data; do not confuse the two features.

For large files, prefer generated multipart LOB endpoints or a dedicated storage/download flow over
inline JSON or tabular export. Exporting binary content as row data can exhaust memory, produce
unusable CSV/XLSX files, and bypass the file-size controls that normally protect upload/download
paths.

## Example: export-enabled, no-delete API

```java
@Entity
@CrudCrafted(
    template = CrudTemplate.NO_DELETE,
    includeEndpoints = CrudEndpoint.EXPORT,
    omitEndpoints = CrudEndpoint.BULK_UPSERT
)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto(ref = true)
    private UUID id;

    @Dto({"List"})
    @Request
    @Searchable
    private String title;
}
```

This generates normal read/write endpoints, no delete endpoints, no bulk upsert endpoint, a search endpoint because `title` is searchable, an export endpoint because it is included, and a `ReportListResponseDto` variant because of `@Dto({"List"})`.

## Related documentation

- [Annotations](annotations.md)
- [Editable Stubs](editable-stubs.md)
- [Security Authorization](../security/authorization.md)
- [Export](../export/)
- [Feature Coverage](../../features.md)
