---
title: "Development Setup"
description: "Set up a local CrudCraft development environment that matches the Java, Maven, docs, and script assumptions used by CI."
section: "Contributor Handbook"
audience:
  - "Contributors"
status: "stable"
related:
  - "/contributor-handbook/local-build"
  - "/contributor-handbook/running-tests"
  - "/contributor-handbook/repository-structure"
---

# Development Setup

CrudCraft is a Java 21 multi-module Maven project with PowerShell helper scripts and a VitePress documentation build. Set up the environment once from the repository root before relying on focused module commands.

## Required Tools

| Tool | Required because |
|---|---|
| Java 21 | The root Maven compiler configuration targets Java 21 and CI uses Temurin 21. |
| Git | CI/doc drift scripts compare changed files. |
| Maven wrapper | Contributors should use `mvnw`/`mvnw.cmd` so local Maven version drift does not affect review. |
| PowerShell | Repository helper scripts under `scripts/` are PowerShell scripts. |
| Node 20.19+ | Needed when building `docs-deploy` locally. |

## First Checks

Check Java:

```powershell
java -version
```

Expected: major version `21`.

Check Maven wrapper:

```powershell
.\mvnw.cmd -version
```

Expected: Maven runs through the wrapper and reports Java 21.

On Unix-like shells, use:

```bash
./mvnw -version
```

## First Compile

Run one clean compile from the root:

```powershell
.\mvnw.cmd -B clean compile
```

This proves the reactor can resolve dependencies and compile all modules on your machine.

## Docs Setup

Only needed when changing docs or docs deploy tooling:

```powershell
cd docs-deploy
npm install
npm run build -- --source ..
```

The docs source of truth is always `docs/`, not generated VitePress output under `docs-deploy/site`.

## Common Mistakes

| Mistake | Why it hurts review | Correct approach |
|---|---|---|
| Building with Java 17 | CI uses Java 21 and source uses Java 21 assumptions. | Fix `JAVA_HOME` and `PATH` before running Maven. |
| Using system Maven by habit | Local Maven config may differ from wrapper expectations. | Use `.\mvnw.cmd` or `./mvnw`. |
| Editing generated docs output | The next docs build overwrites it. | Edit files under `docs/`. |
| Starting with only one module test | Environment issues stay hidden. | Run root compile once after setup. |

## Related Documentation

- [Local Build](local-build.md)
- [Running Tests](running-tests.md)
- [Repository Structure](repository-structure.md)
