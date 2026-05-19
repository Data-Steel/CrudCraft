---
title: "Documentation Requirements"
description: "Defines the required structure, metadata, writing rules, examples, quality gates, and site rules for CrudCraft written documentation."
section: "Documentation"
audience:
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/documentation-inventory"
  - "/features"
  - "/"
---

# Documentation Requirements

CrudCraft documentation must be clear, accurate, practical, and easy to scan. It should help people complete real development work without hiding important assumptions.

Use this page as the mandatory standard for every written documentation file.

## Who this page is for

This page is for contributors and maintainers who create, edit, review, or validate CrudCraft documentation.

## When to use this page

Use this page before writing or reviewing a public Markdown or MDX documentation page.

## When not to use this page

Do not use this page to learn a CrudCraft feature. Use Feature Guides for both task-oriented examples and exact feature facts.

## Documentation model

CrudCraft written docs follow a Diataxis-style split:

| Section | Purpose | Reader intent |
|---|---|---|
| Quick Start | Tutorial path for a first working API. | Learn by doing |
| Feature Guides | Canonical feature docs with examples, exact facts, configuration, and generated behavior. | Solve a concrete task or inspect feature behavior |
| Architecture | Explain internal system design. | Understand design and tradeoffs |
| Contributor Handbook | Direct rules for project contribution. | Change the repo correctly |
| Maintainer Handbook | Release, quality, compatibility, and project stewardship rules. | Keep the project releasable |

## File standard

Every written documentation file must:

1. Be written in English.
2. Have exactly one H1 title.
3. Start with a short summary that explains what the page helps the reader do or understand.
4. Define the intended readers.
5. State when the page should be used.
6. State when the page should not be used, if relevant.
7. Use a clear heading hierarchy with no skipped levels.
8. Use descriptive section names.
9. Include at least one concrete example when the page explains a feature, configuration, extension point, or workflow.
10. Prefer runnable or copy-pasteable examples.
11. Mark placeholder values clearly.
12. Explain required prerequisites before showing steps.
13. Link to related documentation pages.
14. Link to lower-level code docs when implementation details matter.
15. Avoid unexplained jargon.
16. Introduce CrudCraft-specific terms before using them heavily.
17. Keep each page focused on one topic.
18. Avoid duplicating content from another page.
19. Point to the canonical page when a topic is explained elsewhere.
20. Include failure modes, common mistakes, or troubleshooting notes when relevant.
21. Include version or compatibility notes when behavior differs between modules or releases.
22. Use consistent names for modules, annotations, packages, configuration keys, and generated artifacts.
23. Use code blocks with language identifiers.
24. Use tables only when they improve scanning.
25. End with a final `Related documentation` section.

## Required frontmatter

Frontmatter is a contract for generated sidebars, sitemaps, related links, and future validation.

```yaml
---
title: "Authorization"
description: "Learn how CrudCraft applies authorization rules to generated CRUD endpoints."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
  - "Advanced users"
difficulty: "intermediate"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
related:
  - "/feature-guides/security"
  - "/architecture/security-model"
  - "/feature-guides/security/testing"
---
```

Required keys:

| Key | Required | Notes |
|---|---|---|
| `title` | Yes | Must be unique within public docs. |
| `description` | Yes | Must be unique and useful for search snippets. |
| `section` | Yes | Must match a top-level documentation area. |
| `audience` | Yes | Must list intended readers. |
| `status` | Yes | Use `draft`, `stable`, `deprecated`, or `archived`. |
| `related` | Yes for non-index pages | Must point to canonical related docs. |

## Required page template

Use this structure for written docs unless a stricter section template applies.

````md
---
title: "Security Runtime Module"
description: "Learn how CrudCraft's security runtime module protects generated APIs with authentication, authorization, and tenant-aware access rules."
section: "Feature Guides"
category: "Security"
audience:
  - "Beginner users"
  - "Advanced users"
status: "stable"
related:
  - "/feature-guides/security"
---

# Security Runtime Module

CrudCraft's security runtime module adds authentication, authorization, and access-control behavior to generated CRUD APIs.

Use this page when you want to protect generated endpoints, enforce access rules, or understand how security integrates with generated code.

## Who this page is for

This page is for developers who want to use CrudCraft security features in an application.

## Prerequisites

Before using this feature, you should understand:

- How CrudCraft generates CRUD APIs
- How runtime modules are enabled
- How your application handles authentication

## Quick example

```java
// Example here.
```

## What this feature does

Explain the feature in plain English.

## What this feature does not do

Explain boundaries clearly.

## Configuration

```yaml
crudcraft:
  security:
    enabled: true
```

## Behavior

Explain runtime behavior.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Example | Explanation | Fix |

## Related documentation

- [Security](feature-guides/security/)
````

## Quick Start standard

Quick Start contains exactly five pages:

1. Choose a Starter
2. Generate Your First API
3. Run the API Locally
4. Associate Your First Entities
5. Enable Your First Runtime Feature

Each Quick Start page must include:

| Section | Requirement |
|---|---|
| Goal | State what the reader will accomplish. |
| Before you start | List requirements before steps. |
| Steps | Use ordered instructions. |
| Expected result | Show what should exist or happen. |
| Next step | Link to the next page. |

## Feature Guide standard

Each feature must have its own directory. The directory `index.md` is a route map, not a long essay.

```text
feature-guides/security/
+-- index.md
+-- authentication.md
+-- authorization.md
+-- role-based-access.md
+-- tenant-isolation.md
+-- field-level-security.md
+-- configuration.md
+-- testing.md
+-- troubleshooting.md
```

Every feature guide must include:

- One minimal example.
- One realistic example.
- One common mistake.
- One troubleshooting or failure-mode note when relevant.
- One final `Related documentation` section.

## Feature fact standard

Feature facts must be placed in the most relevant Feature Guide page. A page that documents a feature must include exact module names, public annotations/classes, generated behavior, configuration properties, examples, boundaries, common mistakes, and related links.

## Architecture standard

Every architecture page must include:

- Context.
- Problem.
- Design.
- Responsibilities.
- Non-responsibilities.
- Important invariants.
- Extension points.
- Failure modes.
- Related ADRs.
- Related code docs.

Required invariant examples:

```text
Codegen must not depend on runtime module implementation details.
Runtime modules must not require generated code regeneration unless explicitly documented.
Generated code must remain deterministic for the same input model and configuration.
```

## Contributor and maintainer wording

Contributor Handbook and Maintainer Handbook pages must be direct:

- Use `must` for required behavior.
- Use `must not` for forbidden behavior.
- Avoid vague permission language.
- Every pull request rule must be testable or reviewable.

## Clear writing rules

Every page must:

- Use explicit prerequisites.
- Use exact package, class, annotation, module, and configuration names.
- Avoid hidden assumptions.
- Avoid `simply`, `just`, and `obviously`.
- Put steps in order.
- Show expected output where possible.
- Separate user-facing behavior from internal implementation.

## Site requirements

The documentation site must satisfy these rules:

- `docs.crudcraft.dev` has its own `robots.txt`.
- `docs.crudcraft.dev` has its own `sitemap.xml`.
- Every canonical page has a canonical URL.
- Old URLs redirect to new canonical URLs.
- Generated duplicate pages are not indexable.
- Every page has a unique title.
- Every page has a unique meta description.
- Every page has stable, human-readable URLs.
- Every heading has a stable anchor link.
- The docs build fails on broken internal links.
- The docs build fails on missing required frontmatter.
- The docs build fails on duplicate slugs.
- The docs build fails on orphaned public pages.

## File names and routes

Use kebab-case for files and routes.

Good:

```text
authorization.md
tenant-isolation.md
runtime-architecture.md
generated-code-lifecycle.md
```

Bad:

```text
Authorization.md
tenantIsolation.md
RuntimeArchitecture.md
security_stuff.md
```

## Related documentation

- [Documentation Inventory](documentation-inventory.md)
- [CrudCraft Feature Map](features.md)
- [CrudCraft Documentation](./)
