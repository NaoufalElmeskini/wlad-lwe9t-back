# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.3.5 REST API application (`wladLwe9t`) that demonstrates basic web service architecture. Currently a minimal application with a health check endpoint.

## Architecture

### Hexagonal Architecture (Ports and Adapters)

**MANDATORY**: This application MUST follow **Hexagonal Architecture** principles to ensure clean separation of concerns, testability, and maintainability:

#### Core Domain (Business Logic)
- **Domain Models**: Pure business entities with no framework dependencies
- **Domain Services**: Business logic and rules
- **Ports**: Abstract interfaces defining contracts for external interactions
  - Primary Ports: Interfaces for driving adapters (APIs, controllers)
  - Secondary Ports: Interfaces for driven adapters (repositories, external services)

#### Primary Adapters (Driving Side - Inbound)
- **REST Controllers**: HTTP API adapters (e.g., `StatusController`)
- **Event Listeners**: Message queue or event adapters
- **CLI Interfaces**: Command-line adapters

#### Secondary Adapters (Driven Side - Outbound)
- **Repositories**: Data persistence adapters
- **External Service Clients**: Third-party API adapters
- **Message Publishers**: Event/message queue adapters
- **File System**: File I/O adapters

#### Package Structure
```
src/main/java/io/lacrobate/wladLwe9t/
├── domain/           # Core business logic (no dependencies)
│   ├── model/        # Domain entities
│   ├── service/      # Domain services
│   └── port/         # Interfaces (contracts)
├── infrastructure/   # Secondary adapters
│   ├── repository/   # Data persistence implementations
│   ├── client/       # External service clients
│   └── messaging/    # Message queue implementations
├── application/      # Primary adapters
│   ├── rest/         # REST controllers
│   ├── cli/          # Command-line interfaces
│   └── config/       # Application configuration
└── WladLwe9tApplication.java
```

#### Architecture Benefits
- **Testability**: Core logic isolated from external dependencies
- **Flexibility**: Easy to swap implementations (in-memory → database)
- **Security**: Clear boundaries for essential data validation
- **Maintainability**: Clear separation of concerns
- **Business Focus**: Domain logic free from technical concerns

**Current State**: The application provides a basic health check endpoint at `/status` and needs refactoring to follow hexagonal architecture.

## Security Guidelines

### Pragmatic Security Approach

**MANDATORY**: Apply **pragmatic security** focusing on major risks while avoiding unnecessary complexity:

#### Core Security Principles
- **Simple is Secure**: Favor simple, well-understood solutions over complex ones
- **Major Risks First**: Focus on high-impact vulnerabilities (injection, authentication, authorization)
- **Progressive Enhancement**: Start with basic protections, add layers as needed
- **Fail Secure**: Default to deny access, validate all inputs at entry points

#### Essential Security Controls
1. **Input Validation**: Validate at controller level (primary adapters)
   - Use Spring's `@Valid` and Bean Validation annotations
   - Sanitize user inputs to prevent injection attacks

2. **Authentication & Authorization**:
   - Implement when handling sensitive data or operations
   - Use Spring Security for standard patterns

3. **Error Handling**:
   - Never expose internal system details in error messages
   - Log security events for monitoring

#### What NOT to Over-Engineer
- Avoid complex custom security frameworks
- Don't implement cryptography from scratch
- Skip security theater (complex patterns with no real benefit)
- Don't add security layers without clear threat model

**Security Documentation**: Document WHY security decisions were made, not just HOW they work.

#### Integration Testing Security Guidelines

**MANDATORY SECURITY RULES FOR TESTING**:

1. **NEVER hardcode credentials in test code** - Security vulnerability via source control exposure
2. **Use `@WithMockUser` for authentication testing** - Mock security context without real credentials
3. **Separate test security configuration** - Create test-specific security configs when needed
4. **Environment variables ONLY** - If real credentials absolutely required (rare cases)
5. **Test security behaviors, not actual credentials** - Focus on authorization logic, not credential validation

**Secure Testing Patterns**:

**Unit Tests with MockMvc**:
```java
// ✅ SECURE: Mock authentication without credentials
@Test
@WithMockUser(username = "testuser", roles = "USER")
void shouldAccessProtectedEndpoint() { ... }
```

**Integration Tests with TestRestTemplate**:
```java
// ✅ SECURE: Use configured credentials for integration testing
@Autowired
private RestTemplateBuilder restTemplateBuilder;

private TestRestTemplate authenticatedRestTemplate() {
    return new TestRestTemplate(restTemplateBuilder)
            .withBasicAuth("tintin", "acrobate"); // Test environment credentials only
}

// ✅ SECURE: Test unauthorized access
@Test
void shouldReturnUnauthorizedWhenNoAuthentication() {
    ResponseEntity<Object> response = restTemplate.getForEntity("/api/endpoint", Object.class);
    assertEquals(401, response.getStatusCodeValue());
}

// ❌ INSECURE: Hardcoded production credentials
restTemplate.withBasicAuth("admin", "prod-password")
```

**Test Security Architecture**:
- Unit tests: Use `@WithMockUser` for mocked security context
- Integration tests: Use test environment credentials in isolated methods
- Authorization tests verify role-based access control
- Unauthenticated tests verify proper 401 responses
- Test credentials separate from production credentials

## Development Commands

### Running the Application

**IDE Development** (recommended for development):
- Run `src/main/java/io/lacrobate/wladLwe9t/WladLwe9tApplication.java`
- Application runs on `http://localhost:8080`

**Docker**:
```bash
mvn clean package
docker build -t wladlwe9t .
docker run -p 127.0.0.1:8080:8080 --name wladlwe9t wladlwe9t
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

Base URL: `http://localhost:8080`

## Dependencies & Technologies

- **Java 21**
- **Spring Boot 3.3.5** with Spring Web
- **Lombok** for code generation
- **Spring Boot Test** for testing

## Testing Standards and Best Practices

### Tests as Specifications Philosophy

**MANDATORY**: Tests MUST serve as **living specifications** that document system behavior, business rules, and architectural decisions.

#### Test-Driven Development (TDD)
- **Write tests FIRST** before implementation
- Tests define **expected behavior** and serve as specifications
- **Red-Green-Refactor** cycle: failing test → minimal implementation → refactor
- Domain logic tests should be **independent of frameworks**

#### Test Categories and Specifications

**Domain Tests** (Core Business Logic):
- Test pure domain entities and business rules
- No Spring annotations or framework dependencies
- Focus on business behavior and edge cases
- Example: `ProductTest` specifies product validation rules

**Unit Tests** (Component Specifications):
- Test individual components in isolation using mocks
- Specify component contracts and behavior
- Use `@WebMvcTest` for controller specifications
- Use `@DataJpaTest` for repository contract specifications
- Example: `StatusControllerTest` specifies HTTP contract

**Integration Tests** (System Specifications):
- Test complete user scenarios and system behavior
- Use `@SpringBootTest` for full system specifications
- Test actual HTTP endpoints with real infrastructure
- Example: `StatusControllerIntegrationTest` specifies system health check

**Contract Tests** (API Specifications):
- Document and verify API contracts between services
- Use tools like WireMock for external service contracts
- Specify input/output formats and error conditions

#### Test Documentation Requirements
- **Test names** must describe **business scenarios**, not implementation
  - ✅ `shouldReturnAcceptedStatusWhenSystemIsHealthy()`
  - ❌ `testStatus()`
- **Given-When-Then** structure in test methods
- **Arrange-Act-Assert** pattern for clarity
- Comments explaining **business context** when complex

#### Testing Commands
```bash
# Run all tests (specifications)
mvn test

# Run specific test specifications
mvn test -Dtest=StatusControllerIntegrationTest

# Run tests with coverage analysis
mvn test jacoco:report

# Run domain tests only
mvn test -Dtest="**/*Test.java"

# Run integration specifications only  
mvn test -Dtest="**/*IntegrationTest.java"
```

### Documentation Standards
The project should implement:

**API Documentation**:
- **Swagger/OpenAPI**: MANDATORY for all REST controllers - document endpoints with business context
- **README**: Comprehensive setup and usage instructions
- **Test as Specification**: Tests should serve as living documentation of API behavior

**Code Documentation**:
- Javadoc for public APIs and complex business logic
- Clear naming conventions for classes and methods
- Architecture documentation explaining service interactions

**Architectural Decision Documentation**:
- **Concepts over Implementation**: Document WHY and WHAT (business purpose) over HOW (technical details)
- **Major Decisions**: Record significant architectural choices in commit messages and code
- **Business Context**: Explain business drivers behind technical decisions
- **Trade-offs**: Document what was considered and why specific approach was chosen

## Commit Message Standards

### Commit Message Format
Use clear, functional commit messages that focus on business value and architectural decisions:

**Good Examples**:
- `Add user authentication with JWT security layer`
- `Implement product catalog with pagination for performance`
- `Refactor payment service for PCI compliance`
- `Add inventory management with concurrent access protection`

**Structure**:
```
Functional summary: business impact

- Architecture decision: explain why this approach
- Security consideration: mention security implications
- Testability: how this improves testing
- Performance/Scalability: impact on system performance
```

**Commit Message Rules**:
1. **Focus on functionality** over implementation details
2. **Always mention architectural decisions** and their rationale
3. **Highlight security implications** of changes
4. **Explain testability improvements** or testing approach
5. **Avoid generic technical jargon** - focus on business value
6. **Never mention AI tools, code generation, Claude, or IA** in commit messages
7. **Keep first line under 50 characters** for better readability, include emoji
8. **Add change category (feat, bugfix, refacto...)**, example: 'feat: Adding security management'...  