# CrudCraft Codegen

The code generation module is a Java annotation processor that scans JPA entities marked with CrudCraft annotations and writes the controllers, services, repositories, DTOs, and search helpers needed to expose full CRUD APIs.

## Features
- Generates CRUD layers using [JavaPoet](https://github.com/square/javapoet).
- Integrates with MapStruct for DTO mapping.
- Understands security annotations to enforce endpoint, field, and row rules at compile time.
- Emits OpenAPI annotations for automatic documentation.

## Usage
1. Include the module directly or through the Spring Boot starter.
2. Enable annotation processing in your build or IDE.
3. Annotate your entities with `@CrudCrafted`, `@Dto`, and other CrudCraft annotations.
4. Compile your project to generate sources under `target/generated-sources/crudcraft`.

See the [Entity Annotations guide](../guides/entity-annotations.md) for supported annotations and options.

## More Information
- [Architecture overview](../concepts/architecture.md)
- [crudcraft.dev](https://crudcraft.dev)
