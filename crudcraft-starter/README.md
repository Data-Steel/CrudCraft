# CrudCraft Spring Boot Starter

The starter module bundles CrudCraft's codegen, runtime, security, projection, and tooling modules and auto-configures them for Spring Boot applications.

## Features
- Brings in all core CrudCraft dependencies.
- Registers annotation processors for code generation.
- Auto-configures MapStruct, OpenAPI, and other supporting libraries.

## Usage
Add the starter dependency to your project:
```xml
<dependency>
  <groupId>nl.datasteel.crudcraft</groupId>
  <artifactId>crudcraft-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```
Enable annotation processing, annotate your entities, and run `mvn compile` to generate fully wired CRUD endpoints.

See the [Getting Started guide](../guides/getting-started.md) for a detailed walkthrough.

## More Information
- [Guides](../guides)
- [crudcraft.dev](https://crudcraft.dev)
