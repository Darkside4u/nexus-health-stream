# API Development

[← Back to Main Instructions](../copilot-instructions.md)

## RESTful API Standards

This project follows REST API best practices for building a microservice that integrates with the broader eCommerce ecosystem.

## HTTP Methods and Status Codes

### Use Appropriate HTTP Methods

```
GET     - Retrieve resources (read-only, idempotent)
POST    - Create new resources
PUT     - Update/replace entire resource (idempotent)
PATCH   - Partial update of resource
DELETE  - Remove resource (idempotent)
```

### Return Proper HTTP Status Codes

**Success Codes:**
- `200 OK` - Successful GET, PUT, PATCH, or DELETE
- `201 Created` - Successful POST that creates a resource
- `204 No Content` - Successful request with no response body

**Client Error Codes:**
- `400 Bad Request` - Invalid request format or validation failure
- `401 Unauthorized` - Authentication required or failed
- `403 Forbidden` - Authenticated but not authorized
- `404 Not Found` - Resource doesn't exist
- `409 Conflict` - Request conflicts with current state

**Server Error Codes:**
- `500 Internal Server Error` - Unexpected server error
- `503 Service Unavailable` - Service temporarily unavailable

### Example Implementation

```java
@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
public class EntityController {
    
    private final EntityService service;
    
    @GetMapping("/{id}")
    public ResponseEntity<Entity> getEntity(@PathVariable String id) {
        Entity entity = service.getEntityById(id);
        return ResponseEntity.ok(entity);  // 200 OK
    }
    
    @PostMapping
    public ResponseEntity<Entity> createEntity(
            @Valid @RequestBody EntityRequest request) {
        Entity entity = service.createEntity(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entity.getId())
                .toUri();
        return ResponseEntity.created(location).body(entity);  // 201 Created
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Entity> updateEntity(
            @PathVariable String id,
            @Valid @RequestBody EntityRequest request) {
        Entity entity = service.updateEntity(id, request);
        return ResponseEntity.ok(entity);  // 200 OK
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable String id) {
        service.deleteEntity(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
```

## API Versioning

### URL Path Versioning

Use version in the URL path:
```
/api/v1/entities
/api/v2/entities
```

## Request Validation

### Use Bean Validation

```java
@Data
@Builder
public class EntityRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
    private String description;
    
    @NotNull(message = "Effective date is required")
    @FutureOrPresent(message = "Effective date must be today or in the future")
    private LocalDate effectiveDate;
    
    @Email(message = "Contact email must be valid")
    private String contactEmail;
    
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Category must contain only uppercase letters, numbers, and underscores")
    private String category;
}
```

### Controller with Validation

```java
@PostMapping
public ResponseEntity<Entity> createEntity(
        @Valid @RequestBody EntityRequest request) {
    // @Valid triggers validation, returns 400 if validation fails
    Entity entity = service.createEntity(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(entity);
}
```

## Error Response Format

### Consistent Error Structure

```java
@Data
@Builder
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ValidationError> validationErrors;
    
    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
    }
}
```

### Global Exception Handler

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getDescription(false))
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## OpenAPI / Swagger Documentation

### Add OpenAPI Annotations

```java
@RestController
@RequestMapping("/api/v1/entities")
@Tag(name = "Entity API", description = "Endpoints for managing entity information")
@RequiredArgsConstructor
public class EntityController {
    
    @Operation(
        summary = "Get entity by ID",
        description = "Retrieves an entity's information by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Entity found",
            content = @Content(schema = @Schema(implementation = Entity.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Entity not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Entity> getEntity(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(service.getEntityById(id));
    }
    
    @Operation(
        summary = "Create new entity",
        description = "Creates a new entity record"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Entity created successfully",
            content = @Content(schema = @Schema(implementation = Entity.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<Entity> createEntity(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Entity information",
                required = true
            )
            @Valid @RequestBody EntityRequest request) {
        Entity entity = service.createEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }
}
```

### Document Models

```java
@Data
@Builder
@Schema(description = "Entity information")
public class Entity {
    
    @Schema(description = "Unique entity identifier", example = "ENT123456")
    private String id;
    
    @Schema(description = "Entity name", example = "Test Entity")
    private String name;
    
    @Schema(description = "Entity description", example = "A sample entity for demonstration")
    private String description;
    
    @Schema(description = "Entity status", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Entity creation date", example = "2024-01-15")
    private LocalDate createdDate;
}
```

### OpenAPI Configuration

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI entityServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Entity Service API")
                        .description("API for managing entity information")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@company.com")))
                .servers(List.of(
                        new Server().url("https://api-dev.company.com").description("Development"),
                        new Server().url("https://api.company.com").description("Production")
                ));
    }
}
```

## API Best Practices

### Resource Naming

**Use nouns, not verbs:**
- ✅ `/api/v1/entities`
- ❌ `/api/v1/getEntities`

**Use plural for collections:**
- ✅ `/api/v1/entities`
- ❌ `/api/v1/entity`

**Use hierarchical structure for relationships:**
- ✅ `/api/v1/entities/{entityId}/items`
- ✅ `/api/v1/entities/{entityId}/attributes/{attributeId}`

### Pagination and Filtering

For list endpoints, support pagination:

```java
@GetMapping
public ResponseEntity<Page<Entity>> getEntities(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sort) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sort));
    Page<Entity> entities = service.getEntities(pageRequest);
    return ResponseEntity.ok(entities);
}
```

Support filtering with query parameters:

```java
@GetMapping("/search")
public ResponseEntity<List<Entity>> searchEntities(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String category) {
    List<Entity> entities = service.searchEntities(name, status, category);
    return ResponseEntity.ok(entities);
}
```

### HATEOAS (Optional)

For discoverable APIs, include links:

```java
@GetMapping("/{id}")
public ResponseEntity<EntityModel<Entity>> getEntity(@PathVariable String id) {
    Entity entity = service.getEntityById(id);
    EntityModel<Entity> resource = EntityModel.of(entity);
    
    resource.add(linkTo(methodOn(EntityController.class).getEntity(id)).withSelfRel());
    resource.add(linkTo(methodOn(EntityController.class).getEntities(0, 20, "name"))
            .withRel("all-entities"));
    
    return ResponseEntity.ok(resource);
}
```

### Content Negotiation

Support multiple response formats:

```java
@GetMapping(
    value = "/{id}",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public ResponseEntity<Entity> getEntity(@PathVariable String id) {
    return ResponseEntity.ok(service.getEntityById(id));
}
```

## Security Considerations

### Authentication & Authorization

```java
@RestController
@RequestMapping("/api/v2/patients")
public class PatientV2Controller {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PatientV2> getPatient(@PathVariable String id) {
        // Only authenticated users can access
        return ResponseEntity.ok(service.getPatientById(id));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        // Only admins can delete
        service.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Input Sanitization

- Always validate and sanitize user input
- Use `@Valid` for bean validation
- Implement custom validators for complex rules
- Protect against SQL injection, XSS, etc.

### Rate Limiting

Consider implementing rate limiting for public endpoints to prevent abuse.

## Testing APIs

See [Testing & Quality](./testing-quality.md) for controller testing examples using MockMvc.

## External API Documentation

Reference OpenAPI specification in:
```
src/main/resources/schemas/servers/repository-openapi.yaml
```

Follow the patterns established in this specification when adding new endpoints.

---

[← Back to Main Instructions](../copilot-instructions.md)
