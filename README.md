# Data Validation Plugin

A Spring Boot plugin for declarative data validation using YAML configuration files.

## Features

- Declarative validation rules in YAML format
- Support for complex validation rules including patterns, ranges, and custom validators
- Integration with Spring Boot's auto-configuration
- Support for nested object validation
- Easy to extend with custom validation types

## Usage

### 1. Add the Dependency

Add the plugin to your project's dependencies:

```xml
<dependency>
    <groupId>com.example.datavalidation</groupId>
    <artifactId>validator</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Validation Rules Location

In your `application.properties` or `application.yml`:

```yaml
validation:
  rules:
    location: classpath:validation/
```

### 3. Create Validation Rules

Create YAML files in your validation rules directory (e.g., `src/main/resources/validation/`):

```yaml
rules:
  - name: username-format
    description: "Username must contain only alphanumeric characters and underscores"
    type: "pattern"
    field: "username"
    pattern: "^[a-zA-Z0-9_]+$"
    message: "Username must contain only alphanumeric characters and underscores"
```

### 4. Annotate Your Classes

Use the `@ValidatedBy` annotation to specify which validation rules file to use:

```java
@ValidatedBy("user-validation.yml")
public class User {
    private String username;
    // ... other fields
}
```

### 5. Use the Validation Engine

Inject the `ValidationEngine` into your service or controller:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final ValidationEngine validationEngine;

    public UserController(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        List<String> errors = validationEngine.validate(userData, User.class);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        // Process the valid user data
        return ResponseEntity.ok().build();
    }
}
```

## Validation Rule Types

The plugin supports the following validation types:

- `notBlank`: Checks if a string is not null and not empty after trimming
- `notNull`: Checks if a value is not null
- `pattern`: Validates a string against a regular expression
- `size`: Validates string length or collection size
- `min`: Validates numeric values against a minimum
- `max`: Validates numeric values against a maximum
- `enum`: Validates against a list of allowed values

## Example

See the `examples/simple-validation` directory for a complete example of using the plugin.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. 