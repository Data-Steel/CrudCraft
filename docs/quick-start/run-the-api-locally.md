---
title: "Run the API Locally"
description: "Start a Spring Boot application with generated CrudCraft endpoints and verify the first API response."
section: "Quick Start"
audience:
  - "Beginner users"
status: "stable"
crudcraft_modules:
  - "crudcraft-spring-boot-starter-core"
related:
  - "/quick-start/generate-your-first-api"
  - "/quick-start/associate-your-first-entities"
  - "/architecture/runtime-architecture"
---

# Run the API Locally

Generated CrudCraft controllers are Spring MVC controllers and run inside your Spring Boot application.

Use this page to start the application, call the generated `Author` endpoint, and create the `DataSteel` author.

## Who this page is for

This page is for developers who have generated their first CrudCraft API and want to test it locally.

## When to use this page

Use this page after `./mvnw clean compile` succeeds.

## When not to use this page

Do not use this page for production deployment. It only covers local verification.

## Goal

By the end of this page, your local application will serve the generated `Author` endpoint.

## Before you start

You need:

- A successful compile from [Generate Your First API](generate-your-first-api.md).
- A configured local database.
- Spring Boot application properties that allow the app to start.

For a first local run, an H2 database is enough.

```properties
spring.datasource.url=jdbc:h2:mem:crudcraft;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

## Step 1: Start the application

Run the Spring Boot app from the project root.

```bash
./mvnw spring-boot:run
```

On Windows without a Unix shell, use:

```powershell
.\mvnw.cmd spring-boot:run
```

## Step 2: Call the generated authors endpoint

The exact route depends on your generated controller configuration. For an `Author` model, the default route commonly follows the model name.

```bash
curl -i http://localhost:8080/api/authors
```

If your project uses another context path, include it in the URL.

## Step 3: Create DataSteel

Post a JSON request body that matches the generated request DTO.

```bash
curl -i -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"DataSteel\"}"
```

## Step 4: Read the collection again

```bash
curl -i http://localhost:8080/api/authors
```

## Expected result

The first collection request should return `200 OK`. The create request should return a successful create response, and the next collection request should include the `DataSteel` author.

Example response shape:

```json
{
  "content": [
    {
      "id": 1,
      "name": "DataSteel"
    }
  ]
}
```

Your exact response wrapper may differ based on the generated endpoint and runtime version.

## Common mistakes

| Mistake | Why it causes problems | Correct approach |
|---|---|---|
| Calling the wrong route | Generated paths depend on model and configuration. | Check the generated controller under `target/generated-sources/annotations`. |
| Starting without a database | JPA repositories cannot initialize. | Configure H2 or another local datasource. |
| Posting fields not present in the request DTO | The generated request contract rejects or ignores unknown data depending on Jackson settings. | Match the generated `AuthorRequestDto` fields. |

## Next step

Continue with [Associate Your First Entities](associate-your-first-entities.md).

## Related documentation

- [Generate Your First API](generate-your-first-api.md)
- [Associate Your First Entities](associate-your-first-entities.md)
- [Runtime Architecture](../architecture/runtime-architecture.md)
