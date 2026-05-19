---
title: "Security Runtime Module"
description: "Use the CrudCraft security runtime module for generated endpoint security, field filtering, and row isolation."
section: "Feature Guides"
category: "Runtime Modules"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
  - "crudcraft-spring-boot-starter-security"
related:
  - "/feature-guides/security"
  - "/feature-guides/security"
  - "/architecture/security-model"
---

# Security Runtime Module

The security runtime module supports generated security metadata, field filtering, row isolation, and security adapters.

Use this page when generated APIs use CrudCraft security features.

## Who this page is for

This page is for developers adding security runtime support.

## When to use this page

Use this page when security annotations, policies, field rules, or row rules are present.

## When not to use this page

Do not use this page to design authentication. CrudCraft uses your Spring Security setup.

## Prerequisites

- Spring Security is configured.
- Generated security behavior is intended.
- Security tests cover allowed and denied paths.

## Quick example

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-security</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Expected result: generated security-aware APIs can call runtime security helpers.

## Common mistakes

| Mistake | Why it is wrong | Correct approach |
|---|---|---|
| Adding security starter without Spring Security config | No authentication boundary exists. | Configure Spring Security. |
| Securing generated endpoints but not custom endpoints | Custom controllers are application-owned. | Secure custom code separately. |
| Testing only admin users | Denied behavior is untested. | Test unauthorized, forbidden, and allowed requests. |

## Related documentation

- [Security Guides](../security/)
- [Security Reference](../security/)
- [Security Model](../../architecture/security-model.md)
