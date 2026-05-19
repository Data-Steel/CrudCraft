# crudcraft-runtime-search

## Module Purpose
Search request model and query support for runtime.

## Inbound and Outbound Dependencies
- Inbound: starter-search and codegen-generated search endpoints.
- Outbound: `crudcraft-runtime-core`.

## Public Contracts
`SearchRequest`, `SearchOperations`, `SearchLogic`.

## What Breaks If Changed
Search behavior and operator compatibility in generated APIs.

## Test Strategy
Unit tests for operators and request parsing.

## Javadoc Expectations

## Operational Contract

- Threading: `SearchOperations` is stateless and thread-safe. Generated search request classes are mutable `@NotThreadSafe` command objects for one request.
- Lifecycle: generated requests are created by Spring binding or user code and discarded after use.
- Errors: invalid criteria, operators, or sort paths fail with requested/allowed/docs context.
- Configuration: see `docs/configuration-reference.md` for search depth settings.
- Extension points: customize generated specifications or build programmatic requests with `SearchBuilder`.
Document operator semantics and validation.

```mermaid
graph LR
  R[SearchRequest] --> O[SearchOperations]
  O --> L[SearchLogic]
  L --> C[Core Query Strategy]
```
