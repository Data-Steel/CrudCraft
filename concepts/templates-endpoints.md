---
title: Templates & Endpoints Model
summary: How CrudCraft templates decide which endpoints to generate and how to customize them.
sidebar: concepts
---

# Templates & Endpoints Model

CrudCraft models controllers as a set of `CrudEndpoint` definitions. Templates determine which endpoints to emit based on configuration and annotations.

## CrudTemplate Presets

CrudCraft ships with presets such as `DEFAULT`, `READ_ONLY`, and `WRITE_ONLY`. These presets decide which endpoint groups (read, write, bulk, export) are generated.

## CrudEndpoint Structure

Each endpoint definition specifies HTTP method, path, security policy, and whether it supports bulk operations. The annotation processor renders Java code from these definitions.

```java
record CrudEndpoint(String method, String path, CrudEndpointPolicy policy) {}
```

## Including or Omitting Endpoints

Use `@CrudCrafted(include = {CrudEndpointGroup.BULK})` or `omit` to fine-tune generation.

```java
@CrudCrafted(omit = {CrudEndpointGroup.DELETE})
public class Book { }
```

The above annotation generates all endpoints except DELETE.

## Custom Policies

`CrudEndpointPolicy` objects attach security rules at the endpoint level. When combined with a preset, they override default policies for specific methods.

## Generation Decision Flow

1. Determine template preset.
2. Apply `include`/`omit` adjustments.
3. Evaluate `CrudEndpointPolicy` overrides.
4. Render controller methods via JavaPoet.

## Next Steps

- Examine mapping strategy in [DTO Design & Mapping](/concepts/dto-mapping.md).
- Plan security across layers using [Security Design](/concepts/security-design.md).
- Try custom endpoints in [Editable Stubs & Customization](/guides/editable-stubs.md).

