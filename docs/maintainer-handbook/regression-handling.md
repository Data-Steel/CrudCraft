---
title: "Regression Handling"
description: "Handle released CrudCraft regressions with reproduction, contract-level tests, narrow fixes, documentation, and patch-release decisions."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/security-policy"
  - "/maintainer-handbook/release-process"
  - "/architecture/testing-architecture"
---

# Regression Handling

A regression is a break in behavior CrudCraft previously supported through released code, generated output, tests, or docs. Treat it as a contract problem first, then an implementation problem.

## Workflow

1. Confirm whether security impact is possible; if yes, use [Security Policy](security-policy.md).
2. Identify the affected contract: annotation, generated source, HTTP API, runtime module, starter, configuration, docs, or tooling.
3. Reproduce on the newest affected release and, when possible, the last known good release.
4. Add a failing regression test at the contract layer.
5. Apply the smallest fix.
6. Run targeted tests and the relevant quality gates.
7. Update docs or troubleshooting when user guidance changes.
8. Decide whether a patch release is required.

## Patch Release Is Usually Required When

- existing applications no longer compile;
- generated route/DTO/request/response behavior changed unintentionally;
- runtime rejects a previously valid request;
- row/field security behavior regressed;
- starter composition broke application startup;
- docs sent users into a broken supported workflow.

## Regression Record

Maintainers should record:

```text
Regression:
Affected versions:
Last known good:
Contract:
Reproduction:
Fix:
Tests:
Release decision:
```

## Related Documentation

- [Security Policy](security-policy.md)
- [Release Process](release-process.md)
- [Testing Architecture](../architecture/testing-architecture.md)
