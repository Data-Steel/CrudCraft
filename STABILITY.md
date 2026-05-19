# Stability Policy

CrudCraft follows semantic versioning for published Maven artifacts.

## Supported Platform Matrix

| Platform    | Supported line                                    | Policy                                                         |
|-------------|---------------------------------------------------|----------------------------------------------------------------|
| Java        | 21 LTS                                            | Primary build, test, and release target.                       |
| Spring Boot | 4.0.x                                             | Primary starter and sample-app target for CrudCraft 2.x.       |
| Maven       | 3.9.x and newer                                   | CI and release workflows use the Maven wrapper where possible. |
| Databases   | H2 for tests, JPA-compatible production databases | Runtime code stays database-neutral                            |

Java 23 is not an LTS target and is not used as a support baseline.

## Public API

The public API includes annotations in `crudcraft-api`, generated source contracts documented in
the guides and [generated-code contract](docs/generated-code-contract.md), runtime extension
interfaces, and starter module configuration properties. Runtime implementation classes are not
public API unless documented as extension points.

Published JARs expose stable JPMS automatic module names through `Automatic-Module-Name`. Those
names are part of the compatibility surface and are guarded by repository governance tests. Explicit
`module-info.java` descriptors are deferred until the documented 2.x package-boundary migration
removes current split packages; see [JPMS compatibility](docs/maintainers/jpms.md).

## Compatibility Gate

The release workflow reruns the same verification surface used for pull requests before deploying
artifacts: reactor `verify` with the PostgreSQL TCK required, golden generated-source drift checks,
license and Javadoc gates, quality-report verification, dependency scanning, and documentation
index validation. Public API and SPI packages are classified in the module-boundary documentation
and guarded by repository governance tests. A japicmp or Revapi binary/source comparison against
the last Maven Central release is required before declaring the 2.x line broadly stable.

## Breaking Changes

Breaking changes require a major version. A change is breaking when existing generated sources,
annotation usage, starter configuration, or documented runtime extension implementations must be
changed by consumers.

## Deprecation

CrudCraft 2.x does not keep obsolete public API shapes as deprecated aliases. The 2.x line publishes
one supported contract: behavior that is not intended to work in future 2.x releases must not be
supported in the current 2.x code either. Consumers adapt to the 2.x contract or stay on an older
line.

## Support and EOL

Each minor release line receives fixes until the next minor release has been available for at least
90 days. Security fixes may be backported farther when the affected line is still widely used and the
fix is low risk.

The latest published 2.x minor is the active support line and receives regular fixes. Older 2.x
minors remain supported only within the 90-day overlap window unless explicitly extended in release
notes.

End-of-life announcements must include:

1. the last planned patch version for the line,
2. the final support date,
3. the recommended upgrade target,
4. any known migration hazards.

## 2.x Completion Work

The following changes are part of the 2.x completion scope because they change generated or public
API shape and must be finished before the line is called stable:

- replacing generated DTO classes with Java records,
- redesigning `@CrudCrafted` into nested configuration annotations,
- splitting `AbstractCrudService` into smaller public base classes,
- changing generated DTO nullness defaults or controller method signatures.

These changes require characterization tests, migration guide updates, and generated-code contract
updates in the same change set.

## Security Fixes

Security fixes may tighten default behavior in minor or patch releases when the previous behavior
could expose data or bypass access control. Release notes must call out the behavioral change.

## Release Trust

Published releases must include:

1. Maven Central artifacts signed with GPG,
2. aggregate CycloneDX SBOM artifacts,
3. Sigstore keyless signatures or bundles for release artifacts and SBOMs,
4. a changelog entry,
5. a passing reproducible-build workflow or an explicit release-note exception.

## Performance Stability

CrudCraft makes bounded-behavior guarantees for generated APIs and runtime helpers:

1. request search depth is capped by generated metadata and `crudcraft.search.depth`,
2. export requests are capped by configured per-format row limits,
3. pageable endpoint requests are clamped to configured maximum page size,
4. keyset pagination uses deterministic sort/id tie-breaking for stable cursors,
5. row-security filters are applied as query predicates, not post-query list filtering.

These are behavioral guarantees, not absolute latency SLAs. Throughput and memory ceilings depend on
entity shape, selected feature modules, database plan quality, and caller-provided projection/search
paths.

## Release Rollback

When a release must be withdrawn (for example due to a security regression):

1. publish a GitHub Security Advisory or release advisory with affected versions,
2. cut a patch release on the latest safe supported line,
3. mark the withdrawn version as yanked in release notes/changelog,
4. update upgrade guidance with the exact fixed version.
