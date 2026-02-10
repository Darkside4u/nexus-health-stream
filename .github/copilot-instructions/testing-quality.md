# Testing & Quality

[← Back to Main Instructions](../copilot-instructions.md)

## Testing Strategy

This project uses a **two-tier testing approach**:

1. **Unit Tests** - Test individual units of code in isolation with mocked dependencies
2. **Component Tests** - Test the entire service in isolation using Cucumber and Testcontainers

### Coverage Target: 90%

This project maintains a **90% code coverage requirement** measured by Jacoco. All new code must meet or exceed this threshold.

### What to Cover

- ✅ All service layer methods (unit tests)
- ✅ All business logic and validation rules (unit tests)
- ✅ Edge cases and error conditions (unit tests)
- ✅ Custom exceptions and handlers (unit tests)
- ✅ End-to-end user journeys (component tests)
- ✅ Integration with external boundaries (component tests)
- ⚠️ Configuration classes (when they contain logic)
- ❌ Simple DTOs/POJOs with only getters/setters (Lombok-generated)
- ❌ Application.java main method
- ❌ Controllers (tested via component tests, not unit tests)

## Unit Testing Best Practices

### Purpose of Unit Tests

Unit tests verify **individual units of code in isolation** by mocking all dependencies. In this project:

- **Focus on service layer** - Most unit tests are written for service classes
- **Controllers have no logic** - Controllers should be thin and delegate to services
- **Mock all dependencies** - Use Mockito to mock repositories, external services, and other collaborators
- **Fast execution** - Unit tests should run quickly without external dependencies

### Test Structure

Follow the **Arrange-Act-Assert (AAA)** pattern:

```java
@Test
void shouldReturnEntityWhenIdExists() {
    // Arrange
    String entityId = "ENT123";
    Entity expectedEntity = Entity.builder()
            .id(entityId)
            .name("Test Entity")
            .status("ACTIVE")
            .build();
    when(repository.findById(entityId))
            .thenReturn(Optional.of(expectedEntity));
    
    // Act
    Entity result = service.getEntityById(entityId);
    
    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(entityId);
    assertThat(result.getName()).isEqualTo("Test Entity");
}
```

### Testing Framework Stack

**Unit Testing:**
```
JUnit 5 (Jupiter)      - Test framework
Mockito               - Mocking framework
AssertJ               - Fluent assertions
```

**Component Testing:**
```
Cucumber              - BDD framework for behavior specification
Testcontainers        - Container-based integration testing
WireMock              - HTTP service mocking for external APIs
Spring Test           - Spring testing support
RestAssured           - REST API testing
```

### Service Layer Testing

```java
@ExtendWith(MockitoExtension.class)
class EntityServiceTest {
    
    @Mock
    private EntityRepository repository;
    
    @Mock
    private ValidationService validationService;
    
    @InjectMocks
    private EntityService service;
    
    @Test
    void shouldCreateEntitySuccessfully() {
        // Arrange
        EntityRequest request = createValidRequest();
        Entity entity = createEntity();
        when(repository.save(any(Entity.class)))
                .thenReturn(entity);
        
        // Act
        Entity result = service.createEntity(request);
        
        // Assert
        assertThat(result).isNotNull();
        verify(validationService).validate(request);
        verify(repository).save(any(Entity.class));
    }
    
    @Test
    void shouldThrowExceptionWhenEntityNotFound() {
        // Arrange
        String entityId = "INVALID";
        when(repository.findById(entityId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> service.getEntityById(entityId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(entityId);
    }
}
```

## Component Testing with Cucumber and Testcontainers

### Purpose of Component Tests

Component tests verify **the entire service in isolation** by testing complete user journeys through the actual API. In this project:

- **Test real behavior** - Use actual controllers, services, and business logic
- **Mock external boundaries** - Use Testcontainers and WireMock for databases, external APIs, and message queues
- **BDD with Cucumber** - Write tests in Gherkin format for better collaboration
- **Contract verification** - Ensure the service behaves correctly when boundary contracts are satisfied
- **Realistic environment** - Tests run against actual HTTP endpoints with real request/response cycles

### Component Test Structure

Component tests use **Cucumber** for BDD-style scenarios and **Testcontainers** for infrastructure:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWireMock
public class ComponentTestBase {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0")
            .withExposedPorts(27017);
    
    @Container
    static GenericContainer<?> azureStorageContainer = new GenericContainer<>("mcr.microsoft.com/azure-storage/azurite")
            .withExposedPorts(10000);
    
    @LocalServerPort
    protected int port;
    
    protected RequestSpecification requestSpec;
    
    @BeforeEach
    void setUp() {
        requestSpec = RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("azure.storage.connection-string", 
            () -> String.format("DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=...;BlobEndpoint=http://%s:%d/devstoreaccount1;",
                azureStorageContainer.getHost(),
                azureStorageContainer.getFirstMappedPort()));
    }
}
```

### Cucumber Feature Files

Write test scenarios in Gherkin syntax:

```gherkin
Feature: Entity Management
  As a system user
  I want to manage entity information
  So that I can perform business operations

  Scenario: Create a new entity
    Given I have valid entity information
      | name        | description      | status | category  |
      | Test Entity | Test Description | ACTIVE | TYPE_A    |
    When I submit a request to create a new entity
    Then the entity is created successfully
    And the response contains the entity ID
    And the entity can be retrieved by ID

  Scenario: Retrieve existing entity
    Given an entity exists with ID "ENT123"
    When I request entity details for "ENT123"
    Then I receive the entity information
    And the entity name is "Test Entity"

  Scenario: Handle entity not found
    Given no entity exists with ID "INVALID"
    When I request entity details for "INVALID"
    Then I receive a 404 Not Found response
    And the error message indicates entity not found

  Scenario: Validate entity creation with invalid data
    Given I have invalid entity information with missing required fields
    When I submit a request to create a new entity
    Then I receive a 400 Bad Request response
    And the error message lists validation failures
```

### Cucumber Step Definitions

Implement the steps using RestAssured:

```java
@CucumberSpringConfiguration
public class EntityStepDefinitions extends ComponentTestBase {
    
    private Response response;
    private String entityId;
    private Map<String, String> entityData;
    
    @Given("I have valid entity information")
    public void iHaveValidEntityInformation(DataTable dataTable) {
        entityData = dataTable.asMaps().get(0);
    }
    
    @When("I submit a request to create a new entity")
    public void iSubmitRequestToCreateEntity() {
        response = requestSpec
                .body(entityData)
                .when()
                .post("/api/v1/entities");
        
        if (response.statusCode() == 201) {
            entityId = response.jsonPath().getString("id");
        }
    }
    
    @Then("the entity is created successfully")
    public void theEntityIsCreatedSuccessfully() {
        assertThat(response.statusCode()).isEqualTo(201);
    }
    
    @Then("the response contains the entity ID")
    public void theResponseContainsEntityId() {
        assertThat(entityId).isNotNull().isNotEmpty();
    }
    
    @Then("the entity can be retrieved by ID")
    public void theEntityCanBeRetrievedById() {
        Response getResponse = requestSpec
                .when()
                .get("/api/v1/entities/{id}", entityId);
        
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.jsonPath().getString("id")).isEqualTo(entityId);
    }
    
    @Given("an entity exists with ID {string}")
    public void anEntityExistsWithId(String id) {
        // Setup test data in database via repository or API
        entityId = id;
        Map<String, String> testEntity = Map.of(
                "name", "Test Entity",
                "description", "Test Description",
                "status", "ACTIVE"
        );
        
        requestSpec.body(testEntity).post("/api/v1/entities");
    }
    
    @When("I request entity details for {string}")
    public void iRequestEntityDetailsFor(String id) {
        response = requestSpec
                .when()
                .get("/api/v1/entities/{id}", id);
    }
    
    @Then("I receive the entity information")
    public void iReceiveTheEntityInformation() {
        assertThat(response.statusCode()).isEqualTo(200);
    }
    
    @Then("the entity name is {string}")
    public void theEntityNameIs(String expectedName) {
        String name = response.jsonPath().getString("name");
        assertThat(name).isEqualTo(expectedName);
    }
    
    @Then("I receive a {int} Not Found response")
    public void iReceiveNotFoundResponse(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }
    
    @Then("the error message indicates entity not found")
    public void theErrorMessageIndicatesEntityNotFound() {
        String message = response.jsonPath().getString("message");
        assertThat(message).containsIgnoringCase("entity not found");
    }
}
```

### Mocking External Services with WireMock

Use WireMock to mock external HTTP services:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class ExternalServiceComponentTest extends ComponentTestBase {
    
    @Value("${wiremock.server.port}")
    private int wireMockPort;
    
    @BeforeEach
    void setupWireMock() {
        // Mock external verification service
        stubFor(post(urlEqualTo("/api/external/verify"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "verified": true,
                                "referenceNumber": "REF123456",
                                "status": "APPROVED"
                            }
                            """)));
        
        // Mock external data service
        stubFor(get(urlPathMatching("/api/data/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "entityId": "ENT123",
                                "records": [],
                                "metadata": {}
                            }
                            """)));
    }
    
    @Test
    void shouldVerifyUsingExternalService() {
        // Arrange
        String entityData = """
            {
                "name": "Test Entity",
                "description": "Test Description",
                "referenceNumber": "REF123456"
            }
            """;
        
        // Act
        Response response = requestSpec
                .body(entityData)
                .post("/api/v1/entities/verify");
        
        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("verified")).isTrue();
        
        // Verify WireMock received the request
        verify(postRequestedFor(urlEqualTo("/api/external/verify"))
                .withRequestBody(containing("REF123456")));
    }
}
```

### Using Testcontainers for Databases

```java
@Testcontainers
public class DatabaseComponentTest extends ComponentTestBase {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0")
            .withExposedPorts(27017);
    
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure MongoDB
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        
        // Configure PostgreSQL (if used)
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
    
    @Test
    void shouldPersistEntityToDatabase() {
        // Create entity via API
        String entityJson = """
            {
                "name": "Test Entity",
                "description": "Test Description",
                "status": "ACTIVE"
            }
            """;
        
        Response createResponse = requestSpec
                .body(entityJson)
                .post("/api/v1/entities");
        
        String entityId = createResponse.jsonPath().getString("id");
        
        // Verify persistence by retrieving
        Response getResponse = requestSpec
                .get("/api/v1/entities/{id}", entityId);
        
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.jsonPath().getString("name")).isEqualTo("Test Entity");
    }
}
```

### Component Test Best Practices

**✅ Do:**
- Write scenarios from user/business perspective
- Test complete user journeys, not just individual endpoints
- Use Testcontainers for all infrastructure dependencies (databases, message queues, etc.)
- Use WireMock for external HTTP services
- Keep scenarios focused and independent
- Clean up test data between scenarios
- Use meaningful scenario and step names

**❌ Don't:**
- Test framework internals or Spring Boot auto-configuration
- Share state between scenarios
- Use real external services (always mock boundaries)
- Write overly complex scenarios (break them down)
- Skip component tests because they're "slow" (they're essential)
- Test implementation details (focus on behavior)

### Running Component Tests

```bash
# Run all component tests
./gradlew componentTest

# Run specific feature
./gradlew componentTest -Dcucumber.filter.tags="@patient-management"

# Run tests with specific tag
./gradlew componentTest -Dcucumber.filter.tags="@smoke"

# Generate Cucumber reports
./gradlew componentTest -Dcucumber.plugin="html:build/reports/cucumber.html"
```

### Tagging Cucumber Scenarios

Use tags to organize and filter tests:

```gherkin
@entity-management @smoke
Feature: Entity Management

  @happy-path
  Scenario: Create a new entity
    Given I have valid entity information
    When I submit a request to create a new entity
    Then the entity is created successfully

  @error-handling
  Scenario: Handle duplicate entity
    Given an entity already exists with name "Test Entity"
    When I try to create another entity with the same name
    Then I receive a 409 Conflict response
```

## Test Coverage Guidelines

### What Makes Good Test Coverage

**✅ Good Coverage:**
- Tests all logical branches (if/else, switch)
- Tests error conditions and exceptions
- Tests boundary conditions
- Tests with different input variations
- Verifies interactions with dependencies

**❌ Poor Coverage:**
- Tests only happy path
- Tests that just call methods without assertions
- Tests that don't verify behavior
- Duplicate tests that add no value

### Example: Comprehensive Coverage

```java
@Test
void shouldValidateAge() {
    // Test boundary: minimum age
    assertThat(service.isValidAge(0)).isTrue();
    
    // Test boundary: maximum age
    assertThat(service.isValidAge(120)).isTrue();
    
    // Test invalid: negative age
    assertThat(service.isValidAge(-1)).isFalse();
    
    // Test invalid: too old
    assertThat(service.isValidAge(121)).isFalse();
    
    // Test normal case
    assertThat(service.isValidAge(45)).isTrue();
}
```

## Testing Exception Handlers

Exception handlers are tested as part of component tests since they're triggered by actual HTTP requests:

```gherkin
Feature: Error Handling

  Scenario: Handle entity not found
    Given no entity exists with ID "INVALID"
    When I request entity details for "INVALID"
    Then I receive a 404 Not Found response
    And the error message indicates entity not found
    And the error response contains a timestamp
    And the error response contains the request path

  Scenario: Handle validation errors
    Given I have entity information with invalid format
    When I submit a request to create a new entity
    Then I receive a 400 Bad Request response
    And the error response contains validation failures
    And the validation error indicates required field is invalid
```

Step definitions for error handling:

```java
@Then("the error response contains a timestamp")
public void theErrorResponseContainsTimestamp() {
    String timestamp = response.jsonPath().getString("timestamp");
    assertThat(timestamp).isNotNull().isNotEmpty();
}

@Then("the error response contains the request path")
public void theErrorResponseContainsRequestPath() {
    String path = response.jsonPath().getString("path");
    assertThat(path).isNotNull().contains("/api/v1/entities");
}

@Then("the error response contains validation failures")
public void theErrorResponseContainsValidationFailures() {
    List<Map<String, String>> errors = response.jsonPath().getList("validationErrors");
    assertThat(errors).isNotEmpty();
}

@Then("the validation error indicates {string} field is invalid")
public void theValidationErrorIndicatesFieldIsInvalid(String fieldName) {
    List<Map<String, String>> errors = response.jsonPath().getList("validationErrors");
    boolean hasFieldError = errors.stream()
            .anyMatch(error -> fieldName.equals(error.get("field")));
    assertThat(hasFieldError).isTrue();
}
```

## Test Naming Conventions

Use descriptive test names that explain the scenario:

```java
// Good naming patterns:
shouldReturnEntityWhenIdExists()
shouldThrowExceptionWhenIdIsNull()
shouldReturn404WhenEntityNotFound()
shouldValidateEmailFormat()

// Alternative pattern (Given-When-Then):
givenValidEntityId_whenGetEntity_thenReturnsEntity()
givenInvalidId_whenGetEntity_thenThrowsException()
```

## Quality Tools

### Jacoco Configuration

Jacoco measures code coverage. Configuration in `build.gradle`:

```gradle
jacoco {
    toolVersion = "0.8.8"
}

jacocoTestReport {
    reports {
        xml.enabled true
        html.enabled true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.90  // 90% coverage required
            }
        }
    }
}
```

### CheckStyle

Ensures code style consistency. Rules in `/config/checkstyle/checkstyle.xml`.

Run checks:
```bash
./gradlew checkstyleMain
./gradlew checkstyleTest
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all component tests
./gradlew componentTest

# Run all tests (unit + component)
./gradlew test componentTest

# Run unit tests with coverage report
./gradlew test jacocoTestReport

# Verify coverage threshold
./gradlew test jacocoTestCoverageVerification

# Run specific unit test class
./gradlew test --tests EntityServiceTest

# Run unit tests matching pattern
./gradlew test --tests *Service*

# Run specific Cucumber feature
./gradlew componentTest -Dcucumber.filter.tags="@entity-management"

# Run Cucumber tests with specific tag
./gradlew componentTest -Dcucumber.filter.tags="@smoke"
```

## Test Data Builders

Use builder pattern or test data factories:

```java
public class EntityTestDataBuilder {
    
    public static Entity createValidEntity() {
        return Entity.builder()
                .id("ENT123")
                .name("Test Entity")
                .description("Test Description")
                .status("ACTIVE")
                .createdDate(LocalDate.now())
                .build();
    }
    
    public static EntityRequest createValidRequest() {
        return EntityRequest.builder()
                .name("Test Entity")
                .description("Test Description")
                .category("TYPE_A")
                .build();
    }
}
```

## Common Testing Pitfalls to Avoid

❌ **Don't:**
- Write tests that depend on execution order
- Use real external services (use mocks)
- Test framework code (Spring Boot internals)
- Have tests with no assertions
- Ignore test failures
- Skip tests to meet deadlines

✅ **Do:**
- Make tests independent and isolated
- Mock external dependencies
- Test your own business logic
- Assert expected behavior
- Fix failing tests immediately
- Write tests as you code

## Test Coverage for New Features

When adding new functionality:

1. **Write tests first** (TDD approach) or alongside implementation
2. **Cover all scenarios:** happy path, error cases, edge cases
3. **Run coverage report:** `./gradlew jacocoTestReport`
4. **Check coverage:** Open `build/reports/jacoco/test/html/index.html`
5. **Ensure 90%+ coverage** for new code
6. **Fix any gaps** in coverage before submitting

---

[← Back to Main Instructions](../copilot-instructions.md)
