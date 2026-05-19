# Javadoc Standard

Public CrudCraft APIs must have class and method Javadoc. Internal package-private classes should document architectural intent when they define a module boundary, threading rule, or extension contract.

Every public class should state:

- Purpose.
- Threading expectation.
- Main failure modes.
- Extension points, if any.

Every public method should document parameters, return values, and checked/runtime exceptions that callers are expected to handle. Generated code may use concise standard comments when the contract is already documented in `docs/generated-code-contract.md`.
