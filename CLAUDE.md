# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.3.5 REST API application (`wladLwe9t`) that demonstrates a microservices architecture pattern. The application fetches product data from an internal repository and enriches it with pricing information from an external price API service.

## Architecture

The application follows a simple layered architecture:

- **Controllers**: Handle HTTP requests (`ProductController`, `StatusController`)
- **Services**: Business logic layer (`PriceClient` for external API communication)  
- **Repositories**: Data access layer (`SimpleProductRepository` with in-memory HashMap storage)
- **Models**: Domain objects (`Product`, `Price`)

**Key Integration**: The `ProductController` combines data from two sources:
1. Local product data from `SimpleProductRepository`
2. External pricing data via `PriceClient` calling `http://price-api:8082/api2`

## Development Commands

### Running the Application

**IDE Development** (recommended for development):
- Run `src/main/java/io/lacrobate/wladLwe9t/WladLwe9tApplication.java`
- Application runs on `http://localhost:8080`

**Docker Compose** (recommended for full stack):
```bash
docker compose up -d
```
- Requires `price-api` service in `../api2/` directory
- Runs both wladlwe9t-service (port 8080) and price-service (port 8082)

**Manual Docker**:
```bash
mvn clean package
docker build -t wladlwe9t .
docker network create api-network
docker run -p 127.0.0.1:8080:8080 --network=api-network --name wladlwe9t wladlwe9t
```

### Testing

```bash
# Run tests
mvn test

# Package application
mvn clean package
```

## API Endpoints

- `GET /status` - Health check endpoint
- `GET /product/{id}` - Get product with pricing (requires price-api to be running)

Base URL: `http://localhost:8080/rest-api`

## Dependencies & Technologies

- **Java 21**
- **Spring Boot 3.3.5** with Spring Web
- **OpenTelemetry** for distributed tracing
- **Lombok** for code generation
- **RestTemplate** for HTTP client communication

## External Dependencies

This service depends on a separate `price-api` service that should be running on port 8082. The `PriceClient` expects the price service to be available at `http://price-api:8082/api2`.

## Testing Standards and Best Practices

### Testing Philosophy
This project should follow comprehensive testing standards with both unit and integration tests:

**Unit Tests**:
- Test individual components in isolation
- Mock external dependencies (services, repositories, clients)
- Focus on business logic and edge cases
- Use `@WebMvcTest` for controller unit tests
- Use `@DataJpaTest` for repository tests (when applicable)

**Integration Tests**:
- Test complete request/response flows
- Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
- Test actual HTTP endpoints with `TestRestTemplate`
- Verify service integration points
- Example: `StatusControllerIntegrationTest` tests the `/status` endpoint

### Testing Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=StatusControllerIntegrationTest

# Run tests with coverage
mvn test jacoco:report
```

### Documentation Standards
The project should implement:

**API Documentation**:
- **Swagger/OpenAPI**: Add springdoc-openapi dependency for interactive API docs
- **README**: Comprehensive setup and usage instructions
- **Test as Specification**: Tests should serve as living documentation of API behavior

**Code Documentation**:
- Javadoc for public APIs and complex business logic
- Clear naming conventions for classes and methods
- Architecture documentation explaining service interactions