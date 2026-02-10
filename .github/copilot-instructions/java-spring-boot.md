# Java & Spring Boot Standards

[← Back to Main Instructions](../copilot-instructions.md)

## Project-Specific Conventions

This project uses **Spring Boot** with enterprise-grade practices for building a microservice. Follow these standards when generating or modifying code.

## Spring Boot Architecture

### Layer Structure

Follow the established three-layer architecture:

```
Controller Layer → Service Layer → Repository/External Services
     ↓                  ↓                      ↓
  REST API         Business Logic         Data Access
```

### Package Organization

```
com.company.project.domain/
├── controller/          # REST endpoints
├── service/             # Business logic
├── model/              # Domain objects and DTOs
├── repository/         # Data access (if applicable)
├── exception/          # Custom exceptions and handlers
├── config/             # Configuration classes
└── util/               # Utility classes
```

## Spring Annotations

### Use Appropriate Stereotypes

```java
@RestController                    // For REST controllers
@Service                          // For service layer
@Repository                       // For data access layer
@Configuration                    // For configuration classes
@Component                        // For generic Spring components
```

### Dependency Injection

**Prefer constructor injection:**

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class EntityService {
    private final EntityRepository repository;
    private final ValidationService validator;
    // Constructor injection via Lombok
}
```

**Avoid field injection:**
```java
// ❌ Don't do this
@Autowired
private PatientRepository repository;
```

## REST Controller Best Practices

### Structure Your Controllers

```java
@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@Slf4j
public class EntityController {
    
    private final EntityService service;
    
    @GetMapping("/{id}")
    public ResponseEntity<Entity> getEntity(
            @PathVariable String id) {
        log.info("Fetching entity with id: {}", id);
        return ResponseEntity.ok(service.getEntityById(id));
    }
    
    @PostMapping
    public ResponseEntity<Entity> createEntity(
            @Valid @RequestBody EntityRequest request) {
        Entity entity = service.createEntity(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entity);
    }
}
```

### Key Points

- Use `@RequestMapping` for base path
- Use specific HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.)
- Return `ResponseEntity` for better control over HTTP responses
- Use `@Valid` for request validation
- Add logging for important operations
- Use proper HTTP status codes

## Service Layer Best Practices

### Keep Services Focused

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityService {
    
    private final EntityRepository repository;
    private final ValidationService validationService;
    
    public Entity getEntityById(String id) {
        log.debug("Retrieving entity: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }
    
    @Transactional
    public Entity createEntity(EntityRequest request) {
        validationService.validate(request);
        Entity entity = mapToEntity(request);
        return repository.save(entity);
    }
}
```

### Service Layer Guidelines

- Use `@Transactional` for operations that modify data
- Implement business logic here, not in controllers
- Handle exceptions appropriately
- Keep methods focused on single responsibilities
- Use meaningful method names

## Exception Handling

### Global Exception Handler

Use the existing `GlobalExceptionHandler` pattern:

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex) {
        log.error("Validation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Custom Exceptions

Create meaningful custom exceptions:

```java
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityId) {
        super("Entity not found with id: " + entityId);
    }
}
```

## Lombok Usage

### Recommended Annotations

```java
@Data                    // Generates getters, setters, toString, equals, hashCode
@Builder                 // Implements Builder pattern
@NoArgsConstructor      // Generates no-args constructor
@AllArgsConstructor     // Generates constructor with all fields
@RequiredArgsConstructor // Constructor for final fields
@Slf4j                  // Generates logger field
@Value                  // Immutable version of @Data
```

### Example Usage

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
    private String id;
    private String name;
    private String description;
    private LocalDate createdDate;
    private String status;
}
```

## Configuration

### Use ecom-spring-boot-starter

This project uses custom Spring Boot starter:
- Leverage components from `ecom-spring-boot-starter`
- Follow established patterns for authentication and authorization
- Use provided utilities and common configurations

### Profile-Specific Configuration

Organize properties by environment:

```yaml
# application.yaml (common properties)
spring:
  application:
    name: ecom-service-patient-v2

# application-dev.yaml (dev-specific)
logging:
  level:
    com.medline: DEBUG
```

### External Configuration

Use `bootstrap.yaml` for:
- Spring Cloud Config settings
- Azure Key Vault integration
- Early initialization properties

## Object-Oriented Programming Principles

Follow solid object-oriented design principles when writing code:

### SOLID Principles

**Single Responsibility Principle (SRP)**
- Each class should have one reason to change
- Keep classes focused on a single concern
- Separate business logic, data access, and presentation layers

**Open/Closed Principle (OCP)**
- Classes should be open for extension but closed for modification
- Use interfaces and abstract classes for extensibility
- Prefer composition over inheritance

**Liskov Substitution Principle (LSP)**
- Subtypes must be substitutable for their base types
- Ensure derived classes honor the contract of base classes

**Interface Segregation Principle (ISP)**
- Clients should not depend on interfaces they don't use
- Create focused, specific interfaces rather than large general ones

**Dependency Inversion Principle (DIP)**
- Depend on abstractions, not concrete implementations
- Use dependency injection to provide implementations

### Encapsulation

- Keep fields private; expose through methods only when necessary
- Use immutable objects where possible
- Protect class invariants through validation in constructors and setters

### Polymorphism

- Use interfaces to define contracts
- Leverage polymorphism to write flexible, extensible code
- Prefer interface types over concrete types in method signatures

## Java 21 Language Features

This project uses **Java 21**. Leverage modern Java features for cleaner, more expressive code:

### Records (Java 16+)

Use records for immutable data carriers instead of traditional classes with boilerplate:

```java
// ✅ Good: Use record for DTOs and value objects
public record EntityRequest(
        String name,
        String description,
        LocalDate effectiveDate,
        String category) {
    
    // Compact constructor for validation
    public EntityRequest {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(description, "Description is required");
        if (category != null && category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
    }
}

// ❌ Avoid: Unnecessary class with boilerplate for simple data
@Data
@AllArgsConstructor
public class EntityRequest {
    private final String name;
    private final String description;
    // ... boilerplate ...
}
```

### Sealed Classes (Java 17+)

Use sealed classes to control inheritance hierarchy:

```java
public sealed interface PaymentMethod 
        permits CreditCard, DebitCard, Insurance {
}

public final class CreditCard implements PaymentMethod {
    private final String cardNumber;
    private final String cvv;
    // ...
}

public final class Insurance implements PaymentMethod {
    private final String policyNumber;
    // ...
}
```

### Pattern Matching for Switch (Java 21)

Use modern switch expressions with pattern matching:

```java
// ✅ Good: Modern switch with pattern matching
String processPayment(PaymentMethod payment) {
    return switch (payment) {
        case CreditCard cc -> processCreditCard(cc.cardNumber());
        case DebitCard dc -> processDebitCard(dc.accountNumber());
        case Insurance ins -> processInsurance(ins.policyNumber());
    };
}

// ❌ Avoid: Old-style switch with breaks
String processPayment(PaymentMethod payment) {
    String result;
    switch (payment.getType()) {
        case "CREDIT":
            result = processCreditCard(payment);
            break;
        // ... verbose and error-prone
    }
    return result;
}
```

### Text Blocks (Java 15+)

Use text blocks for multi-line strings:

```java
String query = """
        SELECT e.id, e.name, e.description
        FROM entities e
        WHERE e.status = 'ACTIVE'
        AND e.created_date > ?
        """;
```

### Pattern Matching for instanceof (Java 16+)

```java
// ✅ Good: Pattern matching
if (obj instanceof Entity entity) {
    return entity.getId();
}

// ❌ Avoid: Traditional instanceof with cast
if (obj instanceof Entity) {
    Entity entity = (Entity) obj;
    return entity.getId();
}
```

## Effective Java Best Practices

Follow guidelines from **Effective Java (4th Edition)** by Joshua Bloch:

### Item 1: Consider static factory methods instead of constructors

```java
public class Entity {
    private Entity(String id) { ... }
    
    public static Entity of(String id) {
        return new Entity(id);
    }
    
    public static Entity withValidation(String id) {
        validateId(id);
        return new Entity(id);
    }
}
```

### Item 2: Consider a builder when faced with many parameters

```java
// Use @Builder from Lombok or create custom builder
@Builder
public class Entity {
    private String id;
    private String name;
    private String description;
    private LocalDate effectiveDate;
    // ... many fields
}
```

### Item 17: Minimize mutability

Prefer immutable objects:
```java
// Use records for immutability
public record Address(String street, String city, String zipCode) {}

// Or use final fields with no setters
public final class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        // Returns new instance, doesn't modify existing
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### Item 49: Check parameters for validity

```java
public void processEntity(String entityId, LocalDate date) {
    Objects.requireNonNull(entityId, "Entity ID cannot be null");
    Objects.requireNonNull(date, "Date cannot be null");
    if (entityId.isBlank()) {
        throw new IllegalArgumentException("Entity ID cannot be empty");
    }
    // ... process
}
```

### Item 57: Minimize the scope of local variables

```java
// ✅ Good: Declare variable close to usage
for (Entity entity : entities) {
    String status = entity.getStatus();
    processStatus(status);
}

// ❌ Avoid: Unnecessarily wide scope
String status;
for (Entity entity : entities) {
    status = entity.getStatus();
    processStatus(status);
}
```

### Item 63: Beware the performance of string concatenation

```java
// ✅ Good: Use StringBuilder for loops
StringBuilder result = new StringBuilder();
for (String part : parts) {
    result.append(part);
}

// ❌ Avoid: String concatenation in loops
String result = "";
for (String part : parts) {
    result += part;  // Creates new string object each time
}
```

## Code Clarity and Readability

### Write Self-Documenting Code

**Code should be clear and concise:**
- Use descriptive variable and method names
- Keep methods short and focused (ideally < 20 lines)
- Extract complex conditions into well-named methods
- Let the code explain itself

```java
// ✅ Good: Self-documenting
public boolean isEligibleForDiscount(Entity entity) {
    return entity.getAge() > 65 || entity.hasSpecialStatus();
}

if (isEligibleForDiscount(entity)) {
    applyDiscount();
}

// ❌ Avoid: Unclear with unnecessary comments
// Check if entity is older than 65 or has special status
if (e.getAge() > 65 || e.hasSS()) {  // apply discount
    applyDiscount();
}
```

### Avoid Unnecessary Comments

**Don't comment what the code does; comment why it does it:**

```java
// ✅ Good: Explains the reason
// Using exponential backoff to handle transient network failures
retryWithBackoff(operation);

// ❌ Avoid: States the obvious
// Loop through entities
for (Entity entity : entities) {
    // Process each entity
    process(entity);
}
```

**When comments are needed:**
- Complex algorithms requiring explanation
- Business rules that aren't obvious from code
- Workarounds for external system limitations
- API documentation (JavaDoc)

## Streams API Best Practices

### Use Streams Appropriately

**Use streams for collection processing:**

```java
// ✅ Good: Clear stream pipeline
List<String> activeEntityNames = entities.stream()
        .filter(Entity::isActive)
        .map(Entity::getName)
        .sorted()
        .toList();

// Process with method references
entities.stream()
        .filter(this::needsProcessing)
        .forEach(this::processEntity);
```

### When to Use Parallel Streams

**Use parallel streams only when:**
- Working with large data sets (typically 10,000+ elements)
- Operations are computationally intensive
- Operations are stateless and independent
- You've measured and confirmed performance improvement

```java
// ✅ Good: Parallel for CPU-intensive operations on large datasets
List<ProcessedData> results = largeDataset.parallelStream()
        .map(this::expensiveComputation)
        .toList();

// ❌ Avoid: Unnecessary parallelization
List<String> names = entities.parallelStream()  // Small dataset
        .map(Entity::getName)  // Simple operation
        .toList();
```

**Don't use parallel streams when:**
- Dataset is small (< 10,000 elements)
- Operations are I/O-bound (database, network, file system)
- Operations have side effects or shared mutable state
- Order matters and must be preserved

### Stream Performance Tips

```java
// ✅ Good: Efficient filtering first
long count = entities.stream()
        .filter(Entity::isActive)  // Filter first (reduces elements)
        .filter(this::hasValidStatus)
        .map(Entity::getId)  // Map only filtered elements
        .count();

// ❌ Avoid: Unnecessary intermediate operations
entities.stream()
        .map(Entity::getId)  // Maps all elements unnecessarily
        .filter(id -> isActive(id))
        .count();
```

## Concurrency Guidelines

### Avoid Unnecessary Concurrency

**Spring Boot handles concurrency for web requests automatically.** Don't add concurrency unless specifically needed:

```java
// ✅ Good: Let Spring handle request concurrency
@RestController
public class EntityController {
    public ResponseEntity<Entity> getEntity(String id) {
        return ResponseEntity.ok(service.getEntity(id));
    }
}

// ❌ Avoid: Unnecessary manual threading
@RestController
public class EntityController {
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public ResponseEntity<Entity> getEntity(String id) {
        Future<Entity> future = executor.submit(() -> service.getEntity(id));
        // Unnecessary complexity
    }
}
```

### No Synchronized Blocks

**Avoid synchronized blocks.** Instead:
- Use thread-safe collections (e.g., `ConcurrentHashMap`)
- Use atomic variables (e.g., `AtomicInteger`, `AtomicReference`)
- Design stateless services (Spring's default)
- Use database transactions for data consistency

```java
// ✅ Good: Thread-safe concurrent collection
private final Map<String, Entity> cache = new ConcurrentHashMap<>();

public void cacheEntity(Entity entity) {
    cache.put(entity.getId(), entity);
}

// ❌ Avoid: Synchronized blocks
private final Map<String, Entity> cache = new HashMap<>();

public synchronized void cacheEntity(Entity entity) {
    cache.put(entity.getId(), entity);
}
```

### Use Non-Blocking I/O When Appropriate

**For reactive applications or high-throughput scenarios:**

```java
// ✅ Good: WebClient for non-blocking HTTP calls
@Service
public class ExternalDataService {
    private final WebClient webClient;
    
    public Mono<DataResponse> fetchData(String id) {
        return webClient.get()
                .uri("/data/{id}", id)
                .retrieve()
                .bodyToMono(DataResponse.class);
    }
}

// Standard blocking I/O is fine for typical CRUD operations
@Service
public class EntityService {
    public Entity getEntity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }
}
```

**When to use non-blocking I/O:**
- High-concurrency scenarios (thousands of concurrent requests)
- Microservices calling multiple external services
- Long-running I/O operations

**When blocking I/O is fine:**
- Standard CRUD operations
- Low to moderate traffic
- Simple request-response patterns

## Java Best Practices Summary

### Code Style

- Follow CheckStyle rules in `/config/checkstyle/checkstyle.xml`
- Use meaningful variable and method names
- Keep methods short and focused (< 20-30 lines ideally)
- Write self-documenting code; avoid unnecessary comments

### Null Safety

```java
// Use Optional for potentially null returns
public Optional<Entity> findEntity(String id) {
    return repository.findById(id);
}

// Use Objects.requireNonNull for validation
public void setEntityId(String id) {
    this.entityId = Objects.requireNonNull(id, "Entity ID cannot be null");
}
```

### Modern Java Features Checklist

- ✅ Use records for immutable data transfer objects
- ✅ Use sealed classes to control inheritance
- ✅ Use modern switch expressions with pattern matching
- ✅ Use text blocks for multi-line strings
- ✅ Use pattern matching for instanceof
- ✅ Use streams for collection processing
- ✅ Use parallel streams only when beneficial
- ❌ Don't add unnecessary concurrency
- ❌ Don't use synchronized blocks
- ❌ Don't use blocking I/O for high-throughput operations

## Logging Best Practices

### Use SLF4J with Lombok

```java
@Slf4j
public class EntityService {
    
    public void processEntity(String id) {
        log.debug("Processing entity: {}", id);  // Detailed info
        log.info("Entity processed successfully: {}", id);  // Important events
        log.warn("Entity data incomplete: {}", id);  // Warnings
        log.error("Failed to process entity: {}", id, exception);  // Errors
    }
}
```

### Logging Levels

- **DEBUG:** Detailed information for debugging
- **INFO:** Important business events
- **WARN:** Potentially harmful situations
- **ERROR:** Error events that might allow the application to continue

## Testing Considerations

When generating code, consider:
- Each public method should be testable
- Avoid static methods that are hard to mock
- Use interfaces for dependencies to enable mocking
- Keep business logic in service layer (easier to test)

See [Testing & Quality](./testing-quality.md) for detailed testing standards.

## API Development

For REST API specific guidelines, see [API Development](./api-development.md).

---

[← Back to Main Instructions](../copilot-instructions.md)
