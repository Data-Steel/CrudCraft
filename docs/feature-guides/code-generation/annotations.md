---
title: "Annotations"
description: "Use CrudCraft annotations to control generated DTOs, requests, search fields, projections, security metadata, and endpoint templates."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
related:
  - "/feature-guides/code-generation/generated-layers"
  - "/feature-guides/code-generation"
  - "/architecture/contract-model"
---

# Annotations

CrudCraft annotations describe which entities and fields participate in generation.

Use this page when adding or changing generation annotations on an entity.

## Who this page is for

This page is for developers modeling generated APIs with annotations.

## When to use this page

Use this page before compiling an entity with CrudCraft generation.

## When not to use this page

Do not use annotations to implement request-time logic. Runtime behavior belongs in services, policies, or adapters.

## Prerequisites

- The entity is a supported JPA entity.
- `crudcraft-api` is on the application classpath.
- `crudcraft-codegen` is configured as an annotation processor.

## Quick example

```java
@Entity
@CrudCrafted
public class Customer {
    @Id
    @GeneratedValue
    private Long id;

    @Dto
    @Request
    @Searchable
    private String email;
}
```

Expected result: CrudCraft generates response, request, and search support for `email`.

## Common annotations

| Annotation | Purpose |
|---|---|
| `@CrudCrafted` | Marks an entity for generation. |
| `@Dto` | Includes a field in generated response DTOs. |
| `@Request` | Includes a field in generated request DTOs. |
| `@Searchable` | Allows generated search for a field. |
| `@ProjectionField` | Maps projection fields to source paths. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Forgetting `@CrudCrafted` | The processor ignores the entity. | Add `@CrudCrafted` to the entity class. |
| Marking every field for every DTO | Generated APIs expose too much data. | Add annotations intentionally. |
| Changing annotations without clean compile | Stale generated source can remain. | Run `./mvnw clean compile`. |
| Putting the same endpoint in `omitEndpoints` and `includeEndpoints` | The endpoint contract becomes ambiguous. | Choose one overlay; the processor rejects contradictions. |

## Complex Annotation Combinations

When combining `secure = true`, a custom `CrudSecurityPolicy`, endpoint templates, and endpoint
overlays, resolve the endpoint set first and security second:

1. `template` selects the baseline endpoint set.
2. `includeEndpoints` adds endpoints and `omitEndpoints` removes endpoints.
3. `endpointPolicy` can make a final generated/not-generated decision.
4. Security expressions are generated only for endpoints that remain.

For custom policies, include an explicit branch for export, bulk, and support endpoints. These often
have different risk than single-row reads or writes. Golden fixtures include secure and custom-policy
template combinations; add a new fixture before introducing an endpoint policy that changes this
ordering.

## Deep Embeddables

CrudCraft supports embeddable DTO generation, but deeply nested embeddable chains can create large
DTO graphs and slow serialization. Keep embeddable nesting shallow for public APIs. If an
embeddable-of-embeddable chain grows beyond two levels, prefer a named DTO variant or projection DTO
that exposes only the fields the endpoint actually needs.

## Troubleshooting

If expected fields are missing from generated DTOs, inspect whether the field has the correct CrudCraft annotation.

## Related documentation

- [Generated Layers](generated-layers.md)
- [Code Generation](../code-generation/)
- [Contract Model](../../architecture/contract-model.md)
