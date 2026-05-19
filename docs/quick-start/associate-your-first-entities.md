---
title: "Associate Your First Entities"
description: "Associate Book records with an Author through a JPA many-to-one relationship in a CrudCraft-generated API."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
related:
  - "/quick-start/run-the-api-locally"
  - "/quick-start/enable-your-first-runtime-feature"
  - "/architecture/generated-code-lifecycle"
---

# Associate Your First Entities

CrudCraft can generate APIs for related JPA entities, including a `Book` that belongs to an `Author`.

Use this page to add a `Book` entity with a `@ManyToOne` association to the `Author` entity from the previous page.

## Who this page is for

This page is for developers who already generated and ran the first `Author` API.

## When to use this page

Use this page after the `Author` endpoint works locally.

## When not to use this page

Do not use this page for advanced relationship management or bidirectional synchronization. This page only covers the first many-to-one association.

## Goal

By the end of this page, your project will generate a `Book` API whose records can reference an `Author`.

## Before you start

You need:

- The `Author` entity from [Generate Your First API](generate-your-first-api.md).
- A local application that can create an author named `DataSteel`.
- A clean compile before regenerating.

## Step 1: Add the Book entity

```java
package com.example.demo.book;

import com.example.demo.author.Author;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

@Entity
@CrudCrafted
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @Dto
    @Request
    @NotBlank
    private String title;

    @Dto
    @Request
    @ManyToOne(fetch = FetchType.LAZY)
    private Author author;

    protected Book() {
    }

    public Book(String title, Author author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
```

## Step 2: Regenerate

```bash
./mvnw clean compile
```

On Windows without a Unix shell, use:

```powershell
.\mvnw.cmd clean compile
```

## Step 3: Create DataSteel if it does not exist

```bash
curl -i -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"DataSteel\"}"
```

Expected result: the response contains an author id. The examples below use `<author-id>` as a placeholder for that value.

## Step 4: Create the first book for DataSteel

The exact request shape depends on the generated request DTO. Many generated relationship requests use an id reference for related entities.

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"CrudCraft Handbook\",\"authorId\":<author-id>}"
```

Replace `<author-id>` with the id returned when creating `DataSteel`.

## Expected result

The project should compile, generated `Book` artifacts should exist, and a `Book` record should be associated with the `DataSteel` author.

Expected generated artifacts include:

```text
BookRequestDto
BookResponseDto
BookMapper
BookRepository
BookService
BookController
```

## Common mistakes

| Mistake | Why it causes problems | Correct approach |
|---|---|---|
| Creating `Book` before `Author` | The association has no target record. | Create `DataSteel` first. |
| Using the wrong relationship request field | Generated DTO names can differ by template. | Inspect `BookRequestDto`. |
| Making the first relationship bidirectional | It adds mapping complexity too early. | Start with one `@ManyToOne`. |

## Next step

Continue with [Enable Your First Runtime Feature](enable-your-first-runtime-feature.md).

## Related documentation

- [Run the API Locally](run-the-api-locally.md)
- [Enable Your First Runtime Feature](enable-your-first-runtime-feature.md)
- [Generated Code Lifecycle](../architecture/generated-code-lifecycle.md)
