# Change Impact Matrix

This matrix maps changed modules to minimum validation scope.

| Changed module | Minimum required checks |
|---|---|
| `crudcraft-api` | Full reactor verify, codegen tests, sample compile |
| `crudcraft-codegen` | Codegen unit/golden tests, sample compile, integration tests |
| `crudcraft-runtime-core` | Runtime-core tests plus all dependent runtime module tests |
| `crudcraft-runtime-security` | Security runtime tests, sample security integration tests |
| `crudcraft-runtime-search` | Search runtime tests, generated search endpoint tests |
| `crudcraft-runtime-export` | Export runtime tests, sample export integration tests |
| `crudcraft-runtime-projection` | Projection runtime tests, projection integration tests |
| `crudcraft-runtime-observability` | Observability runtime tests and starter-observability tests |
| `crudcraft-starter-*` | Starter module tests and sample boot smoke test |
| `crudcraft-starter` | Full starter composition test and sample app boot |
| `crudcraft-sample-app` | Sample tests, TCK matrix, generated roundtrip PIT |
| `crudcraft-tools` | Governance tests and affected workflow checks |

Release PRs should include this matrix in reviewer notes when a change crosses module boundaries.
