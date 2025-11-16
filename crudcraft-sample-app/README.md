# CrudCraft Sample App

A demonstration Spring Boot application that showcases CrudCraft's generated CRUD endpoints using a simple blog domain.

## Domain Model

The sample app demonstrates a blog/content management system with the following entities:

- **Author** - Blog authors with name, email, and bio (FULL template - all endpoints)
- **Category** - Post categories for organization (READ_ONLY template - no modifications)
- **Tag** - Tags for post classification (IMMUTABLE_WRITE template - create only, ManyToMany relationship)
- **Post** - Blog posts with title, content, and metadata (demonstrates soft deletes, auditing, editable stubs, and all relationship types)
- **PostStats** - View/like/share statistics (PATCH_ONLY template - OneToOne relationship)
- **Comment** - User comments on posts (NO_DELETE template - validation, ManyToOne)
- **User** - Application users with role-based security (demonstrates field-level security with hidden password hash)

## Features Demonstrated

### Core Features
- **Basic CRUD operations** - All entities have endpoints for create, read, update, delete
- **Relationships** - OneToMany, ManyToOne, OneToOne, and ManyToMany
- **Search and filtering** - @Searchable fields enable API filtering and sorting
- **Soft deletes** - Posts use soft delete functionality (can be restored)
- **Auditing** - Automatic creation/update timestamps on all entities
- **Validation** - Bean validation constraints on all entity fields

### Advanced Features
- **Value DTOs** - List and Map response DTO variants
  - `AuthorListResponseDto` - List view with name and email only
  - `CategoryListResponseDto` and `CategoryMapResponseDto` - Both List and Map variants
  - `PostListResponseDto` - List view with title and summary
  - `TagListResponseDto`, `CommentListResponseDto`, `PostStatsListResponseDto`, `UserListResponseDto`
- **Projection support** - ProjectionField annotations for custom query paths
  - Author, Category, Tag, Post, Comment, PostStats, and User all have projection metadata
- **CrudTemplate variations** - Different endpoint configurations per entity:
  - `Author` - FULL template (all endpoints enabled)
  - `Category` - READ_ONLY template (no create/update/delete)
  - `Tag` - IMMUTABLE_WRITE template (create only, no updates)
  - `PostStats` - PATCH_ONLY template (only partial updates allowed)
  - `Comment` - NO_DELETE template (can't be deleted for audit purposes)
  - `Post` - Default template (full CRUD)
  - `User` - Default template with AdminOnly security
- **Security policies** - AdminOnlySecurityPolicy on User endpoints
- **Field-level security** - User password hash is write-only (never returned in responses)
- **Editable stubs** - Post and User demonstrate customizable generated code
- **Bulk operations** - Bulk create, update, patch, upsert, and delete on applicable entities
- **Export functionality** - Available on entities with SEARCH endpoints

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
