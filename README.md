# Data Validation Plugin

A Spring Boot plugin for declarative data validation using YAML configuration files. This plugin enforces strict separation of concerns between validation logic and business logic.

## Core Principles

1. **Separation of Concerns**
   - All validation logic resides in the plugin
   - Consumer applications only define rules and use annotations
   - No validation logic leaks into consuming applications
   - Clear boundaries between plugin and consumer responsibilities

2. **Plugin Responsibilities**
   - Validation processing engine
   - Rule loading and parsing
   - Validation annotations
   - Auto-configuration
   - Error message handling

3. **Consumer Responsibilities**
   - Define YAML validation rules
   - Apply `@ValidatedBy` annotations to models
   - Configure validation properties
   - Handle validation results

## Features

- Declarative validation rules in YAML format
- Support for complex validation rules including patterns, ranges, and custom validators
- Integration with Spring Boot's auto-configuration
- Support for nested object validation
- Easy to extend with custom validation types

## Documentation

- [Validation Rules Guide](docs/ValidationRules.md): Detailed guide on writing validation rules
- [Custom Validators Guide](docs/CustomValidators.md): How to create and use custom validators
- [Developer Guide](docs/DeveloperGuide.md): Plugin development and extension guide

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
- `custom`: Supports custom validation logic (see [Custom Validators Guide](docs/CustomValidators.md))

## Examples

See the `examples/simple-validation` directory for a complete example of using the plugin.

## Integration Best Practices

1. Keep all validation logic within the plugin
2. Use YAML files for rule definitions only
3. Avoid implementing validation logic in consumer applications
4. Use custom validators when built-in validators are insufficient
5. Follow the separation of concerns principle strictly

## Contributing

Contributions are welcome! Please read our [Developer Guide](docs/DeveloperGuide.md) before submitting a Pull Request. 