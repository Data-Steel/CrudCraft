# CrudCraft Sample App

A demonstration Spring Boot application that showcases CrudCraft's generated CRUD endpoints.

## Features
- Annotated entities with generated controllers, services, and repositories.
- JWT-based security configuration.
- H2 in-memory database for quick demos.
- Uses `EditableFileTool` to copy editable stubs into the source tree.

## Running the App
From this module's directory, start the application with:
```bash
mvn spring-boot:run
```
The app starts on <http://localhost:8080> and exposes generated endpoints for the sample domain model.

## Learn More
- [Getting Started guide](../guides/getting-started.md)
- [crudcraft.dev](https://crudcraft.dev)
