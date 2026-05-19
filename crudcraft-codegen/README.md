# crudcraft-codegen

## Module Purpose
Compile-time annotation processor that generates CRUD layers.

## Inbound and Outbound Dependencies
- Inbound: consuming apps via annotation processing.
- Outbound: `crudcraft-api`; test dependencies on runtime modules.

## Public Contracts
Processor behavior, generated code shapes, and writer extension points.

## What Breaks If Changed
Generated API signatures, endpoint semantics, and project compilation.

## Test Strategy
Compile-testing, writer unit tests, descriptor/reader tests, boundary tests.

## Javadoc Expectations

## Operational Contract

- Threading: annotation processors are created by javac and should keep per-round state local.
- Lifecycle: each round parses annotations into descriptors, validates cohesive annotation usage, then writes generated sources deterministically.
- Errors: validation failures are reported as compiler errors with model context; questionable but valid annotation combinations are compiler warnings.
- Configuration: see `docs/configuration-reference.md` for processor options.
- Extension points: writers, descriptor readers, and editable stubs are the supported customization boundaries.
Public generation contracts and extension points document invariants.

```mermaid
graph LR
  A[AnnotationModelReader] --> D[Descriptor]
  D --> W[WriterRegistry]
  W --> G[Generators]
  G --> O[Generated Sources]
```
