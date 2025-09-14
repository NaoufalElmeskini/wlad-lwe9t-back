Write-Host "=== Docker Diagnostic ===" -ForegroundColor Cyan

# Check Docker installation
Write-Host "1. Checking Docker installation..." -ForegroundColor Yellow
try {
    $dockerVersion = docker version --format "{{.Client.Version}}" 2>$null
    if ($dockerVersion) {
        Write-Host "   Docker client version: $dockerVersion" -ForegroundColor Green
    } else {
        Write-Host "   Docker client not found" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   Docker not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check Docker daemon
Write-Host "2. Checking Docker daemon..." -ForegroundColor Yellow
try {
    $dockerInfo = docker info --format "{{.ServerVersion}}" 2>$null
    if ($dockerInfo) {
        Write-Host "   Docker daemon running - version: $dockerInfo" -ForegroundColor Green
        $dockerRunning = $true
    } else {
        Write-Host "   Docker daemon not running" -ForegroundColor Red
        $dockerRunning = $false
    }
} catch {
    Write-Host "   Cannot connect to Docker daemon" -ForegroundColor Red
    $dockerRunning = $false
}

if (-not $dockerRunning) {
    Write-Host ""
    Write-Host "=== Docker Desktop is not running ===" -ForegroundColor Red
    Write-Host "Please start Docker Desktop manually:" -ForegroundColor Yellow
    Write-Host "1. Open Docker Desktop from Start menu" -ForegroundColor White
    Write-Host "2. Wait for it to fully start" -ForegroundColor White
    Write-Host "3. Re-run this script" -ForegroundColor White
    Write-Host ""
    Write-Host "Alternative: Test locally with PostgreSQL" -ForegroundColor Yellow
    Write-Host "1. Install PostgreSQL locally" -ForegroundColor White
    Write-Host "2. Create database 'wladlwe9t' with user 'wladlwe9t_user'" -ForegroundColor White
    Write-Host "3. Run: mvn spring-boot:run" -ForegroundColor White
    exit 1
}

# Check if containers exist
Write-Host "3. Checking existing containers..." -ForegroundColor Yellow
$containers = docker ps -a --format "table {{.Names}}\t{{.Status}}" 2>$null
if ($containers) {
    Write-Host "   Existing containers:" -ForegroundColor Blue
    Write-Host $containers
} else {
    Write-Host "   No containers found" -ForegroundColor Green
}

# Check if images exist
Write-Host "4. Checking Docker images..." -ForegroundColor Yellow
$images = docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" 2>$null | Select-String "wladlwe9t|postgres"
if ($images) {
    Write-Host "   Relevant images:" -ForegroundColor Blue
    Write-Host $images
} else {
    Write-Host "   No relevant images found" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Docker is ready! ===" -ForegroundColor Green
Write-Host "You can now run:" -ForegroundColor Yellow
Write-Host "   docker-compose up -d" -ForegroundColor White