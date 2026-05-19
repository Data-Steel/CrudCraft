---
title: "Compatibility Policy"
description: "Review CrudCraft backward compatibility across annotations, generated source, generated HTTP APIs, runtime contracts, starters, configuration, and docs."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/architecture/contract-model"
  - "/maintainer-handbook/versioning-policy"
  - "/feature-guides/runtime-modules"
---

# Compatibility Policy

Compatibility review answers whether existing users can upgrade without changing annotated source, generated-code usage, HTTP clients, configuration, dependencies, or documented workflows.

## Surfaces To Review

| Surface | Breaking examples |
|---|---|
| `crudcraft-api` annotations/enums/interfaces | Removing an annotation member, renaming `CrudEndpoint`, changing defaults. |
| Generated Java source | DTO record components renamed, controller/service class signatures changed, mapper/repository names changed. |
| Generated HTTP API | Route, method, parameter, payload, status, pagination, or response wrapper changed. |
| Runtime contracts | Generated code calls a removed method/class or runtime semantics reject previously valid generated calls. |
| Starters/dependencies | Optional feature becomes transitive through core, dependency removed from a starter, incompatible Spring Boot behavior. |
| Configuration | Key removed, default changed, value semantics changed. |
| Documentation | Docs promise behavior the implementation does not provide, or omit required upgrade guidance. |

## Compatibility Outcomes

| Outcome | Maintainer action |
|---|---|
| No user-visible change | Normal quality gates. |
| Backward-compatible feature | Minor release when shipped as artifact behavior. |
| Backward-compatible bug fix | Patch or minor depending on user-visible risk. |
| Behavior correction with possible user impact | Document clearly; decide patch/minor/major through versioning policy. |
| Breaking change | Redesign to preserve compatibility or require major release handling. |

## Generated API Rule

Generated does not mean private. If users compile against it or clients call it, it is a public contract. Maintainers must review generated diffs for DTO shape, route shape, search/export/security endpoint changes, metadata output, and request/response status behavior.

## Related Documentation

- [Contract Model](../architecture/contract-model.md)
- [Versioning Policy](versioning-policy.md)
- [Runtime Modules](../feature-guides/runtime-modules/)
