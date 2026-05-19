param(
    [switch]$Fix
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$modules = Get-ChildItem -Directory | Where-Object { $_.Name -like "crudcraft-*" } | Sort-Object Name
$missingReadme = @()
$missingMermaid = @()

foreach ($module in $modules) {
    $readmePath = Join-Path $module.FullName "README.md"
    if (-not (Test-Path $readmePath)) {
        $missingReadme += $module.Name
        continue
    }
    $content = Get-Content -Raw $readmePath
    if ($content -notmatch "```mermaid") {
        $missingMermaid += $module.Name
    }
}

if ($missingReadme.Count -gt 0) {
    Write-Error "Missing module README.md: $($missingReadme -join ', ')"
}
if ($missingMermaid.Count -gt 0) {
    Write-Error "Missing Mermaid diagram in module README: $($missingMermaid -join ', ')"
}

$docsReadme = Join-Path $repoRoot "docs\README.md"
if (-not (Test-Path $docsReadme)) {
    Write-Error "Missing docs/README.md"
}

$docsContent = Get-Content -Raw $docsReadme
$requiredHandbookPages = @(
    "docs/contributor-handbook/README.md",
    "docs/contributor-handbook/development-setup.md",
    "docs/contributor-handbook/repository-structure.md",
    "docs/contributor-handbook/module-overview.md",
    "docs/contributor-handbook/running-tests.md",
    "docs/contributor-handbook/writing-tests.md",
    "docs/contributor-handbook/local-build.md",
    "docs/contributor-handbook/coding-standards.md",
    "docs/contributor-handbook/documentation-standards.md",
    "docs/contributor-handbook/pull-request-process.md",
    "docs/contributor-handbook/review-checklist.md",
    "docs/maintainer-handbook/README.md",
    "docs/maintainer-handbook/quality-gates.md",
    "docs/maintainer-handbook/ci-cd.md",
    "docs/maintainer-handbook/release-process.md",
    "docs/maintainer-handbook/compatibility-policy.md",
    "docs/maintainer-handbook/versioning-policy.md",
    "docs/maintainer-handbook/deprecation-policy.md",
    "docs/maintainer-handbook/dependency-management.md",
    "docs/maintainer-handbook/security-policy.md",
    "docs/maintainer-handbook/regression-handling.md",
    "docs/maintainer-handbook/documentation-review-policy.md"
)

foreach ($page in $requiredHandbookPages) {
    if (-not (Test-Path $page)) {
        Write-Error "Missing required handbook page: $page"
    }
}

$requiredRootLinks = @(
    "quick-start/",
    "feature-guides/",
    "architecture/",
    "contributor-handbook/",
    "maintainer-handbook/",
    "features.md"
)

foreach ($link in $requiredRootLinks) {
    if ($docsContent -notmatch [regex]::Escape($link)) {
        Write-Error "docs/README.md does not link: $link"
    }
}

Write-Host "Documentation index validation passed."
