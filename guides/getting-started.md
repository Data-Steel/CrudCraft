---
title: Getting Started
summary: Install the CrudCraft starter, enable annotation processing, and boot your first generated CRUD API.
sidebar: guides
---

# Getting Started with CrudCraft

CrudCraft brings compile-time code generation to Spring Boot. This guide shows how to add the starter, enable annotation processing, define an entity, and run the resulting endpoints.

## Prerequisites

- Java 17+
- Maven 3.8 or later
- Spring Boot 3.x
- Any JPA-compatible database

## Step 1 — Add the Starter

Include the CrudCraft starter in your `pom.xml`:

```xml
<dependency>
  <groupId>nl.datasteel.crudcraft</groupId>
  <artifactId>crudcraft-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

The starter pulls in annotations, the annotation processor, runtime support, and optional security integration.

## Step 2 — Enable Annotation Processing

CrudCraft generates code during compilation. Ensure your build or IDE enables annotation processing:

- **Maven**: enabled by default. Run `mvn compile` and inspect `target/generated-sources/crudcraft`.
- **IntelliJ**: Settings → Build → Compiler → Annotation Processors → *Enable annotation processing*.

## Step 3 — Define an Entity

Create a JPA entity and mark it with `@CrudCrafted`. Use `@Dto` to expose fields and `@Request` to accept input.

```java
import jakarta.persistence.*;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import java.util.UUID;

@Entity
@CrudCrafted
public class Book {

  @Id
  @GeneratedValue
  @Dto
  private UUID id;

  @Dto
  @Request
  private String title;

  @Dto
  @Request
  private String author;
}
```

## Step 4 — Build the Project

Compile the project:

```bash
mvn clean compile
```

CrudCraft produces DTOs, a MapStruct mapper, repository, service, controller, and a `BookSearchRequest`. Stubs marked as editable are generated once and can be safely modified.

## Step 5 — Run the Application

Start Spring Boot:

```bash
mvn spring-boot:run
```

Default endpoints include:

- `GET /books`
- `POST /books`
- `GET /books/{id}`
- `PUT /books/{id}`
- `PATCH /books/{id}`
- `DELETE /books/{id}`

Requests and responses use the generated DTOs. Pagination and sorting parameters are supported out of the box.

## Step 6 — Optional Configuration

Tune limits in `application.properties`:

```properties
crudcraft.api.max-page-size=200
crudcraft.export.max-csv-rows=50000
crudcraft.export.max-json-rows=20000
crudcraft.export.max-xlsx-rows=10000
```

## Next Steps

- Study [Annotate Your Entities](/guides/entity-annotations.md) for advanced mapping options.
- Explore [Generated Layers](/guides/generated-layers.md) to see what code is produced.
- Review [Security Overview](/guides/security/overview.md) when ready to protect your endpoints.

