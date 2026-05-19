---
title: "Enable Your First Runtime Feature"
description: "Enable CrudCraft search, create a second book, and verify that searching CrudCraft returns only the matching book."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-search"
related:
  - "/quick-start/associate-your-first-entities"
  - "/feature-guides/search"
  - "/architecture/runtime-architecture"
---

# Enable Your First Runtime Feature

CrudCraft runtime features add behavior around generated APIs without hand-writing the CRUD layer.

Use this page to enable search, create a second book, and verify that a search for `CrudCraft` returns only the CrudCraft book.

## Who this page is for

This page is for developers who have an `Author` and `Book` generated API running locally.

## When to use this page

Use this page after a `Book` can be associated with the `DataSteel` author.

## When not to use this page

Do not use this page for advanced search tuning. Read the Search Feature Guides after this first runtime feature works.

## Goal

By the end of this page, the generated `Book` API will support search on `title`, and searching for `CrudCraft` will return only the CrudCraft book.

## Before you start

You need:

- The `Author` and `Book` entities from the previous Quick Start pages.
- A `DataSteel` author record.
- One existing book titled `CrudCraft Handbook`.
- The same CrudCraft version for all modules.

## Step 1: Add the search starter

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-spring-boot-starter-search</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

Replace `${crudcraft.version}` with the same CrudCraft version used by the core starter, API, and codegen artifacts.

## Step 2: Make Book title searchable

Update `Book.title` to include `@Searchable`.

```java
@Dto
@Request
@Searchable
@NotBlank
private String title;
```

Expected result: CrudCraft generates search support for book titles.

## Step 3: Regenerate the API

```bash
./mvnw clean compile
```

On Windows without a Unix shell, use:

```powershell
.\mvnw.cmd clean compile
```

## Step 4: Start the application

```bash
./mvnw spring-boot:run
```

On Windows without a Unix shell, use:

```powershell
.\mvnw.cmd spring-boot:run
```

## Step 5: Add a second book

Create another book for the same author that does not contain `CrudCraft` in the title.

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Spring Boot Notes\",\"authorId\":<author-id>}"
```

Replace `<author-id>` with the id of the `DataSteel` author.

## Step 6: Search for CrudCraft

```bash
curl -i "http://localhost:8080/api/books/search?title.contains=CrudCraft"
```

## Expected result

The response should contain `CrudCraft Handbook` and should not contain `Spring Boot Notes`.

```json
{
  "content": [
    {
      "title": "CrudCraft Handbook"
    }
  ]
}
```

Your exact response wrapper may include additional pagination fields.

## Common mistakes

| Mistake | Why it causes problems | Correct approach |
|---|---|---|
| Adding the search starter without `@Searchable` | No useful search fields are generated. | Add `@Searchable` to `Book.title` and regenerate. |
| Searching before adding the second book | You cannot prove filtering works. | Seed one matching and one non-matching record. |
| Using a stale generated API | The search endpoint may not exist yet. | Run `./mvnw clean compile`. |

## Next step

Read [Search](../feature-guides/search/) to learn operators, sorting, pagination, nested fields, and tests.

## Related documentation

- [Associate Your First Entities](associate-your-first-entities.md)
- [Search](../feature-guides/search/)
- [Runtime Architecture](../architecture/runtime-architecture.md)
