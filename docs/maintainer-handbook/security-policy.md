---
title: "Security Policy"
description: "Triage, fix, verify, document, and release CrudCraft security-sensitive issues affecting generated endpoints, row isolation, field security, starters, dependencies, or docs."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/architecture/security-model"
  - "/feature-guides/security/testing"
  - "/maintainer-handbook/regression-handling"
---

# Security Policy

Security-sensitive issues include data exposure, authorization bypass, incorrect row isolation, field filtering gaps, unsafe generated defaults, vulnerable dependencies, or docs that overpromise protection.

## Triage Surfaces

| Surface | Examples |
|---|---|
| Generated endpoint authorization | Missing `@PreAuthorize`, wrong endpoint expression, unsafe fallback. |
| Row security | Tenant/client/owner filter missing from read/search/update/delete/export path. |
| Field security | Protected field visible on read/export/projection or writable despite policy. |
| Runtime security module | `FieldSecurityAdapter`, `RowSecurityRuntimeExtension`, `PrincipalScopeAccessor` behavior. |
| Starter/dependency | Security module not wired, wrong dependency version, vulnerable library. |
| Documentation | Docs imply authentication/authorization behavior CrudCraft does not own. |

## Response Rules

1. Acknowledge and restrict sensitive public detail when needed.
2. Identify affected modules and known versions.
3. Reproduce through the contract path, preferably generated endpoint or runtime API.
4. Add denied-path regression tests.
5. Keep the fix narrow.
6. Update Security Feature Guides and Security Model when behavior or guidance changes.
7. Decide release urgency with compatibility and versioning policy.

## Required Tests

Security fixes should include at least one test that proves the unsafe path is blocked. For generated behavior, prefer sample app or generated integration coverage in addition to runtime unit tests.

Examples:

- non-admin cannot call generated `EXPORT`;
- user from tenant A cannot read/search/update/delete tenant B row;
- protected field is removed from normal read and export;
- write policy blocks or handles protected write as documented.

## Related Documentation

- [Security Model](../architecture/security-model.md)
- [Testing Secured APIs](../feature-guides/security/testing.md)
- [Regression Handling](regression-handling.md)
