# Maintainer Release Process

## 1. Prepare

1. Confirm `CHANGELOG.md` and release notes are complete.
2. Confirm `mvn -B verify` and `mvn -B verify -Pmutation` pass locally.
3. Confirm no open release-blocking findings remain.

## 2. Cut the release

1. Create/publish the GitHub release tag (`v<version>`).
2. Let `.github/workflows/release.yml` run to completion.
3. Confirm Maven Central verification succeeds for all public artifacts.

## 3. Verify trust artifacts

1. Confirm GPG signatures are present for published jars and poms.
2. Confirm CycloneDX SBOM artifacts are uploaded.
3. Confirm Sigstore bundles are uploaded.

## 4. Post-release

1. Announce the release with key upgrade notes.
2. Update active support windows in `STABILITY.md` if needed.
3. Open follow-up issues for deferred work.
