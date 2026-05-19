# crudcraft-starter-security

## Module Purpose
Auto-configuration for the security runtime.

## Inbound and Outbound Dependencies
- Inbound: umbrella starter and applications.
- Outbound: `crudcraft-runtime-security`.

## Public Contracts
Starter auto-configuration imports.

## What Breaks If Changed
Security policies and handlers are not initialized.

## Test Strategy
Auto-config tests + sample security smoke tests.

## Javadoc Expectations
Config classes moeten security assumptions benoemen.

```mermaid
graph LR
  A[Starter Security] --> C[AutoConfig]
  C --> R[Runtime Security]
```
