---
title: CrudCraft Overview
summary: High-level introduction to CrudCraft with links to guides and concepts.
sidebar: root
---

# CrudCraft Overview

CrudCraft is a compile-time code generator for Spring Boot that assembles full CRUD APIs from annotated JPA entities. It minimizes boilerplate while preserving explicit control over data exposure, search, and security. Developers annotate their domain model and CrudCraft produces the layers required to ship production-grade endpoints.

## Why CrudCraft

- **Productive**: Generate controllers, services, repositories, DTOs, mappers, and search utilities with a single annotation.
- **Consistent**: Code is generated from templates and can be regenerated, ensuring uniform APIs across modules.
- **Flexible**: Every generated type can be customized via editable stubs or replaced entirely.
- **Feature-rich**: Pagination, bulk operations, searching, and export support are available out of the box.

## Quick Links

**Start building**

- [Getting Started](/guides/getting-started.md) — install the starter and run your first API.
- [Annotate Your Entities](/guides/entity-annotations.md) — learn the annotations CrudCraft understands.

**Explore deeper**

- [Architecture & Modules](/concepts/architecture.md) — understand how the generator works.
- [Security Overview](/guides/security/overview.md) — configure endpoint, field, and row protection.

## Who Should Use It

CrudCraft is designed for Spring Boot teams seeking rapid, maintainable CRUD endpoints without sacrificing control. It is most effective when paired with JPA and MapStruct, but it does not mandate any particular database or security provider.

## Next Steps

- Follow the [Getting Started guide](/guides/getting-started.md).
- Read about [Architecture & Modules](/concepts/architecture.md) to see how generation fits into your build.

