---
title: "Runtime Module Compatibility"
description: "Keep CrudCraft runtime modules, generated code, and starters compatible."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
  - "Maintainers"
status: "stable"
related:
  - "/feature-guides/runtime-modules/configuration"
  - "/feature-guides/runtime-modules"
  - "/architecture/module-boundaries"
---

# Runtime Module Compatibility

Runtime module compatibility means generated code, runtime modules, and starters come from the same CrudCraft release line.

Use this page before upgrading or combining CrudCraft modules.

## Who this page is for

This page is for developers and maintainers reviewing dependency compatibility.

## When to use this page

Use this page when adding modules, upgrading versions, or diagnosing runtime contract mismatches.

## When not to use this page

Do not use this page for feature-specific behavior. Use the relevant Feature Guide.

## Prerequisites

- You know all CrudCraft artifacts in the project.
- You can inspect the dependency tree.
- You know which generated features are enabled.

## Quick example

```bash
./mvnw dependency:tree
```

Expected result: all `nl.datasteel.crudcraft` artifacts resolve to the same intended version.

## Compatibility rules

| Rule | Reason |
|---|---|
| Align all CrudCraft versions | Generated code calls runtime contracts from the same release. |
| Add optional starters only when needed | Optional modules remain optional. |
| Review generated imports after enabling a feature | Imports reveal runtime requirements. |
| Read release notes before major upgrades | Generated public API may change. |

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Upgrading only `crudcraft-codegen` | Generated code can target newer runtime contracts. | Upgrade codegen and runtime together. |
| Keeping old starters with new generated code | Runtime beans may be missing or incompatible. | Align starter versions. |
| Ignoring transitive dependencies | Older versions can be pulled indirectly. | Check `dependency:tree`. |

## Troubleshooting

If a generated class fails with `NoSuchMethodError` or `ClassNotFoundException`, verify CrudCraft versions and generated imports first.

## Related documentation

- [Runtime Module Configuration](configuration.md)
- [Runtime Modules](../runtime-modules/)
- [Module Boundaries](../../architecture/module-boundaries.md)
