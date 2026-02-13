# Entity-Based Export - Usage Guide

## Overview

CrudCraft now supports **fully modular entity-based export** functionality that allows you to export ANY entity field dynamically at runtime, not just fields marked with `@Dto`. This feature provides:

- Dynamic field selection from entities at runtime
- Efficient relationship loading (avoiding N+1 queries)
- All relationship types supported (ManyToOne, OneToOne, OneToMany, ManyToMany)
- Nested field access via dot notation
- Field-level security with `@ExportExclude` annotation
- Streaming support for large datasets
- Multiple export formats (CSV, JSON, XLSX)

## Quick Start

### 1. Basic Entity Export

To export all fields from an entity in entity mode:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY
```

### 2. Field Selection

Export specific fields only:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.includeFields=title,author.name,author.email
```

### 3. Field Exclusion

Export all fields except specific ones:

```bash
GET /posts/export?format=json&exportRequest.exportMode=ENTITY&exportRequest.excludeFields=internalData,secretField
```

### 4. Relationship Depth Control

Control how deep relationships are traversed:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.maxDepth=2
```

## Export Modes

### DTO Mode (Default)

The traditional export mode that only exports fields marked with `@Dto` annotation:

```bash
GET /posts/export?format=csv
# or explicitly
GET /posts/export?format=csv&exportRequest.exportMode=DTO
```

**Use when:**
- You want backward compatibility
- DTO fields are sufficient
- You have existing integrations

### Entity Mode

The new dynamic mode that can export any entity field:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY
```

**Use when:**
- You need fields not in the DTO
- You want runtime field selection
- You need flexible export configuration

## Field Selection Syntax

### Include Fields

Specify exactly which fields to export:

```
includeFields=field1,field2,nested.field3
```

Example:
```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.includeFields=title,publishedAt,author.name
```

### Exclude Fields

Export all fields except specified ones:

```
excludeFields=field1,field2,nested.field3
```

Example:
```bash
GET /posts/export?format=json&exportRequest.exportMode=ENTITY&exportRequest.excludeFields=internalProcessingData,author.passwordHash
```

### Nested Fields

Use dot notation for nested relationships:

```
includeFields=author.name,author.email,category.name,tags.name
```

### Precedence Rules

1. `excludeFields` takes precedence over `includeFields`
2. If neither is specified, all exportable fields are included
3. `@ExportExclude` annotation is always respected

## Relationship Handling

### ManyToOne and OneToOne

These relationships are fetched using JOIN FETCH for optimal performance:

```java
@Entity
public class Post {
    @ManyToOne
    private Author author;  // Loaded with single query
    
    @OneToOne
    private PostStats stats;  // Loaded with single query
}
```

### OneToMany and ManyToMany

These relationships are batch-loaded to avoid N+1 queries:

```java
@Entity
public class Post {
    @OneToMany(mappedBy = "post")
    private Set<Comment> comments;  // Batch loaded
    
    @ManyToMany
    private Set<Tag> tags;  // Batch loaded
}
```

### Depth Control

Control traversal depth for nested relationships:

```bash
# Only direct relationships (default)
maxDepth=1

# Include nested relationships
maxDepth=2

# No relationships
maxDepth=0
```

## Security

### @ExportExclude Annotation

Mark fields that should never be exported:

```java
@Entity
public class User {
    private String name;  // ✅ Can export
    
    @ExportExclude
    private String passwordHash;  // ❌ Never exported
    
    @ExportExclude
    private String internalToken;  // ❌ Never exported
}
```

### Dynamic Exclusion

Use `excludeFields` parameter for runtime exclusion:

```bash
GET /users/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.excludeFields=email,phoneNumber
```

## Performance

### Query Optimization

Entity mode uses optimized query strategies:

1. **JOIN FETCH** for non-collection relationships
2. **Batch loading** for collections
3. **Streaming** for memory efficiency (format-dependent)
4. **Pagination** for large datasets

### Expected Performance

- **Query efficiency**: O(1+N) queries where N = collection relationship count
- **Memory**: Varies by format:
  - **JSON**: True streaming keeps memory usage low (typically < 100MB for large datasets)
  - **CSV/XLSX**: Current implementation buffers all rows to compute headers; memory usage is proportional to the number of rows and selected fields
- **Speed**: 10,000 records in < 10 seconds
- **No N+1**: All collections batch-fetched

### Limits

Default limits per format:
- CSV: 100,000 rows
- JSON: 50,000 rows
- XLSX: 25,000 rows

Override with `limit` parameter:
```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&limit=50000
```

## Examples

### Example 1: Basic Entity Export

Export all exportable fields:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY
```

Result includes all non-excluded entity fields, not just DTO fields.

### Example 2: Selective Export

Export only specific fields:

```bash
GET /posts/export?format=json&exportRequest.exportMode=ENTITY&exportRequest.includeFields=id,title,author.name,author.email,category.name
```

Result JSON:
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "My Blog Post",
    "author": {
      "name": "John Doe",
      "email": "john@example.com"
    },
    "category": {
      "name": "Technology"
    }
  }
]
```

### Example 3: Exclude Sensitive Data

Export everything except sensitive fields:

```bash
GET /users/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.excludeFields=passwordHash,apiToken,internalNotes
```

### Example 4: Deep Relationship Export

Export with nested relationships:

```bash
GET /posts/export?format=json&exportRequest.exportMode=ENTITY&exportRequest.includeFields=title,author.name,author.address.city&exportRequest.maxDepth=3
```

### Example 5: Collection Export

Export with collections:

```bash
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.includeFields=title,tags.name,comments.content
```

## Migration from DTO Mode

### Step 1: Identify Required Fields

Determine which non-DTO fields you need:

```java
@Entity
public class Product {
    @Dto private String name;          // Already exportable in DTO mode
    private String internalSKU;         // NEW: Now exportable in entity mode
    private BigDecimal costPrice;       // NEW: Now exportable in entity mode
}
```

### Step 2: Update Export Calls

Change from:
```bash
GET /products/export?format=csv
```

To:
```bash
GET /products/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.includeFields=name,internalSKU,costPrice
```

### Step 3: Test and Validate

1. Verify field selection works correctly
2. Check relationship loading performance
3. Validate exported data accuracy

## Configuration

### Spring Boot Properties

Configure export behavior in `application.yml`:

```yaml
crudcraft:
  export:
    max-csv-rows: 100000
    max-json-rows: 50000
    max-xlsx-rows: 25000
  api:
    max-page-size: 100
```

### Bean Configuration

The entity export infrastructure is provided via Spring beans. Ensure `EntityExportConfiguration` is imported or component-scanned in your application context. You can customize it:

```java
@Configuration
public class CustomExportConfig {
    
    @Bean
    public EntityMetadataRegistry entityMetadataRegistry() {
        EntityMetadataRegistry registry = new EntityMetadataRegistry();
        // Custom initialization if needed
        return registry;
    }
}
```

## Troubleshooting

### Issue: "Entity export mode is not available"

**Cause:** EntityExportAdapter not configured

**Solution:** Ensure JPA is on classpath and `EntityExportConfiguration` is imported or component-scanned in your Spring context.

### Issue: Lazy loading exceptions

**Cause:** Parts of the export (such as serialization) run outside the per-page database transactions, so lazily loaded associations may be accessed after the transaction is closed.

**Solution:** Export endpoints are not wrapped in a single transaction by default. If you rely on lazy-loaded associations during export, either:
- Annotate your own controller/service method that invokes the export with `@Transactional`, or
- Adjust your mapping/fetch strategy (e.g. use DTOs or eager fetching) so all required data is loaded within the transactional page-fetch phase.

### Issue: Slow export for large datasets

**Cause:** Deep relationships or inefficient queries

**Solutions:**
1. Limit maxDepth
2. Use field selection to reduce data
3. Increase page size (within limits)
4. Add database indexes

### Issue: Fields not appearing in export

**Possible causes:**
1. Field marked with `@ExportExclude`
2. Field in `excludeFields` list
3. Field not in `includeFields` list (when provided)
4. Transient field

**Solution:** Check field annotations and export request parameters

## API Reference

### ExportRequest Parameters

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `exportMode` | ExportMode | DTO or ENTITY | DTO |
| `includeFields` | Set<String> | Fields to include | All |
| `excludeFields` | Set<String> | Fields to exclude | None |
| `maxDepth` | Integer | Relationship depth | 1 |

### Export Endpoints

Generated for each `@CrudCrafted` entity:

```
GET /{entity}/export?format={format}&exportRequest.{params}
```

### Response Headers

- `Content-Type`: Format-specific MIME type
- `Content-Disposition`: Attachment filename with timestamp

## Best Practices

### 1. Use Field Selection

Always specify fields when possible to reduce data transfer:

```bash
# Good
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY&exportRequest.includeFields=id,title,author.name

# Less efficient (exports everything)
GET /posts/export?format=csv&exportRequest.exportMode=ENTITY
```

### 2. Limit Relationship Depth

Don't traverse deeper than needed:

```bash
# Good for most cases
maxDepth=1

# Only when needed
maxDepth=2
```

### 3. Use Appropriate Formats

- **CSV**: Best for flat data, large volumes
- **JSON**: Best for nested data, API integration
- **XLSX**: Best for end-user consumption in Excel

### 4. Annotate Sensitive Fields

Always use `@ExportExclude` for sensitive data:

```java
@ExportExclude
private String passwordHash;

@ExportExclude
private String apiSecret;
```

### 5. Test Performance

Always test export performance with realistic data volumes before production.

## Support

For issues or feature requests, visit:
https://github.com/Data-Steel/CrudCraft/issues
