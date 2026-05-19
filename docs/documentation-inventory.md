---
title: "Documentation Inventory"
description: "Records the canonical CrudCraft documentation structure after consolidating all feature facts into Feature Guides."
section: "Documentation"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/doc-requirement"
  - "/features"
  - "/feature-guides"
---

# Documentation Inventory

This inventory records the canonical written documentation structure. Feature Guides are the only feature column; exact annotation, module, configuration, and generated-behavior facts live there.

Use this page when adding, removing, or auditing docs.

## Canonical Tree

```text
docs/
+-- README.md
+-- quick-start/
+-- feature-guides/
|   +-- code-generation/
|   +-- runtime-modules/
|   +-- search/
|   +-- projection/
|   +-- export/
|   +-- security/
|   +-- validation/
|   +-- extensions/
+-- architecture/
+-- contributor-handbook/
+-- maintainer-handbook/
+-- doc-requirement.md
+-- documentation-inventory.md
+-- features.md
```

## Removed Sections

| Removed section | Replacement |
|---|---|
| Removed exact-facts pages | The relevant `docs/feature-guides/**` page. |
| Removed upgrade section | Release notes plus compatibility pages in maintainer/contributor docs when needed. |
| Project-reference sidebar grouping | Root docs are grouped as Documentation. |

## Feature Guide Inventory

| Feature area | Required coverage |
|---|---|
| Code Generation | Annotations, generated layers, endpoint templates, editable stubs, deterministic output, tests, troubleshooting. |
| Runtime Modules | Core, search, projection, export, security, extensions, configuration, compatibility. |
| Search | Configuration, operators, filtering, sorting, pagination, nested fields, tests, troubleshooting. |
| Projection | Configuration, field selection, paths, security interaction, tests, troubleshooting. |
| Export | Generated export endpoint, formats, limits, field selection, DTO/entity modes, search, security. |
| Security | Authentication contract, endpoint authorization, RBAC, field security, row scopes, configuration, tests, troubleshooting. |
| Validation | Jakarta Validation, request validation, generated errors, tests, troubleshooting. |
| Extensions | Auditable, soft delete, relationship helpers. |

## Coverage Rule

Every public feature in [CrudCraft Features](features.md) must point to one canonical Feature Guide page. If a page repeats another page's topic, keep the deepest/canonical version and replace the duplicate with a link or delete it.

## Related Documentation

- [Documentation Requirements](doc-requirement.md)
- [CrudCraft Features](features.md)
- [Feature Guides](feature-guides/)
