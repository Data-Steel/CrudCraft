---
title: "Migration Guides"
description: "Track CrudCraft upgrade, compatibility, deprecation, and breaking-change instructions."
section: "Migration Guides"
audience:
  - "Advanced users"
  - "Maintainers"
status: "draft"
related:
  - "/feature-reference"
  - "/maintainer-handbook"
  - "/documentation-inventory"
---

# Migration Guides

Migration Guides explain how to upgrade CrudCraft safely between releases.

Use this page as the route map for migration and compatibility documentation.

## Who this page is for

This page is for application developers and maintainers planning or reviewing an upgrade.

## When to use this page

Use this page when a release changes public APIs, generated APIs, runtime behavior, configuration keys, compatibility guarantees, or deprecation status.

## When not to use this page

Do not use this page for first-time setup. Use Quick Start for first-time usage.

## Required pages

| Page type | Purpose |
|---|---|
| Version migration guide | Explain upgrade steps from one release to another. |
| Breaking change note | Explain required user action for incompatible changes. |
| Deprecation guide | Explain replacements and removal timelines. |
| Compatibility guide | Explain supported module and runtime combinations. |

## Required example

Every breaking change must show old behavior, new behavior, required action, and expected result after migration.

## Deferred Breaking Changes

No generated DTO mutability break is deferred in the 2.x contract: generated DTOs are records, while
generated search requests remain mutable query command objects. Future breaking changes, such as a
nested `@CrudCrafted` configuration model or service base-class split, must go through release
notes, contract tests, and a migration guide before tagging.

## Related documentation

- [Feature Reference](../feature-reference/)
- [Maintainer Handbook](../maintainer-handbook/)
- [Documentation Inventory](../documentation-inventory.md)
