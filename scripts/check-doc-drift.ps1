param(
    [string]$BaseRef = "origin/main",
    [switch]$Staged
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if ($Staged) {
    $changedFiles = @(git diff --cached --name-only --diff-filter=ACMR)
} else {
    $changedFiles = @(git diff $BaseRef --name-only --diff-filter=ACMR)
}

if ($changedFiles.Count -eq 0) {
    Write-Host "No changed files."
    exit 0
}

$codeChanged = $changedFiles | Where-Object {
    $_ -match "^crudcraft-.+?/src/main/" -or $_ -match "^pom\.xml$"
}
$docsChanged = $changedFiles | Where-Object {
    $_ -match "^docs/" -or $_ -match "^README\.md$" -or $_ -match "^crudcraft-.+?/README\.md$"
}

if ($codeChanged.Count -gt 0 -and $docsChanged.Count -eq 0) {
    Write-Error "Doc drift gate failed: code changed without docs/governance updates."
}

Write-Host "Doc drift gate passed."
