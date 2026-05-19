---
title: "Error Model"
description: "Understand CrudCraft compile-time diagnostics, runtime exceptions, HTTP status mapping, validation failures, and security errors."
section: "Architecture"
audience:
  - "Advanced users"
  - "Contributors"
  - "Maintainers"
status: "stable"
related:
  - "/architecture/runtime-architecture"
  - "/architecture/contract-model"
  - "/feature-guides"
---

# Error Model

CrudCraft errors belong to one of two phases:

- compile-time diagnostics from annotation processors;
- runtime failures from generated controllers, services, runtime modules, Spring MVC, Spring Security, validation, and persistence.

Detect errors at compile time when the source model or annotation contract is invalid. Use runtime exceptions when the failure depends on request data, current principal, database state, or optional Spring beans.

## Compile-Time Errors

`CrudCraftProcessor` catches generation failures per processable element and reports a compiler error like:

```text
CrudCraftProcessor failed for com.example.Book while generating IllegalStateException: ...
```

Generation should fail during compilation for:

- uninstantiable endpoint policies;
- uninstantiable security policies;
- unsupported annotation combinations that can be detected from source;
- invalid descriptor state such as missing model name/package/fields;
- generated source conflicts detected by the Java compiler.

Compile-time diagnostics should be attached to the entity or field that caused the problem whenever possible.

## Runtime Error Boundary

Generated controllers should not manually invent different error response styles per endpoint. Known exceptions should flow to `CrudCraftExceptionHandler`, the shared `@RestControllerAdvice` from runtime-core.

Runtime flow:

```text
bad request / missing entity / security denial / runtime failure
  -> generated controller or runtime module throws known exception
  -> CrudCraftExceptionHandler
  -> ErrorResponse with status, error, message, timestamp, path
```

`ErrorResponse` is the shared response body for single-error responses.

## Status Mapping

`CrudCraftExceptionHandler` maps these errors:

| Status | Exceptions |
|---|---|
| `400 Bad Request` | `BadRequestException`, MVC validation/type/body errors, missing request parameters/parts, `PropertyReferenceException`, transaction failures caused by validation. |
| `401 Unauthorized` | `UnauthorizedException`. |
| `403 Forbidden` | `ForbiddenException`; Spring Security access-denied exceptions when the security starter is installed. |
| `404 Not Found` | `ResourceNotFoundException`. |
| `405 Method Not Allowed` | `OperationNotAllowedException`, unsupported HTTP method. |
| `409 Conflict` | `DuplicateResourceException`, `DataIntegrityException`. |
| `412 Precondition Failed` | `PreconditionFailedException`. |
| `415 Unsupported Media Type` | `HttpMediaTypeNotSupportedException`. |
| `429 Too Many Requests` | `TooManyRequestsException`. |
| `501 Not Implemented` | `NotImplementedException`. |
| `413 Content Too Large` | `ExportLimitExceededException`. |
| `207 Multi-Status` | Legacy `BulkOperationException` paths only, with one `ErrorResponse` per failed item. |
| `500 Internal Server Error` | Other `CrudCraftRuntimeException` and unexpected exceptions. |

Unexpected exceptions return the generic message `An unexpected server error occurred.` to avoid leaking internals.

## Bulk Operation Recovery

Bulk operations should use the `BulkResult<T>` response envelope. Successful items are returned in
`succeeded`; failed items are returned in `failed` with the zero-based input index and an actionable
message. Successful entries have completed by the time the response is returned, but bulk operations
are not atomic unless the application adds a stronger transaction policy. Validation errors, missing
visible rows, row-security denials, and conflict errors are recoverable for the batch: the client can
inspect `failed`, fix or omit those indexed inputs, and retry only those failed inputs.

Legacy generated paths that still throw `BulkOperationException` are mapped to `207 Multi-Status`
with one `ErrorResponse` per failed item. New generated APIs should return `BulkResult<T>` because
it does not require clients to infer successes from the absence of an error.

Fatal failures are infrastructure or contract failures that make the whole request unreliable, such
as malformed JSON, unsupported media types, unavailable persistence infrastructure, or unexpected
runtime exceptions. Those failures should use the normal single-error status mapping instead of a
partial-success response.

## Where Common Errors Come From

| Error source | Example | Expected behavior |
|---|---|---|
| Generated controller | Invalid body, invalid path variable, unsupported multipart request. | Spring MVC exception becomes `400` or `415`. |
| `AbstractCrudService` | Entity not visible or absent for id. | `ResourceNotFoundException` becomes `404`. |
| Search runtime | Unsupported search field/operator/sort. | `BadRequestException` becomes `400`. |
| Core keyset pagination | Missing deterministic sort, multiple sort fields, invalid cursor. | `BadRequestException` becomes `400`. |
| Export runtime | Unsupported format or negative limit. | Generated export path returns `400` directly. |
| Security runtime | Row write guard or field write policy denies mutation. | Security-specific exception should map to `401`/`403`/`400` depending on contract. |
| Persistence | Duplicate or integrity issue translated by runtime/application code. | Known conflict exceptions become `409`. |

## Design Rules

- Prefer compile-time failure when the invalid state is visible to annotation processing.
- Prefer `BadRequestException` for client-controlled invalid runtime input.
- Prefer `ResourceNotFoundException` when the row is absent or hidden by row security.
- Preserve Spring Security denial semantics; do not convert authorization failures to `404` unless a feature explicitly documents that behavior.
- Do not leak protected field values, internal stack traces, or SQL details in generated API errors.
- Bulk failures should keep per-item context through `BulkResult<T>.failed[index]`; legacy
  `BulkOperationException` handling exists only for older generated code.

## Related Documentation

- [Runtime Architecture](runtime-architecture.md)
- [Contract Model](contract-model.md)
- [Security Model](security-model.md)
