---
title: "Coding Standards"
description: "Follow CrudCraft Java, codegen, runtime, starter, generated-output, and documentation-adjacent coding standards."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/architecture/module-boundaries"
  - "/architecture/contract-model"
  - "/contributor-handbook/review-checklist"
---

# Coding Standards

CrudCraft code should be explicit, deterministic where generation is involved, and respectful of module ownership. New abstractions must earn their place by reducing real complexity or matching an established local pattern.

## General Java Rules

- Target Java 21.
- Keep public APIs small and intentional.
- Prefer immutable values where the existing module already uses them.
- Keep error messages specific enough for users to act on.
- Avoid broad refactors in feature or bug-fix PRs.
- Follow the local package style before introducing a new pattern.
- Keep comments for non-obvious behavior, not line-by-line narration.

The Maven build enforces Checkstyle, PMD, SpotBugs, Javadocs, license headers, tests, and coverage/report checks. Do not tune around a warning without understanding which contract it protects.

## Codegen Rules

Codegen changes have extra constraints because generated files become user application source.

- Generated output must be deterministic.
- Sort data before writing if the source collection order is not guaranteed.
- Do not include absolute paths, current wall-clock timestamps, or environment-specific values in generated files.
- Generated source must never import `nl.datasteel.crudcraft.codegen.*`.
- Generated optional-runtime imports must appear only when the generated feature requires them.
- Golden files are evidence, not approval. Read generated diffs before updating them.
- Processor diagnostics should point to the source element that caused the failure where possible.

## Runtime Rules

- Runtime modules must expose stable contracts to generated code.
- Keep optional capabilities out of `crudcraft-runtime-core` unless the architecture decision changes.
- Use `CrudRuntimeExtension` for neutral generated-service lifecycle hooks.
- Keep security, search, export, projection, and extensions behavior in their owning modules.
- Test failure paths: invalid request, denied access, hidden row, unsupported field/operator, bad export format, missing resource.

## Starter Rules

- Starters compose dependencies and auto-configuration; they do not own feature logic.
- A starter dependency change is user-visible because it changes application classpaths.
- Keep capability starters usable independently from the umbrella starter.
- Update Runtime Modules docs when starter composition changes.

## Documentation-Adjacent Code

When changing `docs-deploy` or scripts:

- keep `docs/` as the canonical source;
- do not hard-code deleted documentation sections;
- update navigation order when adding/removing docs directories;
- run `node --check` for changed `.mjs` files;
- run link/drift checks when docs behavior changes.

## Related Documentation

- [Module Boundaries](../architecture/module-boundaries.md)
- [Contract Model](../architecture/contract-model.md)
- [Review Checklist](review-checklist.md)
