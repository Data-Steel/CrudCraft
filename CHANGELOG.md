# Changelog

All notable changes to CrudCraft are documented in this file.

The format follows Keep a Changelog and the project follows semantic versioning as documented in
`STABILITY.md`.

## [Unreleased]

### Added

- Documented the generated-code contract for controllers, DTOs, mappers, repositories, services,
  search requests, projection metadata, and editable stubs.
- Added aggregate CycloneDX SBOM generation to the Maven reactor.
- Added release workflow upload for SBOM artifacts and Sigstore bundle artifacts.
- Added a reproducible-build workflow that compares two clean package builds.
- Added a runtime `SearchBuilder` for programmatic `SearchRequest` creation.

### Changed

- Extended the stability policy with supported platform versions, support windows, EOL rules, and
  2.0 deferral decisions for breaking public API changes.

### Security

- Release artifacts remain GPG signed. The release workflow now also signs release artifacts and
  SBOMs with Sigstore keyless signing.
