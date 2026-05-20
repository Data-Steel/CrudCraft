---
title: "Dependency Management"
description: "Review CrudCraft dependency, plugin, starter, JavaPoet, MapStruct, Spring Boot, security, docs, and build-tool upgrades."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/compatibility-policy"
  - "/contributor-handbook/local-build"
  - "/feature-guides/runtime-modules"
---

# Dependency Management

Dependency changes can alter generated Java, runtime behavior, starter classpaths, docs deploy output, security exposure, and CI gates. Treat them as product changes when users can observe the result.

## Upgrade Rules

- Prefer one dependency group per PR.
- Do not mix dependency upgrades with unrelated features.
- Review generated output when JavaPoet, MapStruct, annotation processing, compiler, formatting, or Spring annotation behavior changes.
- Review starter composition when runtime or Spring Boot dependencies change.
- Review security policy when auth, JWT, Spring Security, serialization, validation, or export libraries change.
- Review docs deploy when Node/Vite/VitePress dependencies change.

## Required Evidence

| Dependency type | Evidence |
|---|---|
| Maven plugin | Full reactor or affected lifecycle proof, plus CI impact. |
| JavaPoet / MapStruct / annotation processing | Golden diff review and codegen tests. |
| Spring Boot / Spring Data / JPA | Runtime tests, starter review, sample app integration. |
| Security dependency | Security tests and dependency scan outcome. |
| Export serialization/CSV/XLSX | Export runtime tests and sample export behavior. |
| Docs deploy dependency | `npm run build -- --source ..` from `docs-deploy`. |
| Test tool | Demonstrate that failures still fail and reports are still parsed. |

## Automated Inputs

Maintainers should use:

- Dependabot PR metadata;
- OSV scanner workflow for PR, `main`, weekly, and manual dependency findings;
- OWASP Dependency-Check from the weekly/manual dependency-scan workflow and release preflight;
- Maven dependency tree when starter composition changes;
- generated diff/golden checks for processor-related upgrades.

## Related Documentation

- [Compatibility Policy](compatibility-policy.md)
- [Local Build](../contributor-handbook/local-build.md)
- [Runtime Modules](../feature-guides/runtime-modules/)
