# Developer Guide

This guide provides detailed information about using and extending the Data Validation Plugin.

## Table of Contents
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Validation Rules](#validation-rules)
- [Custom Validators](#custom-validators)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

## Architecture

The validation system consists of several key components:

1. **ValidationEngine** - Core component that processes validation rules
2. **@ValidatedBy** - Annotation to reference validation rules in models
3. **ValidationRule** - Interface for implementing custom validators
4. **YAML Configuration** - Files containing validation rule definitions

### Component Interactions

```
Model Class (@ValidatedBy) → ValidationEngine → YAML Rules → Validation Logic
                                           ↘ Custom Validators
```

## Configuration

### Basic Setup

1. Add Spring Boot configuration:

```java
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationAutoConfiguration {
    @Bean
    public ValidationEngine validationEngine() {
        return new ValidationEngine();
    }
}
```

2. Configure validation properties in `application.yml`:

```yaml
validation:
  rules:
    location: classpath:validation/
```

### YAML Rule Structure

Validation rules follow this structure:

```yaml
rules:
  - type: [validation-type]
    message: "Error message"
    # Additional parameters based on type
```

## Validation Rules

### Built-in Validation Types

1. **notBlank**
   ```yaml
   - type: notBlank
     message: "Field cannot be blank"
   ```

2. **size**
   ```yaml
   - type: size
     min: 2
     max: 100
     message: "Length must be between 2 and 100"
   ```

3. **pattern**
   ```yaml
   - type: pattern
     regex: "^[A-Za-z0-9]+$"
     message: "Only alphanumeric characters allowed"
   ```

4. **past**
   ```yaml
   - type: past
     message: "Date must be in the past"
   ```

### Rule Organization

Organize validation rules by entity and field:

```
src/main/resources/validation/
├── user/
│   ├── name.yml
│   ├── email.yml
│   └── birthDate.yml
└── product/
    ├── name.yml
    └── price.yml
```

## Custom Validators

### Creating a Custom Validator

1. Implement the `ValidationRule` interface:

```java
public class MinimumAgeValidator implements ValidationRule<Person> {
    private static final int MINIMUM_AGE = 18;

    @Override
    public boolean validate(Person person) {
        if (person == null || person.getDateOfBirth() == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        Period age = Period.between(person.getDateOfBirth(), now);
        return age.getYears() >= MINIMUM_AGE;
    }

    @Override
    public String getMessage() {
        return "Person must be at least " + MINIMUM_AGE + " years old";
    }

    @Override
    public Class<Person> getTargetClass() {
        return Person.class;
    }
}
```

2. Reference in YAML:

```yaml
rules:
  - type: custom
    validator: "com.example.validation.MinimumAgeValidator"
    message: "Must be at least 18 years old"
```

## Error Handling

The validation system throws `ValidationException` when validation fails:

```java
try {
    validationEngine.validate(object);
} catch (ValidationException e) {
    // Handle validation error
    String errorMessage = e.getMessage();
}
```

## Best Practices

1. **Rule Organization**
   - Keep validation rules close to their models
   - Use descriptive file names
   - Group related validations

2. **Custom Validators**
   - Make them reusable
   - Keep them focused and single-purpose
   - Document expected inputs and behavior

3. **Error Messages**
   - Be specific and actionable
   - Include field name and constraints
   - Consider internationalization

4. **Performance**
   - Rules are cached after first load
   - Consider rule complexity in custom validators
   - Use appropriate validation types 