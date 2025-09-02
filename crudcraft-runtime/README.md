# CrudCraft Runtime

The runtime module provides the base classes and utilities that generated code depends on at execution time.

## Features
- Abstract controllers and services that generated endpoints extend.
- QueryDSL-powered search and filtering helpers.
- CSV and XLSX export utilities.
- Integration points for row and field level security.
- Built on Spring Boot, Spring Data JPA, and Bean Validation.

## Usage
Include this module directly or transitively via the Spring Boot starter. Generated code will automatically extend and use the types provided here.

Guides:
- [Search & Filtering](../guides/search-and-filtering.md)
- [Exporting](../guides/exporting.md)
- [Endpoints & Bulk Operations](../guides/endpoints-and-bulk.md)

## More Information
- [Architecture overview](../concepts/architecture.md)
- [crudcraft.dev](https://crudcraft.dev)
