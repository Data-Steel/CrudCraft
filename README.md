![CI](https://github.com/Data-Steel/CrudCraft/actions/workflows/ci.yml/badge.svg)
![Snapshot](https://github.com/Data-Steel/CrudCraft/actions/workflows/snapshot.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/nl.datasteel.crudcraft/crudcraft-runtime.svg)](https://central.sonatype.com/)

# CrudCraft

CrudCraft is a compile-time code generator for Spring Boot that assembles full CRUD APIs from annotated JPA entities. It minimizes boilerplate while preserving explicit control over data exposure, search, and security.

## Features
- **Productive**: Generate controllers, services, repositories, DTOs, mappers, and search utilities with a single annotation.
- **Consistent**: Code is generated from templates and can be regenerated, ensuring uniform APIs across modules.
- **Flexible**: Every generated type can be customized via editable stubs or replaced entirely.
- **Feature-rich**: Pagination, bulk operations, searching, export, and security hooks are available out of the box.

## Modules
- [crudcraft-codegen](crudcraft-codegen/README.md) – annotation processor that generates CRUD layers from annotated entities.
- [crudcraft-runtime](crudcraft-runtime/README.md) – base controllers, services, repositories, and utilities used by generated code.
- [crudcraft-security](crudcraft-security/README.md) – optional security annotations and helpers for endpoint, field, and row protection.
- [crudcraft-projection](crudcraft-projection/README.md) – experimental projection support for tailored read models.
- [crudcraft-tools](crudcraft-tools/README.md) – build-time helpers such as the editable file copier.
- [crudcraft-starter](crudcraft-starter/README.md) – Spring Boot starter that bundles the modules and auto-configures generation.
- [crudcraft-sample-app](crudcraft-sample-app/README.md) – demo application showcasing generated endpoints.

## Getting Started
1. Add the starter dependency to your project:
   ```xml
   <dependency>
     <groupId>nl.datasteel.crudcraft</groupId>
     <artifactId>crudcraft-spring-boot-starter</artifactId>
     <version>0.1.0</version>
   </dependency>
   ```
2. Enable annotation processing and annotate your JPA entities.
3. Run `mvn compile` and explore the generated sources under `target/generated-sources/crudcraft`.

Follow the [Getting Started guide](guides/getting-started.md) for a full walkthrough.

## Documentation
- [Guides](guides/) – step-by-step instructions for common tasks.
- [Concepts](concepts/) – design notes and deep dives into architecture.
- Additional resources on [crudcraft.dev](https://crudcraft.dev).

## Building
CrudCraft requires Java 21 and Maven 3.8+. Run the full build and tests with:
```bash
./mvnw verify
```

## License
CrudCraft is licensed under the [Apache 2.0 License](LICENSE).

