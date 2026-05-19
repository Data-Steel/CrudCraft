---
title: "CI/CD"
description: "Maintain CrudCraft CI, dependency scanning, reproducible builds, docs dispatch, release automation, and required checks."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/quality-gates"
  - "/maintainer-handbook/release-process"
  - "/contributor-handbook/local-build"
---

# CI/CD

CrudCraft CI is not generic Maven automation. It protects generated source determinism, runtime behavior, docs drift, security scanning, mutation coverage, benchmarks, reproducibility, and release publication.

## Workflows

| Workflow | Trigger | Maintainer responsibility |
|---|---|---|
| `.github/workflows/ci.yml` | Pull requests to `main`/`develop`. | Keep verify, golden, license, Javadoc, quality report, dependency-check, docs drift, PIT, benchmark, generated roundtrip, and Sonar gates meaningful. |
| `.github/workflows/dependency-scan.yml` | PRs, pushes to `main`, weekly schedule, manual. | Treat OSV findings as dependency/security review input. |
| `.github/workflows/reproducible-build.yml` | PRs, pushes to `main`, manual. | Ensure package output is reproducible across two clean builds. |
| `.github/workflows/release-please.yml` | Push to `main`, manual. | Keep automated release PR generation aligned with versioning policy. |
| `.github/workflows/cut-release.yml` | Manual. | Use only for intentional SemVer tag/release creation from an approved ref. |
| `.github/workflows/release.yml` | Published GitHub release. | Publish signed Maven Central artifacts, verify publication, upload SBOM/Sigstore bundles, dispatch docs deploy. |
| `.github/workflows/dispatch-docs.yml` | Called by release workflow. | Send release tag/SHA/version payload to the docs deployer. |

## Required CI Signals

The main PR workflow currently checks:

- `mvn verify` with PostgreSQL-required TCK flag and Javadoc warnings failing;
- `GoldenTestRunner` generated-source drift;
- license headers;
- aggregate Javadocs;
- parsed quality reports through `scripts/verify-quality-reports.ps1`;
- OWASP Dependency-Check with CVSS threshold;
- documentation index validation and doc drift;
- JaCoCo XML artifact upload;
- PIT mutation matrix for codegen;
- JMH paging benchmark build/run;
- generated roundtrip PIT for sample app;
- Sonar PR analysis and quality gate.

Removing one of these requires either a replacement with equivalent risk coverage or an explicit policy change.

## Workflow Change Rules

- Never weaken a gate to make a PR pass.
- Split slow checks only when coverage remains equivalent.
- Keep policy in this handbook; workflows implement it.
- Release workflows must publish from reviewed tags/releases, not arbitrary local state.
- Secrets used by release and docs dispatch must stay scoped to the job that needs them.

## Related Documentation

- [Quality Gates](quality-gates.md)
- [Release Process](release-process.md)
- [Local Build](../contributor-handbook/local-build.md)
