# Threading Model

Generated CrudCraft services are Spring singleton beans and must remain thread-safe.

## Thread-Safe Components

`AbstractCrudService`, `CoreCrudOperations`, keyset pagination, projection adapters, and runtime extension resolution are designed for concurrent request threads. Shared collaborators are constructor-injected or Spring-managed, and lazy metadata is published through volatile/synchronized paths.

## Not Thread-Safe Components

Generated request DTOs and generated search request classes are mutable command objects. Spring creates them per HTTP request through binding. Do not cache or reuse them between threads.

## Extension Rules

Custom service subclasses and `CrudRuntimeExtension` implementations should be stateless. If an extension needs per-request data, read it from Spring Security, request-scoped beans, or method arguments. Mutable counters, caches, and event buffers must use thread-safe structures or be request scoped.

## EntityManager Lifecycle

`CoreCrudOperations.setEntityManager` is synchronized because Spring injects the persistence metamodel during bean initialization. Replacing it later with a different metamodel is rejected to avoid stale cached ID metadata in tests and restarted contexts.
