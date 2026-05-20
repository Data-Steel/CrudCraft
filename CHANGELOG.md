# Changelog

All notable changes to CrudCraft are documented in this file.

The format follows Keep a Changelog and the project follows semantic versioning as documented in
`STABILITY.md`.

## [2.0.0](https://github.com/Data-Steel/CrudCraft/compare/v1.0.10...v2.0.0) (2026-05-20)


### ⚠ BREAKING CHANGES

* test release please correcty goes to

### Features

* test release please correcty goes to ([1dab11c](https://github.com/Data-Steel/CrudCraft/commit/1dab11c77264bbfa7083cd616e5b608c7bf707af))


### Bug Fixes

* add missing license headers ([f7b7cd7](https://github.com/Data-Steel/CrudCraft/commit/f7b7cd7651acba8c5452c3f5ce4f061cba76bc34))
* dependency, security and verify issues ([37462b6](https://github.com/Data-Steel/CrudCraft/commit/37462b65a0a783c5bfee51e80f3b092de42111ad))
* ensure test coverage is met ([c0fce54](https://github.com/Data-Steel/CrudCraft/commit/c0fce5495595bf3aa9c305143528634d88e89c58))
* first batch of issues ([46bec35](https://github.com/Data-Steel/CrudCraft/commit/46bec352745eead0e24af4c4aba3abb4fc3f6d47))
* golden tests and issues with generated code generating PMD errors ([8373d22](https://github.com/Data-Steel/CrudCraft/commit/8373d22e931b1f720b0389f69185278a010228cb))
* license headers present in generated code ([b152223](https://github.com/Data-Steel/CrudCraft/commit/b152223dfc7e36ea7aa90b28781e9701ddcaef72))
* Missing catch of NumberFormatException and more ([034830f](https://github.com/Data-Steel/CrudCraft/commit/034830f67039d16eb94beeff65854fdba4b34dc5))
* PIT testing failures solved ([f14177c](https://github.com/Data-Steel/CrudCraft/commit/f14177c3f591c3e15e8cacf28c0057eccf0e620c))
* remove dependency scanning in CI verify and adjust docs ([022c58c](https://github.com/Data-Steel/CrudCraft/commit/022c58c599cbb1fb1863149a8191674cab2fa5a5))
* Remove Useless parameters ([7b6b64d](https://github.com/Data-Steel/CrudCraft/commit/7b6b64dabd18624ea28096161b5dc46a6e73f10a))
* stateless requests return 401 ([1004562](https://github.com/Data-Steel/CrudCraft/commit/1004562c67c2c30cd8784acf215037fdc2ccc077))

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
