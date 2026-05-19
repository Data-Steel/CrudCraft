---
title: "Release Process"
description: "Prepare, publish, verify, sign, and document CrudCraft releases from approved GitHub release automation."
section: "Maintainer Handbook"
audience:
  - "Maintainers"
status: "stable"
related:
  - "/maintainer-handbook/versioning-policy"
  - "/maintainer-handbook/compatibility-policy"
  - "/maintainer-handbook/documentation-review-policy"
---

# Release Process

CrudCraft releases are published from GitHub automation, not local machines. The release workflow sets the Maven version from the tag, runs tests, deploys signed artifacts to Maven Central, verifies publication, signs release artifacts with Sigstore, uploads SBOM/signature bundles, and dispatches docs deployment.

## Release Preconditions

- Release scope is frozen.
- Version impact is decided.
- Compatibility and deprecation impact are documented.
- Required PR checks passed on the release source.
- Docs build from the release source.
- Release notes describe user-visible changes, generated API changes, runtime module changes, compatibility impact, and known limitations.
- Maven Central, signing, and docs dispatch secrets are available.

## Normal Flow

1. Use Release Please on `main` when it represents the intended release scope.
2. Review the generated release PR for version, changelog/release notes, and compatibility language.
3. Merge the release PR only after CI is green.
4. Publish the GitHub release or use the manual cut-release workflow only when intentional.
5. Let `.github/workflows/release.yml` publish from the release tag.
6. Verify Maven Central artifacts for every published artifact listed by the workflow.
7. Verify SBOM and Sigstore bundles are attached to the GitHub release.
8. Verify docs deploy was dispatched with the release tag and SHA.

## Manual Cut Release

`.github/workflows/cut-release.yml` creates an annotated `vX.Y.Z` tag and GitHub release from a chosen ref. Use it only when maintainers have already approved the target ref and SemVer version.

The workflow validates:

- version format `X.Y.Z`;
- tag does not already exist;
- release is created with a PAT so `release.published` triggers the publish workflow.

## Release Workflow Artifacts

The Maven Central verification currently expects these artifacts:

- `crudcraft-api`;
- `crudcraft-runtime-core`;
- `crudcraft-runtime-search`;
- `crudcraft-runtime-export`;
- `crudcraft-runtime-extensions`;
- `crudcraft-runtime-projection`;
- `crudcraft-runtime-security`;
- `crudcraft-codegen`;
- all `crudcraft-spring-boot-starter-*` artifacts;
- umbrella `crudcraft-spring-boot-starter`;
- `crudcraft-tools`.

`crudcraft-sample-app` is intentionally excluded from deployment.

## Related Documentation

- [Versioning Policy](versioning-policy.md)
- [Compatibility Policy](compatibility-policy.md)
- [Documentation Review Policy](documentation-review-policy.md)
