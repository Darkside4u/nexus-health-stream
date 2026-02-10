# Code Generation & File Management

[← Back to Main Instructions](../copilot-instructions.md)

## File Management Principles

### ❌ Do NOT Generate Unnecessary Files

When working in agent mode or generating code, **avoid creating extraneous files** such as:

- **Summary files:** SUMMARY.md, changes-summary.txt, modifications-log.md, etc.
- **Fix documentation:** fixes.md, changes.md, what-changed.txt, etc.
- **Temporary files:** scratch.txt, notes.md, temp files, etc.
- **Backup files:** old-version.bak, component.backup.java, etc.
- **Duplicate versions:** Unless explicitly requested for versioning purposes

### ✅ Only Create Essential Files

Create files **only when**:
- The user explicitly requests a specific file
- The file is functionally necessary for the feature being implemented
- It's a standard project file (e.g., new Java class, test file, configuration)

### Modify Existing Files Directly

Instead of creating documentation about changes:
- **Edit the actual files** that need to be changed
- Use the appropriate tools to make direct modifications
- Provide explanations in the response itself, not in separate files

## Code Generation Best Practices

### Follow Project Structure

Respect the existing organization:
```
src/main/java/com/medline/ecom/patientv2/
  ├── controller/     # REST controllers
  ├── service/        # Business logic
  ├── model/          # Domain models and DTOs
  └── exception/      # Custom exceptions and handlers
```

### Naming Conventions

- **Classes:** PascalCase (e.g., `PatientV2Service`, `PatientV2Controller`)
- **Methods:** camelCase (e.g., `getPatientById`, `validatePatient`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages:** lowercase (e.g., `com.medline.ecom.patientv2`)

### Code Quality Standards

#### CheckStyle Compliance
- Follow the rules defined in `/config/checkstyle/checkstyle.xml`
- Ensure proper indentation and formatting
- Add appropriate JavaDoc comments for public methods and classes

#### Lombok Usage
- Use Lombok annotations as configured in `lombok.config`
- Common annotations: `@Data`, `@Builder`, `@Slf4j`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Reduces boilerplate code for getters, setters, constructors, and logging

### Example:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityModel {
    private String id;
    private String name;
    private String description;
    // Lombok generates getters, setters, equals, hashCode, toString
}
```

## Configuration Management

### Environment-Specific Configuration

Use the appropriate YAML files for different environments:
- `application-local.yaml` - Local development
- `application-dev.yaml` - Development environment
- `application-tst.yaml` - Test environment
- `application-ta.yaml` - Test automation environment
- `application-prd.yaml` - Production environment

### Secrets Management

**Never hardcode sensitive data!**

- Use Azure Key Vault for secrets and sensitive configuration
- Reference secrets through Spring Cloud Azure configuration
- Keep credentials out of source code and configuration files

Example:
```yaml
# Good: Reference to Key Vault
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - name: ${KEYVAULT_NAME}
```

## Providing Explanations

### Include in Your Response

When making changes, always provide:

1. **What was changed** - Clear description of modifications
2. **Why it was changed** - Reasoning and context
3. **How it works** - Explanation of the implementation
4. **Best practices used** - References to design patterns, principles, or standards
5. **Impact** - How this affects the system or project

### Reference Best Practices

When applicable, mention:
- Design patterns (e.g., Singleton, Factory, Strategy)
- SOLID principles
- Spring Boot best practices
- Java conventions and standards
- RESTful API guidelines

### Provide Links and References

Include references to:
- Official Spring Boot documentation
- Java API documentation
- Relevant RFCs for protocols or standards
- Industry best practices articles

## Example of Good Code Generation

Instead of creating `changes.md`, provide an explanation like this:

> "I've implemented the new validation service. Here's what was added:
>
> **Changes Made:**
> 1. Created `EntityValidationService.java` with validation logic
> 2. Updated `EntityService.java` to use the new validator
> 3. Added corresponding unit tests with 95% coverage
>
> **Why This Approach:**
> - Follows Single Responsibility Principle by separating validation logic
> - Makes the code more testable and maintainable
> - Follows the existing service pattern in the project
>
> **Best Practices Applied:**
> - Used Spring's `@Service` annotation for dependency injection
> - Implemented custom validation annotations for clarity
> - Added comprehensive error messages for better debugging
>
> This approach aligns with Spring Boot best practices as documented in the [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation)."

---

[← Back to Main Instructions](../copilot-instructions.md)
