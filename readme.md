
# WladLwe9t REST API

Simple Spring Boot 3.3.5 REST API application demonstrating basic web service architecture.

## Project Structure

This is a minimal Spring Boot application with:
- **StatusController**: Health check endpoint (`/status`)
- **Java 21** and **Spring Boot 3.3.5**
- **Basic testing** with integration tests

## Running the Application

### IDE Development (Recommended)
Run [WladLwe9tApplication.java](src/main/java/io/lacrobate/wladLwe9t/WladLwe9tApplication.java)

Application available at: `http://localhost:8080`

### Docker
```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t wladlwe9t .

# Run container
docker run -p 127.0.0.1:8080:8080 --name wladlwe9t wladlwe9t
```

## API Endpoints

- `GET /status` - Health check endpoint

Base URL: `http://localhost:8080`

## Development Commands

```bash
# Run tests
mvn test

# Package application
mvn clean package
```

## Technologies

- **Java 21**
- **Spring Boot 3.3.5** (Web starter)
- **Spring Boot Test** for testing
- **Lombok** for code generation

## Future Enhancements

- Add Swagger documentation
- Implement hexagonal architecture
- Add Spring Actuator for observability
- Add WireMock for testing