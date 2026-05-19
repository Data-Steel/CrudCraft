---
title: "Projection Configuration"
description: "Configure CrudCraft projection runtime support for generated APIs."
section: "Feature Guides"
category: "Projection"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-projection"
related:
  - "/feature-guides/projection/field-selection"
  - "/feature-guides/projection"
  - "/architecture/runtime-architecture"
---

# Projection Configuration

Projection configuration enables generated APIs to use projection runtime support.

Use this page before adding projection-specific read models.

## Who this page is for

This page is for developers enabling projection in a CrudCraft application.

## When to use this page

Use this page when generated APIs need focused read models or selected-field responses.

## When not to use this page

Do not use this page for search filtering. Use Search guides for query behavior.

## Prerequisites

- A generated CrudCraft API compiles.
- Projection use cases are known.
- CrudCraft versions are aligned.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-projection</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: projection runtime beans are available when generated projection code requires them.

## Regenerate

```bash
./mvnw clean compile
```

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Adding projection runtime without generated projection metadata | Runtime support exists, but no projection is available. | Add projection annotations or generated projection configuration. |
| Mixing projection and security without tests | Field visibility can affect projected output. | Test protected projection fields. |
| Using different CrudCraft versions | Generated projection metadata can mismatch runtime. | Align all CrudCraft artifacts. |

## Troubleshooting

If projection beans are missing, confirm the projection starter is on the classpath and auto-configuration conditions are met.

## Related documentation

- [Field Selection](field-selection.md)
- [Projection](../projection/)
- [Runtime Architecture](../../architecture/runtime-architecture.md)
