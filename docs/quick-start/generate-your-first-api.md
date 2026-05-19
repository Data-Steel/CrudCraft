---
title: "Generate Your First API"
description: "Generate a first CrudCraft API for an Author entity and create DataSteel as the first record."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
crudcraft_modules:
  - "crudcraft-api"
  - "crudcraft-codegen"
  - "crudcraft-spring-boot-starter-core"
related:
  - "/quick-start/choose-a-starter"
  - "/quick-start/run-the-api-locally"
  - "/architecture/codegen-architecture"
---

# Generate Your First API

CrudCraft generates CRUD API layers from annotated JPA entities during Java compilation.

Use this page to generate a first API for an `Author` entity and create `DataSteel` as the first record.

## Who this page is for

This page is for developers who have chosen a starter and want to see CrudCraft generate a real endpoint.

## When to use this page

Use this page after choosing a starter and before adding relationships between entities.

## When not to use this page

Do not use this page for relationship mapping. The next Quick Start page covers `@ManyToOne`.

## Goal

By the end of this page, your project will generate an `Author` API and accept a request that creates an author named `DataSteel`.

## Before you start

You need:

- The starter selected in [Choose a Starter](choose-a-starter.md).
- A Spring Boot project with JPA configured.
- Java 21.
- Maven compiler plugin configuration access.

## Step 1: Add the annotation API

Add `crudcraft-api` so application code can use CrudCraft annotations.

```xml
<dependency>
    <groupId>nl.datasteel.crudcraft</groupId>
    <artifactId>crudcraft-api</artifactId>
    <version>${crudcraft.version}</version>
</dependency>
```

## Step 2: Add the annotation processor

Configure `crudcraft-codegen` as an annotation processor. Keep the version identical to the runtime starter version.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>21</source>
        <target>21</target>
        <annotationProcessorPaths>
            <path>
                <groupId>nl.datasteel.crudcraft</groupId>
                <artifactId>crudcraft-codegen</artifactId>
                <version>${crudcraft.version}</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Replace `${mapstruct.version}` with the MapStruct version used by your project.

## Step 3: Create the Author entity

```java
package com.example.demo.author;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

@Entity
@CrudCrafted
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    @Dto
    @Request
    @NotBlank
    private String name;

    protected Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

## Step 4: Compile the project

```bash
./mvnw clean compile
```

On Windows without a Unix shell, use:

```powershell
.\mvnw.cmd clean compile
```

## Expected result

Compilation should succeed and generated source should appear under:

```text
target/generated-sources/annotations
```

For the `Author` entity, expect generated artifacts such as:

```text
AuthorRequestDto
AuthorResponseDto
AuthorMapper
AuthorRepository
AuthorService
AuthorController
```

## Common mistakes

| Mistake | Why it causes problems | Correct approach |
|---|---|---|
| Adding `crudcraft-codegen` as a normal dependency only | Maven may not run it as an annotation processor. | Put `crudcraft-codegen` in `annotationProcessorPaths`. |
| Forgetting `@CrudCrafted` | CrudCraft ignores the entity. | Add `@CrudCrafted` to the entity class. |
| Missing JPA `@Id` | Generated CRUD code cannot identify the entity. | Add exactly one supported identity field. |

## Next step

Continue with [Run the API Locally](run-the-api-locally.md).

## Related documentation

- [Choose a Starter](choose-a-starter.md)
- [Run the API Locally](run-the-api-locally.md)
- [Codegen Architecture](../architecture/codegen-architecture.md)
