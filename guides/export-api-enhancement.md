# Export API Enhancement

This document describes the enhanced export functionality that allows fine-grained control over exported data.

## Overview

The export endpoint now accepts an `ExportRequest` parameter that allows you to:
- Control which fields are included or excluded from the export
- Limit the depth of nested relationships
- Prepare for future entity-level exports (not just DTO fields)

## ExportRequest Parameters

### includeFields (Set<String>)
Specify which fields to include in the export. Use dot notation for nested fields.

**Example:**
```json
{
  "includeFields": ["id", "name", "author.name", "author.email"]
}
```

If not specified or empty, all DTO fields are included by default.

### excludeFields (Set<String>)
Specify which fields to exclude from the export. Exclusions take precedence over inclusions.

**Example:**
```json
{
  "excludeFields": ["passwordHash", "author.email"]
}
```

### maxDepth (Integer)
Control the maximum depth for nested relationships. Default is 1.

**Values:**
- `0` - No nested relationships (only top-level fields)
- `1` - Immediate relationships only (default)
- `2+` - Deeper nesting levels

**Example:**
```json
{
  "maxDepth": 2
}
```

### includeAllFields (Boolean)
**Future capability** - When `true`, will export all accessible entity fields (not just DTO fields). Currently a placeholder for future implementation. Default is `false`.

## Usage Examples

### Basic Export (all fields)
```bash
GET /posts/export?format=csv&limit=1000
```

### Export with Field Selection
```bash
GET /posts/export?format=csv&limit=1000&includeFields=id&includeFields=title&includeFields=author.name
```

### Export Excluding Sensitive Fields
```bash
GET /posts/export?format=csv&limit=1000&excludeFields=passwordHash&excludeFields=author.email
```

### Export with Depth Limitation
```bash
GET /posts/export?format=json&limit=100&maxDepth=0
```
This exports only top-level fields without any nested objects.

### Complex Export Configuration
```bash
GET /posts/export?format=xlsx&limit=500&includeFields=title&includeFields=content&includeFields=author&excludeFields=author.email&maxDepth=1
```

## Query Parameter vs Request Body

The `ExportRequest` is passed as query parameters (via `@ModelAttribute`), which means:
- All parameters are optional
- Use repeated parameters for collections (e.g., `includeFields=id&includeFields=name`)
- Parameters work with standard HTTP GET requests

## Field Filtering Rules

1. **No filters specified**: All DTO fields are included
2. **Include only**: Only specified fields and their descendants are included
3. **Exclude only**: All fields except specified ones are included
4. **Both include and exclude**: Exclusions take precedence

### Examples

#### Include parent includes children
```
includeFields: ["author"]
Result: author.name, author.email, author.bio are all included
```

#### Exclude overrides include
```
includeFields: ["author"]
excludeFields: ["author.email"]
Result: author.name and author.bio are included, but author.email is excluded
```

## Nested Object Flattening

CSV and XLSX exports automatically flatten nested objects using dot notation:

**Input DTO:**
```json
{
  "id": "123",
  "title": "My Post",
  "author": {
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**CSV Output:**
```
id,title,author.name,author.email
123,My Post,John Doe,john@example.com
```

## Depth Control

The `maxDepth` parameter controls how deep the flattening goes:

### maxDepth = 0
```
id,title
123,My Post
```
No nested objects at all.

### maxDepth = 1 (default)
```
id,title,author.name,author.email
123,My Post,John Doe,john@example.com
```
One level of nesting.

### maxDepth = 2
```
id,title,author.name,author.email,author.address.city,author.address.country
123,My Post,John Doe,john@example.com,New York,USA
```
Two levels of nesting.

## Security Considerations

- Field security filtering is always applied before export
- Fields with `@FieldSecurity(readRoles = {})` are automatically excluded
- Export respects the same security rules as regular API responses
- The `ExportRequest` filtering is applied AFTER security filtering

## Future Enhancements

The `includeAllFields` flag is a placeholder for future functionality that will:
- Export all accessible entity fields, not just DTO fields
- Bypass the DTO layer for more complete exports
- Still respect security constraints

This is currently not implemented but the API structure is ready for it.

## Implementation Details

### Code Changes

1. **ExportRequest class** (`nl.datasteel.crudcraft.runtime.export.ExportRequest`)
   - Bean for capturing export configuration
   - Methods for field filtering logic

2. **ExportUtil updates** (`nl.datasteel.crudcraft.runtime.util.ExportUtil`)
   - Enhanced `flattenMap()` with depth and filter support
   - Overloaded stream methods accepting `ExportRequest`

3. **ExportService updates** (`nl.datasteel.crudcraft.runtime.service.ExportService`)
   - New `export()` overload accepting `ExportRequest`
   - Pass-through to appropriate exporters

4. **Code generation** (`nl.datasteel.crudcraft.codegen.writer.controller.endpoints.ExportEndpoint`)
   - Added `ExportRequest` as `@ModelAttribute` parameter
   - Generates export calls with `ExportRequest`

## Backward Compatibility

All changes are backward compatible:
- Existing export calls without `ExportRequest` parameters work unchanged
- Default behavior (export all DTO fields) remains the same
- New parameters are optional
