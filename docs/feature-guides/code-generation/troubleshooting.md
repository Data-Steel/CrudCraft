---
title: "Code Generation Troubleshooting"
description: "Diagnose common CrudCraft annotation processing and generated source failures."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Application developers"
  - "Contributors"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
related:
  - "/feature-guides/code-generation/annotations"
  - "/feature-guides/code-generation/generated-layers"
  - "/feature-guides/code-generation"
---

# Code Generation Troubleshooting

Code generation troubleshooting starts by checking annotations, annotation processor configuration, generated source, and compiler errors.

Use this page when expected generated files are missing or generated code does not compile.

## Who this page is for

This page is for developers diagnosing CrudCraft generation failures.

## When to use this page

Use this page when compilation fails or generated artifacts are missing.

## When not to use this page

Do not use this page for runtime endpoint failures after the application starts.

## Prerequisites

- You can run Maven compile.
- You can inspect `target/generated-sources/annotations`.
- You can read compiler diagnostics.

## Quick example

```bash
./mvnw clean compile
```

Expected result: annotated entities produce generated Java source and compilation succeeds.

## Troubleshooting table

| Symptom | Likely cause | Fix |
|---|---|---|
| No generated files | Processor not configured or no `@CrudCrafted`. | Check compiler plugin and annotations. |
| Missing field in DTO | Field lacks `@Dto` or `@Request`. | Add the correct field annotation. |
| Generated import fails | Runtime dependency missing. | Add the required starter. |
| Duplicate class error | User class conflicts with generated name. | Rename one class or adjust generation settings. |
| Output changes every build | Non-deterministic generator ordering. | Sort generator inputs and add tests. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Running without `clean` after model changes | Stale generated files can confuse diagnosis. | Start with `./mvnw clean compile`. |
| Looking only at source annotations | Generated output shows what happened. | Inspect generated Java files. |
| Mixing CrudCraft versions | Processor and runtime contracts can mismatch. | Align all versions. |

## Related documentation

- [Annotations](annotations.md)
- [Generated Layers](generated-layers.md)
- [Code Generation](../code-generation/)
