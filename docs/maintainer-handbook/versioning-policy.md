---
title: "Versioning Policy"
description: "Decide whether a CrudCraft change requires a patch, minor, or major release based on public and generated contract impact."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/compatibility-policy"
  - "/maintainer-handbook/deprecation-policy"
  - "/maintainer-handbook/release-process"
---

# Versioning Policy

CrudCraft version numbers communicate upgrade risk for annotations, generated source, generated HTTP APIs, runtime modules, starters, configuration, and docs.

## Decide In This Order

1. Identify the affected compatibility surface.
2. Decide whether existing users must change anything.
3. Check whether the change was previously deprecated.
4. Choose patch, minor, or major.
5. Confirm release notes explain the impact.

## Version Rules

| Change | Version impact |
|---|---|
| Internal refactor with no public behavior change | Patch. |
| Documentation correction for existing behavior | Patch or docs-only publish path. |
| Bug fix preserving documented behavior | Patch. |
| New optional annotation member with safe default | Minor. |
| New generated endpoint enabled only by opt-in template/include/policy | Minor. |
| New optional runtime module behavior | Minor. |
| Starter adds optional capability only through an opt-in starter | Minor. |
| Generated DTO/route/request/response shape break | Major. |
| Public annotation or runtime contract removal | Major. |
| Configuration key removal or incompatible default | Major unless still supported through compatibility alias. |
| Removing deprecated public behavior | Major unless the documented deprecation policy explicitly allowed earlier removal. |

## Patch Release Caution

Patch releases should be safe upgrades. A bug fix can still be too risky for patch if it changes generated API behavior, security visibility, row filtering semantics, or accepted request shape. Use [Compatibility Policy](compatibility-policy.md) before deciding.

## Related Documentation

- [Compatibility Policy](compatibility-policy.md)
- [Deprecation Policy](deprecation-policy.md)
- [Release Process](release-process.md)
