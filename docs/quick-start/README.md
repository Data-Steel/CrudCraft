---
title: "Quick Start"
description: "Follow the five-page CrudCraft tutorial path for creating a first generated API."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
related:
  - "/quick-start/choose-a-starter"
  - "/documentation-inventory"
  - "/feature-guides"
---

# Quick Start

Quick Start is the shortest path to a running CrudCraft-generated Spring Boot API.

Use this section when you want to try CrudCraft in a small application before reading deeper feature documentation.

## Who this page is for

This page is for Java developers who want a first working CrudCraft API with minimal detours.

## When to use this page

Use this page when you are new to CrudCraft or want to verify the basic generation flow.

## When not to use this page

Do not use this section as a complete feature manual. Use Feature Guides and Feature Guides after the first API runs.

## Goal

By the end of Quick Start, you will have a Spring Boot application with a generated CRUD API and one enabled runtime feature.

## Before you start

You need:

- Java 21.
- Maven 3.9 or the Maven wrapper from your project.
- A Spring Boot application that can compile.
- Basic familiarity with JPA entities.

## Pages

| Order | Page | Result |
|---|---|---|
| 1 | [Choose a Starter](choose-a-starter.md) | You know which CrudCraft starter to use. |
| 2 | [Generate Your First API](generate-your-first-api.md) | The annotation processor generates an Author API. |
| 3 | [Run the API Locally](run-the-api-locally.md) | The application starts and the generated endpoint responds. |
| 4 | [Associate Your First Entities](associate-your-first-entities.md) | A Book is associated with the DataSteel author. |
| 5 | [Enable Your First Runtime Feature](enable-your-first-runtime-feature.md) | Search returns only the CrudCraft book. |

## Expected result

After the final page, generated `/api/authors` and `/api/books` endpoints should respond locally, and a search for `CrudCraft` should return only the matching book.

## Next step

Start with [Choose a Starter](choose-a-starter.md).

## Related documentation

- [Feature Guides](../feature-guides/)
- [Feature Guides](../feature-guides/)
- [Documentation Requirements](../doc-requirement.md)
