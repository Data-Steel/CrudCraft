---
title: "Local Build"
description: "Run the final local CrudCraft verification commands before requesting review."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/contributor-handbook/running-tests"
  - "/contributor-handbook/review-checklist"
  - "/maintainer-handbook/quality-gates"
---

# Local Build

The local build is the final contributor check before review. It does not need to duplicate every CI matrix job, but it must give reviewers credible evidence that the change is complete.

## Default Final Verification

From the repository root:

```powershell
.\mvnw.cmd -B verify
```

On Unix-like shells:

```bash
./mvnw -B verify
```

Expected result: the full Maven reactor compiles, tests, packages, and verifies successfully.

## Docs Verification

Run when docs changed or public behavior changed:

```powershell
.\scripts\update-doc-index.ps1
.\scripts\check-doc-drift.ps1 -Staged
```

For docs-deploy script changes:

```powershell
node --check docs-deploy\scripts\prepare-content.mjs
cd docs-deploy
npm run build -- --source ..
```

Adjust the `node --check` command to the `.mjs` file you changed.

## Generated Output Verification

Run when codegen changed:

```powershell
.\mvnw.cmd -B -pl crudcraft-codegen -am -Dtest=GoldenTestRunner -Dsurefire.failIfNoSpecifiedTests=false test
```

If expected generated output changed, update golden fixtures only after reviewing the diff.

## What To Put In A PR

Use exact commands and outcomes:

```text
Verification:
- .\mvnw.cmd -B -pl crudcraft-runtime-security -am test passed.
- .\mvnw.cmd -B verify passed.
- .\scripts\update-doc-index.ps1 passed.
```

If a command was skipped, state why. “Not run” is acceptable when honest and scoped; silence is not.

## Related Documentation

- [Running Tests](running-tests.md)
- [Review Checklist](review-checklist.md)
- [Quality Gates](../maintainer-handbook/quality-gates.md)
