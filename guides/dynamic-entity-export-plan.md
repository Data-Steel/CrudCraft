# Dynamic Entity-Based Export - Implementation Plan

## Problem Statement

The current export system is limited to Response DTO fields. Users need the ability to export ANY entity field dynamically, including:
- All relationship types and their fields without N+1 query problems
- Fields selected at runtime via API parameters

## Current Situation

**What Works Now:**
- Export of fields marked with `@Dto` annotation
- Field filtering within DTO structure (includeFields/excludeFields)
- Relationship depth control (maxDepth)
- Streaming for memory efficiency

**What's Missing:**
- Cannot export entity fields not in the DTO
- No dynamic field selection from entity
- Limited to DTO design decisions

**Example:**
```java
@Entity
public class Product {
    @Dto private String name;              // ✅ Can export
    @Dto private BigDecimal price;         // ✅ Can export
    
    private String internalSKU;            // ❌ Cannot export
    private BigDecimal costPrice;          // ❌ Cannot export
    private ConfigParams configuration;    // ❌ Cannot export
}
```

## Why This Requires Major Architecture Work

Entity-based dynamic export is not a simple enhancement. It requires:

### 1. Entity Metadata System
- Runtime introspection of entity structure
- Field name to property mapping
- Relationship type identification
- Security annotation processing

### 2. Dynamic Query Construction
- Build projections from requested fields
- Optimize to fetch only needed data
- Handle JOINs for ManyToOne relationships
- Maintain pagination

### 3. Relationship Hydration Strategy
- ManyToOne: Include with JOIN in main query
- OneToMany: Batch fetch to avoid N+1
- ManyToMany: Batch fetch to avoid N+1
- OneToOne: Include with JOIN in main query
- Nested relationships: author.address.city

### 4. Transaction Management
- Export happens in streaming context
- Lazy loading requires transaction scope
- Need session management strategy

### 5. Security at Entity Level
- Field-level security on entities
- Permission checks per field
- @ExportExclude enforcement
- Audit logging

## Implementation Estimate

**Phase 1: Foundation** (3-4 days)
- Entity metadata extraction
- Field validation
- Scalar field selection
- Basic tests

**Phase 2: Simple Relationships** (4-5 days)
- ManyToOne with JOINs
- OneToOne support
- Embedded objects
- Integration tests

**Phase 3: Collections** (5-6 days)
- OneToMany batch loading
- ManyToMany batch loading
- Hydration strategy
- Performance tests

**Phase 4: Advanced** (3-4 days)
- Nested paths
- Deep traversal
- Query optimization
- Caching

**Phase 5: Integration** (3-4 days)
- API integration
- Backward compatibility
- Migration guide
- Documentation

**Total: 18-23 days of development**

## Performance Requirements

Must maintain:
- **Query efficiency**: O(1+N) queries where N = collection relationship count
- **Memory**: Streaming, < 100MB regardless of export size  
- **Speed**: 10,000 records in < 10 seconds
- **No N+1**: Batch fetch all collections

## Recommended Approach

This should be a **separate feature/PR**, not part of current PR:

### Current PR Provides Foundation:
✅ ExportRequest structure
✅ includeFields/excludeFields parameters
✅ maxDepth for relationships
✅ @ExportExclude annotation
✅ Documentation framework

### Separate PR Would Add:
❌ Entity metadata introspection
❌ Dynamic query building
❌ Relationship hydration
❌ Transaction management
❌ Entity serialization

## Integration Strategy

**Option A: Mode Parameter**
```bash
# Current DTO-based (default)
GET /products/export?format=csv&includeFields=name

# New entity-based
GET /products/export?format=csv&mode=entity&includeFields=internalSKU
```

**Option B: Auto-Detection**
Automatically use entity mode when non-DTO field requested:
```bash
# Requests DTO field -> DTO mode
GET /products/export?format=csv&includeFields=name

# Requests non-DTO field -> Entity mode
GET /products/export?format=csv&includeFields=internalSKU
```

**Option C: Configuration**
```yaml
crudcraft:
  export:
    default-mode: entity  # or 'dto'
```

## Current PR Scope

This PR establishes the **foundation only**:
1. ExportRequest with field filtering
2. Depth control for relationships
3. @ExportExclude annotation
4. Documentation of limitations

**Out of scope** (needs separate PR):
- Entity metadata system
- Dynamic projection building
- Efficient relationship loading
- Entity-based serialization

## Next Steps

1. **Complete current PR** with foundation
2. **Create separate issue** for entity-based export
3. **Design review** on architecture approach
4. **Prototype** core functionality
5. **Performance benchmark** with real data
6. **Gradual rollout** with feature flag

## Open Questions for Team

1. Should entity export be opt-in or eventually default?
2. How to handle lazy loading during export streaming?
3. Transaction strategy for large exports?
4. How to validate field-level security on entities?
5. Should we support computed/derived fields?
6. Caching strategy for entity metadata?

## Conclusion

Entity-based dynamic field export is a valuable feature but requires significant architectural work (3-4 weeks effort). The current PR provides the API foundation. Full implementation should be:

- **Separate PR/issue**
- **Proper design review**
- **Performance benchmarking**
- **Gradual rollout with feature flag**
- **Comprehensive testing**

This ensures we maintain performance and don't introduce regressions while adding this powerful capability.
