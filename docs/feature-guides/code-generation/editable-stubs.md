---
title: "Editable Stubs"
description: "Customize CrudCraft generated APIs safely by using files documented as editable stubs."
section: "Feature Guides"
category: "Code Generation"
audience:
  - "Application developers"
  - "Advanced users"
status: "stable"
crudcraft_modules:
  - "crudcraft-codegen"
related:
  - "/feature-guides/code-generation/generated-layers"
  - "/architecture/generated-code-lifecycle"
  - "/architecture/design-decisions/adr-0003-generated-code-ownership"
---

# Editable Stubs

Editable stubs are generated once and then treated as application-owned customization files.

Use this page when you need to add custom behavior around generated APIs.

## Who this page is for

This page is for developers customizing generated services, controllers, or related extension points.

## When to use this page

Use this page before editing any generated file.

## When not to use this page

Do not edit strict generated files. They may be overwritten during compilation.

## Prerequisites

- You can identify whether a generated file is strict or editable.
- The generated file header or documentation says the file is editable.
- You have run a clean compile.

## Quick example

```text
BookService.java
```

Expected result: if this file is generated as an editable stub in your template, CrudCraft creates it once and preserves later edits.

## Safe customization pattern

```java
@Service
public class BookService extends BookServiceBase {
    public void publish(Long id) {
        // Application-owned behavior.
    }
}
```

Use the actual generated base class and stub names from your project.

## Regeneration and merge strategy

Editable stubs are created once. Later generator runs leave the file in place
and update strict generated base classes around it. Keep application code in the
stub and keep generated base classes out of manual edits.

When a template change introduces a new editable stub shape, compare the new
stub produced in a clean branch with your existing file, then merge only the
new extension points or constructor changes you need. If a merge conflict
appears, prefer the application-owned body from your current stub and the
latest generated signatures from the clean regeneration.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Editing DTOs directly | DTOs are usually strict generated files. | Change annotations or use supported customization points. |
| Assuming every stub is editable | Ownership depends on generated template. | Read the file header and lifecycle docs. |
| Deleting a stub accidentally | Regeneration may create a fresh default file. | Restore from version control or reapply custom logic. |

## Troubleshooting

If a custom change disappears, the file was likely strict generated output rather than an editable stub.

## Related documentation

- [Generated Layers](generated-layers.md)
- [Generated Code Lifecycle](../../architecture/generated-code-lifecycle.md)
- [ADR 0003: Generated Code Ownership](../../architecture/design-decisions/adr-0003-generated-code-ownership.md)
