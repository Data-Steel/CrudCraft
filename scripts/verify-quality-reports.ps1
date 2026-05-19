param()

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$failures = New-Object System.Collections.Generic.List[string]

function Read-XmlFile {
    param([string]$Path)

    try {
        [xml](Get-Content -Raw $Path)
    } catch {
        $failures.Add("Cannot parse XML report: $Path")
        $null
    }
}

function Add-Failure {
    param(
        [string]$Tool,
        [string]$Path,
        [int]$Count
    )

    if ($Count -gt 0) {
        $relative = Resolve-Path -Relative $Path
        $failures.Add("$Tool reported $Count issue(s) in $relative")
    }
}

Get-ChildItem -Recurse -File -Path . -Filter checkstyle-result.xml | ForEach-Object {
    $xml = Read-XmlFile $_.FullName
    if ($null -ne $xml) {
        Add-Failure "Checkstyle" $_.FullName $xml.SelectNodes("//error[not(@severity) or @severity='error']").Count
    }
}

Get-ChildItem -Recurse -File -Path . -Filter pmd.xml | ForEach-Object {
    $xml = Read-XmlFile $_.FullName
    if ($null -ne $xml) {
        Add-Failure "PMD" $_.FullName $xml.SelectNodes("//violation").Count
    }
}

Get-ChildItem -Recurse -File -Path . | Where-Object {
    $_.Name -in @("spotbugsXml.xml", "spotbugs.xml")
} | ForEach-Object {
    $xml = Read-XmlFile $_.FullName
    if ($null -ne $xml) {
        Add-Failure "SpotBugs" $_.FullName $xml.SelectNodes("//BugInstance").Count
    }
}

Get-ChildItem -Recurse -File -Path . -Filter mutations.xml | ForEach-Object {
    $xml = Read-XmlFile $_.FullName
    if ($null -ne $xml) {
        $badMutations = $xml.SelectNodes("//mutation[@status='SURVIVED' or @status='NO_COVERAGE' or @status='TIMED_OUT' or @status='RUN_ERROR' or @status='NON_VIABLE' or @status='MEMORY_ERROR']")
        Add-Failure "PIT" $_.FullName $badMutations.Count
    }
}

$jacocoReports = @(Get-ChildItem -Recurse -File -Path . -Filter jacoco.xml)
if ($jacocoReports.Count -eq 0) {
    $failures.Add("JaCoCo did not produce any jacoco.xml reports")
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ -ErrorAction Continue }
    exit 1
}

Write-Host "Quality report verification passed."
