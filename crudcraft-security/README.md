# CrudCraft Security

The security module supplies annotations and helper classes to protect generated endpoints at multiple levels.

## Features
- Annotation-driven endpoint, field, and row security.
- Hooks for integrating with Spring Security or custom providers.
- Works in tandem with the code generator to enforce rules at compile time.

## Usage
Add this module directly or transitively via the Spring Boot starter. Decorate your entities or controllers with security annotations such as `@RowSecurity` to express access rules.

Guides:
- [Security Overview](../guides/security/overview.md)
- [Security Design Concepts](../concepts/security-design.md)

> **Note:** This module is under active development; APIs may evolve.

## More Information
- [crudcraft.dev](https://crudcraft.dev)
