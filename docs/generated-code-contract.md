# Generated Code Contract

Generated CrudCraft code is deterministic and follows these compatibility rules.

## Service Methods

Generated services extend `AbstractCrudService`. Create, update, patch, delete, bulk, search, keyset, projection, count, exists, and reference methods are transactional at the service layer. `findById` throws `ResourceNotFoundException`; `findByIdOptional` returns an optional; page and list methods never return null.

## Controller Validation

Generated controllers place `@Valid` on request bodies where Bean Validation applies. Spring MVC translates validation failures to HTTP 400 through the CrudCraft exception handler.

## Bulk Error Contract

Bulk endpoints return a `BulkResult<T>` envelope. Fully successful create/upsert batches return the
normal success status for that operation; update, patch, and delete batches return `200 OK`. When a
batch has both successes and recoverable item failures, CrudCraft returns HTTP `207 Multi-Status`.
The response body contains `succeeded` items and `failed` entries with zero-based input indexes.
Successful items remain persisted unless the application adds a stronger transaction policy.

## DTO Nullness

Request field nullness is controlled by Jakarta validation. PATCH treats nullable fields as caller intent according to mapper behavior; PUT replaces the entity state with the provided request values. Response fields may be null because of database values, projections, or field-level security.

## Repository Contract

Generated repositories extend Spring Data JPA repositories. CrudCraft does not guarantee custom finder methods unless a feature explicitly generated them.

## Mapper Contract

Generated mappers implement `EntityMapper<T, U, R, F, ID>`. Users may customize mapping through generated editable mapper stubs and MapStruct extension mechanisms. The runtime assumes mapper methods throw `MapperException` or another runtime exception when mapping fails; services wrap failures with operation/entity/request context.
