---
title: Security Design
summary: Rationale behind CrudCraft's endpoint, field, and row security layers.
sidebar: concepts
---

# Security Design

CrudCraft separates security concerns across endpoint, field, and row layers. This design provides flexibility while keeping generated code straightforward.

## Endpoint Security

Endpoint policies wrap controller methods with `@PreAuthorize` expressions. They are coarse-grained and fast to evaluate. Use them to guard entire operations, such as restricting deletes to administrators.

### Trade-offs

- Simple to reason about.
- Cannot differentiate access to individual fields or rows.

## Field Security

Field rules apply within DTO mapping, allowing per-property restrictions and transformations. They protect sensitive data when multiple roles share the same endpoint.

### Trade-offs

- Adds processing overhead per field.
- Complex rules may be harder to test.

## Row Security

Row-level handlers supply query predicates that limit database results or validate writes. This layer enforces multi-tenancy and ownership rules.

### Trade-offs

- Predicates run on every query and can impact database performance.
- Misconfigured predicates may unintentionally hide or expose data.

## Composition Patterns

- Start with endpoint policies for broad access control.
- Add field security to mask or restrict sensitive attributes.
- Apply row security for tenant or ownership constraints.

## Failure Modes

- Overlapping rules can lead to `403` responses that are hard to trace; log decisions in custom handlers.
- Ignoring field or row security in tests may allow regressions; include security scenarios in your test suite.

## Next Steps

- Configure these layers in [Security Overview](/guides/security/overview.md).
- Implement custom security logic with [Extensibility & Hooks](/concepts/extensibility.md).
- Verify with tests from [Testing Generated APIs](/guides/testing.md).

