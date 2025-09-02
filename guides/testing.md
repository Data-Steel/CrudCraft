---
title: Testing Generated APIs
summary: Write focused tests for controllers, services, mappers, validation, and security rules.
sidebar: guides
---

# Testing Generated APIs

Generated code is still your code. Well-placed tests ensure your customizations and security rules work as expected. This guide presents common testing patterns for CrudCraft projects.

## Controller Tests

Use MockMvc or WebTestClient to test endpoints without starting a full server:

```java
@WebMvcTest(BookController.class)
class BookControllerTest {
  @Autowired MockMvc mvc;

  @Test
  void listReturnsOk() throws Exception {
    mvc.perform(get("/books")).andExpect(status().isOk());
  }
}
```

## Service Tests

Slice tests verify business logic in generated services:

```java
@SpringBootTest
class BookServiceTest {
  @Autowired BookService service;

  @Test
  void createsBook() {
    BookRequestDto dto = new BookRequestDto();
    dto.setTitle("Cloud Native Java");
    BookResponseDto saved = service.create(dto);
    assertNotNull(saved.getId());
  }
}
```

## Mapper Tests

MapStruct mappers can be tested directly:

```java
@MapperTest(BookMapper.class)
class BookMapperTest {
  @Autowired BookMapper mapper;
}
```

## Validation Tests

Use `Validator` or MockMvc to assert constraint violations:

```java
mvc.perform(post("/books").content("{}")
  .contentType(MediaType.APPLICATION_JSON))
  .andExpect(status().isBadRequest());
```

## Security Tests

Ensure unauthorized users are rejected:

```java
mvc.perform(get("/books")).andExpect(status().isForbidden());
```

Row and field security can be tested using custom Authentication tokens or preconfigured user details.

## Test Data Setup

Use `@Sql` scripts, Testcontainers, or repository calls to prepare data. Keep fixtures small to speed up tests.

## Next Steps

- Plan for performance using [Performance & Tuning](/guides/performance.md).
- Review [Security Overview](/guides/security/overview.md) for testing guidance.
- Learn about [Regeneration & Migrations](/guides/migration.md) when evolving entities.

