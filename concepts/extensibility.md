---
title: Extensibility & Hooks
summary: Extension points for custom endpoints, handlers, and future integrations.
sidebar: concepts
---

# Extensibility & Hooks

CrudCraft is designed with extension points so you can adapt it to your application's needs.

## Custom Endpoints

Editable controller stubs allow you to add new REST methods alongside generated ones:

```java
@RestController
public class BookController extends AbstractCrudController<...> {
  @GetMapping("/books/popular")
  public List<BookResponseDto> popular() { ... }
}
```

## Handlers and Policies

Implement interfaces such as `RowSecurityHandler`, `CrudSecurityPolicy`, or custom validators to plug into generation hooks. Register these beans in your application context.

## Custom Annotations

You can define your own annotations and extend the annotation processor via Java's `ServiceLoader`. Custom processors can add fields to DTOs or introduce new endpoint groups.

## Future Hooks

Planned areas for extension include:

- Projection or GraphQL generation
- Async or reactive controllers
- Alternative persistence models

## Next Steps

- Review [Editable Stubs & Customization](/guides/editable-stubs.md) for practical editing tips.
- Understand the generation pipeline in [Architecture & Modules](/concepts/architecture.md).
- Ensure security when extending by consulting [Security Design](/concepts/security-design.md).

