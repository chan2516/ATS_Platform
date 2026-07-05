param(
    [ValidateSet('all', 'backend', 'frontend')]
    [string]$Part = 'all'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

try {
    Write-Host 'Rebuilding Docker services...'
    if ($Part -eq 'all') {
        docker compose up -d --build postgres redis backend frontend
    }
    elseif ($Part -eq 'backend') {
        docker compose up -d --build backend
    }
    elseif ($Part -eq 'frontend') {
        docker compose up -d --build frontend
    }

    Write-Host "Docker rebuild complete for: $Part"
}
finally {
    Set-Location $root
}