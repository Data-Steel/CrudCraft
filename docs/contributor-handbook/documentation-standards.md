---
title: "Documentation Standards"
description: "Update CrudCraft documentation in the canonical section with exact feature behavior, examples, links, frontmatter, and drift checks."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/doc-requirement"
  - "/documentation-inventory"
  - "/contributor-handbook/pull-request-process"
---

# Documentation Standards

CrudCraft docs must let users understand the real feature surface without reading the source. Write exact module names, annotations, generated artifacts, configuration keys, examples, expected results, and boundaries.

## Canonical Placement

| Topic | Put it in |
|---|---|
| First successful API generation | Quick Start. |
| User-facing feature behavior | Feature Guides. |
| Exact annotation/property/module/generated behavior | The deepest relevant Feature Guide page. |
| Internal architecture and contracts | Architecture. |
| Contributor workflow | Contributor Handbook. |
| Maintainer merge/release/security policy | Maintainer Handbook. |
| Feature coverage audit | `docs/features.md`. |

There is no separate exact-facts column. Do not recreate one by scattering exact facts across multiple pages.

## Page Requirements

- English only.
- YAML frontmatter with title, description, section, audience, status, and related links where relevant.
- Exactly one H1.
- Concrete examples for feature pages.
- Expected result or behavior explanation after examples.
- Common failure modes or troubleshooting when users can make mistakes.
- Canonical explanation in one page; other pages link to it.
- Final `Related Documentation` section.

## What Good Looks Like

Weak:

```text
CrudCraft supports security for endpoints.
```

Useful:

```text
`@EndpointRbac(endpoint = CrudEndpoint.EXPORT, expression = "hasRole('ADMIN')")`
generates `@PreAuthorize` only on the export endpoint. Use it when normal readers may call
`GET_ALL`, but export should be admin-only.
```

## Docs Checks

For docs changes:

```powershell
.\scripts\update-doc-index.ps1
```

For public behavior changes:

```powershell
.\scripts\check-doc-drift.ps1 -Staged
```

For docs-deploy script changes:

```powershell
node --check docs-deploy\scripts\prepare-content.mjs
```

## Related Documentation

- [Documentation Requirements](../doc-requirement.md)
- [Documentation Inventory](../documentation-inventory.md)
- [Pull Request Process](pull-request-process.md)
