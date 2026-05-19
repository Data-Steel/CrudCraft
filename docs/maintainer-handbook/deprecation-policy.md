---
title: "Deprecation Policy"
description: "Deprecate CrudCraft public annotations, generated behavior, runtime contracts, configuration keys, and documented workflows with a clear replacement path."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/versioning-policy"
  - "/maintainer-handbook/compatibility-policy"
  - "/maintainer-handbook/release-process"
---

# Deprecation Policy

Deprecation is a compatibility tool. It gives users time, replacement behavior, and clear release notes before a public surface disappears or changes incompatibly.

## Deprecation Must State

- Deprecated API, generated behavior, configuration key, route, or workflow.
- Replacement.
- Earliest removal version.
- Whether generated source changes.
- Whether runtime behavior changes.
- Migration example.
- Tests that keep old behavior working until removal.

## Removal Gate

Before removing deprecated behavior, maintainers must verify:

- earliest removal version has been reached;
- replacement behavior is documented and tested;
- release notes call out the removal;
- generated output impact is reviewed;
- version impact follows [Versioning Policy](versioning-policy.md).

## Example

```text
Deprecated: generated search query parameter `limit`.
Replacement: Spring Data `size`.
Earliest removal: 3.0.0.
Migration: replace `limit=20` with `size=20`.
Generated API impact: generated search endpoint docs change; existing route remains until removal.
```

## Related Documentation

- [Versioning Policy](versioning-policy.md)
- [Compatibility Policy](compatibility-policy.md)
- [Release Process](release-process.md)
