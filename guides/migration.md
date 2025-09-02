---
title: Regeneration & Migrations
summary: Manage code regeneration when entities or annotations evolve.
sidebar: guides
---

# Regeneration & Migrations

Evolving your domain model often requires regenerating code or migrating existing data. This guide explains how to handle changes safely.

## Changing Annotations

- Adding `@Dto`, `@Request`, or `@Searchable` triggers regenerated DTOs and search requests.
- Removing an annotation removes the corresponding property from generated classes. Update custom code and database schemas accordingly.

## Regenerating Editable Stubs

If method signatures change or you need fresh templates, delete the editable stub and recompile. CrudCraft recreates the file with current definitions while preserving your version control history.

## Removing Fields

When deleting entity fields, run database migrations before compilation to avoid runtime errors. CrudCraft will remove related DTO properties and mapper rules automatically.

## Managing Breakage

- Re-run tests after regeneration to catch compilation errors.
- For large refactors, regenerate in a separate commit to keep diffs clear.

## Version Pinning

Pin the CrudCraft starter version in your build to avoid unexpected template changes during upgrades.

```xml
<dependency>
  <groupId>nl.datasteel.crudcraft</groupId>
  <artifactId>crudcraft-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Next Steps

- Learn customization options in [Editable Stubs & Customization](/guides/editable-stubs.md).
- Review [Testing Generated APIs](/guides/testing.md) to validate migrations.
- Plan security updates via [Security Overview](/guides/security/overview.md).

