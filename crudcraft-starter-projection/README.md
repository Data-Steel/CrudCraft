# crudcraft-starter-projection

## Module Purpose
Auto-configuration for the projection runtime.

## Inbound and Outbound Dependencies
- Inbound: umbrella starter and applications.
- Outbound: `crudcraft-runtime-projection`.

## Public Contracts
Starter auto-configuration imports.

## What Breaks If Changed
Projection runtime is not available in the Spring context.

## Test Strategy
Auto-configuration and projection context tests.

## Javadoc Expectations
Public configuration documents compatibility expectations.

```mermaid
graph LR
  A[Starter Projection] --> C[AutoConfig]
  C --> R[Runtime Projection]
```
