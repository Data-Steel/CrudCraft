---
title: Architecture & Modules
summary: Overview of CrudCraft's modules and build-time generation pipeline.
sidebar: concepts
---

# Architecture & Modules

CrudCraft is divided into several modules that cooperate at compile time to produce runtime-ready code. Understanding the architecture helps when debugging or extending the framework.

## Modules

| Module | Purpose |
|--------|--------|
| **crudcraft-annotations** | Defines annotations such as `@CrudCrafted`, `@Dto`, and `@RowSecurity`. |
| **crudcraft-codegen** | Annotation processor that scans entities and writes Java source files using JavaPoet. |
| **crudcraft-runtime** | Base classes like `AbstractCrudService`, `AbstractCrudController`, and mapper interfaces. |
| **crudcraft-security** | Optional module providing security annotations and handlers. |
| **crudcraft-projection** | Experimental support for projection endpoints. |
| **crudcraft-starter** | Spring Boot starter that aggregates dependencies and auto-configures runtime beans. |

## Build Flow

1. **APT Invocation** — During compilation, the annotation processor analyses entities annotated with CrudCraft annotations.
2. **Template Generation** — JavaPoet templates emit DTOs, repositories, services, controllers, and search requests.
3. **Compilation** — Generated sources are added to the compiler classpath and compiled alongside your code.

## Generated vs Editable Code

CrudCraft deliberately separates generated files into editable and non-editable categories. Editable stubs provide extension points, while regenerated files remain deterministic. This design minimizes merge conflicts and makes regeneration safe.

## Example Build Output

After compilation, generated sources reside under `target/generated-sources/crudcraft`:

```
BookController.java
BookService.java
BookRepository.java
```

## Next Steps

- Dive into endpoint generation in [Templates & Endpoints Model](/concepts/templates-endpoints.md).
- Explore the rationale behind security layers in [Security Design](/concepts/security-design.md).
- Get hands-on by following [Getting Started](/guides/getting-started.md).

