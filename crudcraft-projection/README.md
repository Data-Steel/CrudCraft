# CrudCraft Projection

This module offers experimental support for projection-based endpoints that expose tailored views of your entities.

## Features
- Generates projection DTOs and controllers for read-optimized views.
- Builds on Spring Data and QueryDSL.
- Useful for returning partial representations or aggregated data.

## Status
Projection support is experimental and APIs may change between releases.

## Usage
Include the module (or the Spring Boot starter) and define projection interfaces or annotations. During compilation, CrudCraft will generate endpoints that serve the projections.

## More Information
- [Architecture overview](../concepts/architecture.md)
- [crudcraft.dev](https://crudcraft.dev)
