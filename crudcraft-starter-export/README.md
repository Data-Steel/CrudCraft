# crudcraft-starter-export

## Module Purpose
Auto-configuration for export functionality.

## Inbound and Outbound Dependencies
- Inbound: umbrella starter and applications.
- Outbound: `crudcraft-runtime-export`.

## Public Contracts
Starter auto-configuration imports.

## What Breaks If Changed
Export beans are missing or configured incorrectly.

## Test Strategy
Context load and auto-configuration tests.

## Javadoc Expectations
Configuration classes document required dependencies.

```mermaid
graph LR
  A[Starter Export] --> C[AutoConfig]
  C --> R[Runtime Export]
```
