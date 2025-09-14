Write-Host "=== WladLwe9t Configuration Validation ===" -ForegroundColor Cyan

# Check if JAR exists
if (Test-Path "target/wladLwe9t-0.0.1-SNAPSHOT.jar") {
    Write-Host "JAR file exists" -ForegroundColor Green
    $jarSize = (Get-Item "target/wladLwe9t-0.0.1-SNAPSHOT.jar").Length / 1MB
    Write-Host "JAR size: $([math]::Round($jarSize, 1)) MB" -ForegroundColor Blue
} else {
    Write-Host "JAR file missing. Run: mvn clean package -DskipTests" -ForegroundColor Red
    exit 1
}

# Check Liquibase changelogs
if (Test-Path "src/main/resources/db/changelog/db.changelog-master.yaml") {
    Write-Host "Liquibase master changelog exists" -ForegroundColor Green
} else {
    Write-Host "Liquibase master changelog missing" -ForegroundColor Red
}

if (Test-Path "src/main/resources/db/changelog/changes/001-create-product-table.yaml") {
    Write-Host "Product table changelog exists" -ForegroundColor Green
} else {
    Write-Host "Product table changelog missing" -ForegroundColor Red
}

# Check configuration files
Write-Host ""
Write-Host "Configuration files:" -ForegroundColor Cyan
$configs = @("application.yml", "application-docker.yml", "application-test.yml")
foreach ($config in $configs) {
    if (Test-Path "src/main/resources/$config") {
        Write-Host "  $config - OK" -ForegroundColor Green
    } else {
        Write-Host "  $config - MISSING" -ForegroundColor Red
    }
}

# Check Docker files
Write-Host ""
Write-Host "Docker files:" -ForegroundColor Cyan
if (Test-Path "Dockerfile") {
    Write-Host "  Dockerfile - OK" -ForegroundColor Green
} else {
    Write-Host "  Dockerfile - MISSING" -ForegroundColor Red
}

if (Test-Path "docker-compose.yml") {
    Write-Host "  docker-compose.yml - OK" -ForegroundColor Green
} else {
    Write-Host "  docker-compose.yml - MISSING" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Validation Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "To test with Docker:" -ForegroundColor Yellow
Write-Host "   1. Start Docker Desktop" -ForegroundColor White
Write-Host "   2. Run: docker-compose up -d" -ForegroundColor White
Write-Host "   3. Test: curl http://localhost:8080/api/status" -ForegroundColor White
Write-Host "   4. Products: curl -u tintin:acrobate http://localhost:8080/api/produits" -ForegroundColor White