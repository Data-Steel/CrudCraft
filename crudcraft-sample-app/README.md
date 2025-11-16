# CrudCraft Sample App

A demonstration Spring Boot application that showcases CrudCraft's generated CRUD endpoints using a simple blog domain.

## Domain Model

The sample app demonstrates a blog/content management system with the following entities:

- **Author** - Blog authors with name, email, and bio
- **Category** - Post categories for organization
- **Tag** - Tags for post classification (ManyToMany relationship)
- **Post** - Blog posts with title, content, and metadata (demonstrates soft deletes, auditing, and editable stubs)
- **PostStats** - View/like/share statistics (OneToOne relationship)
- **Comment** - User comments on posts (demonstrates validation)
- **User** - Application users with role-based security (demonstrates field-level security with hidden password hash)

## Features Demonstrated

- **Basic CRUD operations** - All entities have full CRUD endpoints
- **Relationships** - OneToMany, ManyToOne, OneToOne, and ManyToMany
- **Search and filtering** - @Searchable fields enable API filtering
- **Soft deletes** - Posts use soft delete functionality
- **Auditing** - Automatic creation/update timestamps
- **Security** - AdminOnly security policy on User endpoints, field-level security on password
- **Validation** - Bean validation on all entity fields
- **Editable stubs** - Post and User entities demonstrate customizable generated code

## Sample Data

The application automatically seeds the database with:
- 50 authors
- 18 categories
- 23 tags  
- 300 blog posts with statistics
- 500 comments
- 3 demo users (admin/password, editor/password, viewer/password)

Total: **850+ records** to thoroughly test pagination, search, and other features.

## Running the App

From the repository root, start the application with:
```bash
./mvnw spring-boot:run -pl crudcraft-sample-app -am
```

Or build and run the JAR:
```bash
./mvnw package -pl crudcraft-sample-app -am -DskipTests
java -jar crudcraft-sample-app/target/crudcraft-sample-app-1.0.1-SNAPSHOT.jar
```

The app starts on <http://localhost:8080> and exposes generated endpoints for the blog domain.

## Available Endpoints

Once running, explore the API documentation at:
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI spec: <http://localhost:8080/v3/api-docs>

Example endpoints:
- `GET /posts` - List all posts (with pagination, sorting, and search)
- `GET /authors` - List all authors
- `GET /comments?approved=true` - List approved comments
- `POST /auth/login` - Authenticate and get a JWT token

## Authentication

Use the login endpoint to get a JWT token:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Include the returned token in subsequent requests:
```bash
curl http://localhost:8080/users \
  -H "Authorization: Bearer <your-token>"
```

## Learn More
- [Getting Started guide](../guides/getting-started.md)
- [crudcraft.dev](https://crudcraft.dev)
