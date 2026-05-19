---
title: "Documentation Review Policy"
description: "Decide whether CrudCraft documentation is accurate, complete, canonical, linked, and sufficient for merge or release."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/doc-requirement"
  - "/documentation-inventory"
  - "/contributor-handbook/documentation-standards"
---

# Documentation Review Policy

Documentation review is a release-quality gate. CrudCraft has a broad generated feature surface; docs must show users what exists, how to use it, when to use it, what it does, and where the boundaries are.

## Block Merge When

- public behavior changed and docs did not;
- docs mention deleted sections or old navigation;
- exact feature facts are duplicated instead of canonicalized;
- examples are generic and do not use CrudCraft names/classes/annotations;
- generated behavior is described without mentioning generated artifacts or runtime modules;
- security docs overpromise what CrudCraft owns;
- `docs/features.md` coverage is stale for a public feature.

## Required Docs By Impact

| Impact | Required docs |
|---|---|
| New user feature | Feature Guide with real example, explanation, expected behavior, boundaries. |
| New annotation/property/module/generated behavior | Relevant Feature Guide page and `features.md` coverage. |
| Codegen architecture change | Architecture page only if pipeline, lifecycle, contract, or boundary changes. |
| Runtime architecture change | Runtime Architecture or Module Boundaries when contracts/dependencies change. |
| Security behavior | Security Feature Guide plus Security Model when layering changes. |
| Contributor workflow | Contributor Handbook. |
| Merge/release/security/version policy | Maintainer Handbook. |
| Docs navigation/deletion | Documentation Inventory and docs-deploy navigation. |

## Review Checks

Maintainers should expect:

```powershell
.\scripts\update-doc-index.ps1
.\scripts\check-doc-drift.ps1 -Staged
```

For docs-deploy changes:

```powershell
cd docs-deploy
npm run build -- --source ..
```

## Related Documentation

- [Documentation Requirements](../doc-requirement.md)
- [Documentation Inventory](../documentation-inventory.md)
- [Documentation Standards](../contributor-handbook/documentation-standards.md)
