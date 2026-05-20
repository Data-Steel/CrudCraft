---
title: "Running Tests"
description: "Choose focused CrudCraft test commands while developing, then return to the full verification gates before review."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/contributor-handbook/writing-tests"
  - "/architecture/testing-architecture"
  - "/contributor-handbook/local-build"
---

# Running Tests

Use focused tests while coding, but do not confuse a fast loop with review evidence. CrudCraft has generated source, runtime modules, starters, sample integration, PIT, docs, and release automation; one module test rarely proves the whole change.

## Core Commands

Full reactor:

```powershell
.\mvnw.cmd -B verify
```

One module plus upstream dependencies:

```powershell
.\mvnw.cmd -B -pl crudcraft-codegen -am test
```

One test class:

```powershell
.\mvnw.cmd -B -pl crudcraft-codegen -Dtest=GoldenTestRunner test
```

Quality loop for changed modules:

```powershell
.\scripts\quality-loop.ps1
```

On Unix-like shells, use `./mvnw` for Maven commands.

## Focused Loop By Change

| Change | Start with | Then run |
|---|---|---|
| API annotation/enum | `.\mvnw.cmd -B -pl crudcraft-api test` | Codegen tests that consume it. |
| Descriptor extraction | `.\mvnw.cmd -B -pl crudcraft-codegen -Dtest=*Extractor* test` | Processor/golden tests. |
| Controller/service generation | `.\mvnw.cmd -B -pl crudcraft-codegen -Dtest=GoldenTestRunner test` | Sample app scenario if behavior changes. |
| Runtime core | `.\mvnw.cmd -B -pl crudcraft-runtime-core -am test` | Sample app if generated services rely on it. |
| Search/projection/export/security runtime | Affected runtime module test | Sample app HTTP/integration test for generated paths. |
| Starter composition | Affected starter tests | Full reactor. |
| Docs only | link check/docs build | `node --check` for changed scripts. |
| Workflow/script | Script-specific local run | Maintainer review of CI impact. |

## CI-Specific Gates To Know

CI also runs checks that are expensive or environment-dependent:

- full `verify` with `-Dcrudcraft.tck.postgres.required=true`;
- `GoldenTestRunner` drift check;
- `license:check`;
- aggregate Javadocs with warnings failing;
- `scripts/verify-quality-reports.ps1`;
- `scripts/update-doc-index.ps1`;
- `scripts/check-doc-drift.ps1`;
- PIT matrix for `crudcraft-codegen`;
- JMH benchmark build/run;
- generated roundtrip PIT against `crudcraft-sample-app`;
- Sonar analysis and quality gate.

Dependency scanning is split out from the main PR workflow. OSV runs on PRs, pushes to `main`,
the weekly schedule, and manual dispatch. OWASP Dependency-Check runs from the dependency-scan
workflow only on the weekly schedule or manual dispatch, and still runs as a release preflight.

Contributors do not need to run every CI job locally for every PR, but PR notes must be honest about what was and was not run.

## Related Documentation

- [Writing Tests](writing-tests.md)
- [Testing Architecture](../architecture/testing-architecture.md)
- [Local Build](local-build.md)
