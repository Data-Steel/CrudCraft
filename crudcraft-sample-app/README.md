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

### Inheritance Demonstration

The sample app also demonstrates **abstract base classes with inheritance**:

- **Content** (abstract) - Base class for all content items with common fields (title, body, author, publishedAt, status)
  - CrudCraft skips generating stubs for abstract classes
  - Child classes inherit all parent fields in their DTOs
- **Article** (extends Content) - Adds subtitle, readingTimeMinutes, featured, allowComments
  - Generated DTOs include both Article fields AND all Content fields
  - Full CRUD endpoints generated
- **Tutorial** (extends Content) - Adds difficultyLevel, estimatedDurationMinutes, prerequisites, githubRepoUrl
  - Generated DTOs include both Tutorial fields AND all Content fields
  - Full CRUD endpoints generated

## Features Demonstrated

### Core Features
- **Basic CRUD operations** - All entities have endpoints for create, read, update, delete
- **Relationships** - OneToMany, ManyToOne, OneToOne, and ManyToMany
- **Search and filtering** - @Searchable fields enable API filtering and sorting
- **Soft deletes** - Posts use soft delete functionality (can be restored)
- **Auditing** - Automatic creation/update timestamps on all entities
- **Validation** - Bean validation constraints on all entity fields

### Advanced Features
- **Value DTOs** - List response DTO variants (not Map - List is more appropriate)
  - `AuthorListResponseDto` - List view with name and email only
  - `CategoryListResponseDto` - List view for categories
  - `PostListResponseDto` - List view with title and summary
  - `TagListResponseDto`, `CommentListResponseDto`, `PostStatsListResponseDto`, `UserListResponseDto`
  - `ArticleListResponseDto`, `TutorialListResponseDto` - List views for inherited entities
- **Projection support** - ProjectionField annotations for custom query paths
  - Author, Category, Tag, Post, Comment, PostStats, User, Article, Tutorial all have projection metadata
- **Inheritance with abstract classes** - Demonstrates CrudCraft's handling of class hierarchies
  - Abstract `Content` class marked @CrudCrafted (no stubs generated)
  - Concrete `Article` and `Tutorial` classes extend Content
  - Child class DTOs automatically include all parent fields (title, body, author, etc.)
  - Full CRUD endpoints generated for child classes only
- **CrudTemplate variations** - Different endpoint configurations per entity:
  - `Author` - FULL template (all endpoints enabled)
  - `Category` - READ_ONLY template (no create/update/delete)
  - `Tag` - IMMUTABLE_WRITE template (create only, no updates)
  - `PostStats` - PATCH_ONLY template (only partial updates allowed)
  - `Comment` - NO_DELETE template (can't be deleted for audit purposes)
  - `Post`, `Article`, `Tutorial`, `User` - Default template (full CRUD)
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
- 100 articles (extending Content base class)
- 50 tutorials (extending Content base class)
- 500 comments
- 3 demo users (admin/password, editor/password, viewer/password)

Total: **1000+ records** to thoroughly test pagination, search, inheritance, and other features.

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
