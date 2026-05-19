param(
    [switch]$Staged
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$diffArgs = if ($Staged) { @("--cached") } else { @() }
$changedFiles = @(git diff @diffArgs --name-only --diff-filter=ACMR)

if ($changedFiles.Count -eq 0) {
    Write-Host "No changed files detected."
    exit 0
}

$moduleMap = @{}
Get-ChildItem -Directory | Where-Object { $_.Name -like "crudcraft-*" } | ForEach-Object {
    $moduleMap[$_.Name] = $_.Name
}

$touchedModules = New-Object System.Collections.Generic.HashSet[string]
foreach ($file in $changedFiles) {
    foreach ($moduleName in $moduleMap.Keys) {
        if ($file.StartsWith("$moduleName/") -or $file.StartsWith("$moduleName\")) {
            [void]$touchedModules.Add($moduleName)
        }
    }
}

if ($touchedModules.Count -eq 0) {
    Write-Host "No module source changes detected. Running root verify."
    & .\mvnw -B verify
    exit $LASTEXITCODE
}

$moduleList = ($touchedModules | Sort-Object) -join ","
Write-Host "Touched modules: $moduleList"

& .\mvnw -B -pl $moduleList -am checkstyle:check pmd:check test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& .\mvnw -B -pl $moduleList -am verify
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Quality loop passed for: $moduleList"
