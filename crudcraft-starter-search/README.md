# crudcraft-starter-search

## Module Purpose
Auto-configuration for the search runtime.

## Inbound and Outbound Dependencies
- Inbound: umbrella starter and applications.
- Outbound: `crudcraft-runtime-search`.

## Public Contracts
Starter auto-configuration imports.

## What Breaks If Changed
Search endpoints missen runtime ondersteuning.

## Test Strategy
Auto-configuration tests and search integration in the sample app.

## Javadoc Expectations
Documenteer search bean contracten.

```mermaid
graph LR
  A[Starter Search] --> C[AutoConfig]
  C --> R[Runtime Search]
```
