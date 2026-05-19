# crudcraft-starter-extensions

## Module Purpose
Auto-configuration for runtime extensions.

## Inbound and Outbound Dependencies
- Inbound: umbrella starter and applications.
- Outbound: `crudcraft-runtime-extensions`.

## Public Contracts
Starter auto-configuration imports.

## What Breaks If Changed
Extensions are not initialized.

## Test Strategy
Context auto-config tests.

## Javadoc Expectations
Document extension enablement and ordering.

```mermaid
graph LR
  A[Starter Extensions] --> C[AutoConfig]
  C --> R[Runtime Extensions]
```
