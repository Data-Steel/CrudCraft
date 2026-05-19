---
title: "Writing Tests"
description: "Add CrudCraft tests at the layer where the contract is promised: API, codegen, runtime, starter, sample app, docs, or tooling."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/architecture/testing-architecture"
  - "/contributor-handbook/running-tests"
  - "/contributor-handbook/review-checklist"
---

# Writing Tests

A useful CrudCraft test proves the contract that changed. It should fail without the implementation and pass for the reason the PR claims.

## Test At The Contract Layer

| Contract changed | Add or update |
|---|---|
| Annotation defaults, retention, enum values, policy interfaces | API tests or compile usage tests. |
| Descriptor extraction | Codegen reader/extractor tests. |
| Generated source shape | Compile-testing, source assertions, or golden fixtures. |
| Generated endpoint behavior | Codegen test plus sample app HTTP/integration test when runtime behavior is observable. |
| Runtime service behavior | Runtime module unit/integration tests. |
| Search validation | `SearchOperations` tests plus generated search tests when request shape changes. |
| Projection execution | Projection runtime tests plus generated metadata tests. |
| Export streaming | Runtime export tests and generated `/export` integration when endpoint shape changes. |
| Security behavior | Denied-path and allowed-path tests for endpoint, row, and field rules. |
| Starter wiring | Auto-configuration/dependency composition tests. |
| Docs/tooling | Script or build checks that fail on drift. |

## Golden Tests

Golden files are appropriate when the generated source shape is the contract. They are not a replacement for runtime tests.

Before updating golden output:

- inspect every changed generated file;
- identify which annotation or writer change caused it;
- confirm route, DTO, security, metadata, and import changes are intentional;
- update docs if the generated public contract changed.

## Security And Failure Paths

CrudCraft features often fail at boundaries. Include negative tests when the behavior touches:

- endpoint authorization;
- field write policies;
- tenant/client/owner row scopes;
- unsupported search fields or operators;
- invalid sort paths;
- export format or limit validation;
- not-found behavior after row filters;
- bulk partial failures.

## Sample App Tests

Use `crudcraft-sample-app` when generated code and runtime modules must work together. Do not use it as the only proof for library logic that belongs in a runtime or codegen module.

## Related Documentation

- [Testing Architecture](../architecture/testing-architecture.md)
- [Running Tests](running-tests.md)
- [Review Checklist](review-checklist.md)
