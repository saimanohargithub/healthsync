param (
    [Parameter(Mandatory=$false)]
    [string]$ProjectId = $env:FIREBASE_PROJECT_ID
)

if (-not $ProjectId) {
    Write-Host "Error: FIREBASE_PROJECT_ID is not set." -ForegroundColor Red
    Write-Host "Please provide it by setting the environment variable or passing it as a parameter:"
    Write-Host ".\run_load_test.ps1 -ProjectId 'your-firebase-project-id'"
    exit 1
}

# Check if k6 is installed
if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    Write-Host "k6 is not installed on this system." -ForegroundColor Yellow
    Write-Host "Please install it first: https://k6.io/docs/get-started/installation"
    Write-Host "On Windows, you can use: winget install k6"
    exit 1
}

Write-Host "Starting baseline load test with 100 users..." -ForegroundColor Cyan
$env:FIREBASE_PROJECT_ID = $ProjectId

k6 run .\k6_firebase_test.js
