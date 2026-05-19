---
title: "Testing Code Generation"
description: "Test CrudCraft annotation processing, generated source shape, generated compilation, and generated API contracts."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
related:
  - "/feature-guides/code-generation/deterministic-output"
  - "/architecture/testing-architecture"
  - "/feature-guides/code-generation"
---

# Testing Code Generation

Code generation tests prove that annotations produce the expected source and that generated source compiles.

Use this page when changing generator logic or generated public API shape.

## Who this page is for

This page is for contributors and maintainers working on codegen behavior.

## When to use this page

Use this page whenever generated source changes.

## When not to use this page

Do not use only generated source tests for runtime behavior. Add runtime or integration tests too.

## Prerequisites

- Test fixtures can compile annotated source.
- Expected generated output is known.
- Public contract changes are documented.

## Quick example

```java
Compilation compilation = javac()
        .withProcessors(new CrudCraftProcessor())
        .compile(JavaFileObjects.forSourceString("Book", source));

assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);
```

Expected result: annotation processing succeeds and generated files are available for inspection.

## Required test types

| Test type | Purpose |
|---|---|
| Descriptor tests | Prove model extraction. |
| Writer tests | Prove generated source fragments. |
| Compile tests | Prove generated source compiles. |
| Golden-file tests | Prove stable output shape. |
| Integration tests | Prove generated code works with runtime. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Testing only helper methods | Generated source can still be wrong. | Compile generated source. |
| Updating golden files blindly | Regressions can be approved accidentally. | Review every generated diff. |
| Skipping docs for public changes | Users cannot migrate safely. | Update guides, reference, or migration docs. |

## Troubleshooting

If compile tests fail after a writer change, inspect the generated source before changing runtime code.

## Related documentation

- [Deterministic Output](deterministic-output.md)
- [Testing Architecture](../../architecture/testing-architecture.md)
- [Code Generation](../code-generation/)
