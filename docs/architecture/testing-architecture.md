---
title: "Testing Architecture"
description: "Understand CrudCraft's test layers for API annotations, codegen, generated source, runtime modules, starters, sample integration, docs, and release gates."
section: "Architecture"
audience:
  - "Contributors"
  - "Maintainers"
  - "Advanced users"
status: "stable"
related:
  - "/architecture/codegen-architecture"
  - "/architecture/runtime-architecture"
  - "/contributor-handbook/writing-tests"
---

# Testing Architecture

CrudCraft has two hard-to-test surfaces: generated source shape and request-time behavior that depends on generated source. A good test suite covers both without using one as a substitute for the other.

## Test Layers

| Layer | Proves | Typical location |
|---|---|---|
| API annotation tests | Public annotation defaults, retention, target, enum values, policy interfaces. | `crudcraft-api/src/test` |
| Descriptor/extractor tests | Source annotations become the right `ModelDescriptor` and `FieldDescriptor` data. | `crudcraft-codegen/src/test` |
| Writer unit tests | A generator writes the expected JavaPoet output for a descriptor. | `crudcraft-codegen/src/test` |
| Compile-testing processor tests | `CrudCraftProcessor` compiles real source and emits expected generated classes or diagnostics. | `crudcraft-codegen/src/test` |
| Golden generated output tests | Generated source shape stays deterministic and reviewable. | `crudcraft-codegen/src/test/resources/golden` |
| Runtime unit tests | Runtime services, adapters, validation, exceptions, export, security, projection behavior. | `crudcraft-runtime-*/src/test` |
| Starter tests | Auto-configuration and dependency composition. | `crudcraft-starter-*/src/test` |
| Sample app integration tests | Generated controllers + services + runtime modules + Spring Boot + persistence work together. | `crudcraft-sample-app/src/test` |
| TCK/matrix tests | Feature matrix coverage and cross-feature behavior. | Sample app and generated matrix resources. |
| Docs checks | Links, navigation, syntax, and docs inventory stay valid. | `docs`, `docs-deploy` |
| Release gates | Full reactor, static analysis, coverage, package metadata. | Root Maven build and maintainer workflows. |

## Which Test To Add

| Change | Required test signal |
|---|---|
| New annotation attribute | API test plus descriptor extraction test. |
| Changed endpoint resolution | Controller generator test, golden fixture, and sample endpoint coverage if runtime behavior changes. |
| DTO shape change | Golden output test and compile-testing test. |
| Search operator/path behavior | Search generator test plus runtime `SearchOperations` test. |
| Projection metadata change | Codegen metadata test plus projection runtime test. |
| Export endpoint or streaming behavior | Controller generator/golden test plus runtime export test and sample HTTP test. |
| Endpoint security expression behavior | Security extraction/generator test plus sample Spring Security test. |
| Row or field security runtime behavior | Runtime security test plus generated service/controller integration test. |
| Starter dependency change | Starter auto-configuration or dependency composition test plus docs update. |
| Public error mapping change | Runtime-core exception handler test plus contract docs update. |

## Golden Tests

Golden tests are for generated source shape, not runtime behavior. They should fail when:

- generated class names/packages change;
- DTO components or validation annotations change;
- endpoint methods, mappings, parameters, or `@PreAuthorize` annotations change;
- generated metadata changes;
- output order becomes unstable.

They should not be the only test for behavior. If generated source compiles but runtime execution is wrong, add a runtime or sample integration test.

## Runtime Tests

Runtime tests should target public runtime contracts:

- `AbstractCrudService` behavior through a concrete service or focused fixture;
- `SearchOperations` validation and delegation;
- `ExportService` streaming, format, limit, and filtering behavior;
- projection adapter/metadata execution;
- `FieldSecurityRuntimeExtension` and `RowSecurityRuntimeExtension` lifecycle hooks;
- `CrudCraftExceptionHandler` status mapping.

Avoid asserting private implementation details when observable behavior is available.

## Sample App Tests

Use sample app tests when the behavior crosses generated source and runtime modules. Good sample tests prove:

- a generated endpoint exists and has the expected method/path;
- generated security annotations work with actual Spring Security;
- row and field security behave through HTTP, search, update, and export;
- generated projections execute against JPA;
- bulk endpoints preserve expected response/error behavior;
- starter composition loads the needed runtime beans.

## Documentation Tests

Docs are part of CrudCraft's contract because users rely on them for generation and runtime setup. At minimum, docs changes should keep:

- relative Markdown links valid;
- docs deploy script syntactically valid;
- navigation free of deleted sections;
- `docs/features.md` aligned with canonical Feature Guide coverage.

## Related Documentation

- [Codegen Architecture](codegen-architecture.md)
- [Runtime Architecture](runtime-architecture.md)
- [Writing Tests](../contributor-handbook/writing-tests.md)
