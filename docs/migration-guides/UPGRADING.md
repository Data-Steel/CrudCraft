---
title: "Upgrading Between Versions"
description: "Checklist for moving CrudCraft applications between minor or major versions."
section: "Migration Guides"
audience:
  - "Application developers"
  - "Maintainers"
status: "draft"
related:
  - "/migration-guides"
  - "/generated-code-contract"
  - "/nullability-contract"
---

# Upgrading Between Versions

Use this checklist for every CrudCraft upgrade, even when the release notes describe the change as
compatible.

## Before upgrading

1. Commit or archive generated editable stubs you own.
2. Read release notes for changed annotations, generated contracts, runtime properties, and starter
   module requirements.
3. Run the current version's `mvn verify` so upgrade failures are not mixed with pre-existing test
   failures.

## Upgrade steps

1. Update all CrudCraft artifacts to the same version.
2. Regenerate annotation-processor output with a clean Maven build.
3. Review editable generated stubs for newly documented hooks or removed deprecated calls.
4. Re-run application tests that cover create, update, patch, delete, search, projection, export,
   row security, and field security.
5. Run `mvn verify -Pmutation` before releasing a library or starter upgrade.

## Compatibility rules

Generated strict files may change whenever annotations, templates, or runtime contracts change.
Editable stubs preserve user-owned code, but their generated headers and surrounding contracts may
still need manual review after a major upgrade.

Types annotated with `@InternalOnly` are not migration-stable APIs. Replace usages with the
documented public annotation, runtime interface, editable stub hook, or feature-guide contract before
upgrading.

## From 1.0.x to 1.1.x

No 1.1.x breaking changes are documented yet. Treat generated DTO records, generated search request
nullness, export limits, projection metadata validation, and runtime extension singleton semantics as
the baseline compatibility contract.

## Related documentation

- [Migration Guides](README.md)
- [Generated Code Contract](../generated-code-contract.md)
- [Nullability Contract](../nullability-contract.md)
