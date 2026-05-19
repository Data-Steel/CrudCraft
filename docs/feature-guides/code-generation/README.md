---
title: "Code Generation"
description: "Use CrudCraft code generation to turn annotated JPA entities into generated CRUD API layers."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
related:
  - "/feature-guides/code-generation"
  - "/architecture/codegen-architecture"
  - "/feature-guides/code-generation/annotations"
---

# Code Generation

CrudCraft code generation reads annotated JPA entities and writes application-specific CRUD API source during compilation.

Use this page to choose the right code generation guide.

## Who this page is for

This page is for developers configuring generated artifacts or diagnosing generated output.

## When to use this page

Use this page when annotations, generated files, stubs, or deterministic output matter.

## When not to use this page

Do not use this page for runtime behavior. Use Runtime Modules guides for request-time features.

## Start here

| Need | Read |
|---|---|
| Configure entity annotations | [Annotations](annotations.md) |
| Understand generated files | [Generated Layers](generated-layers.md) |
| Customize safely | [Editable Stubs](editable-stubs.md) |
| Keep output stable | [Deterministic Output](deterministic-output.md) |
| Test generation | [Testing](testing.md) |
| Diagnose generation failures | [Troubleshooting](troubleshooting.md) |

## Quick example

```java
@Entity
@CrudCrafted
public class Book {
}
```

Expected result: CrudCraft processes `Book` during compilation and generates the configured CRUD API layers.

## Related documentation

- [Code Generation](../code-generation/)
- [Codegen Architecture](../../architecture/codegen-architecture.md)
- [Annotations](annotations.md)
