# Custom Validators Guide

This guide explains how to create and use custom validators in the Data Validation Plugin.

## Overview

Custom validators allow you to implement complex validation logic that can't be expressed through the standard validation types. They are particularly useful for:

- Business rule validation
- Cross-field validation
- Complex date/time validation
- Domain-specific validation

## Creating a Custom Validator

### Basic Structure

Custom validators must implement the `ValidationRule<T>` interface:

```java
public interface ValidationRule<T> {
    boolean validate(T object);
    String getMessage();
    Class<T> getTargetClass();
}
```

### Example Implementation

Here's a complete example of a custom validator that checks if a person is at least 18 years old:

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

## Using Custom Validators

### In YAML Configuration

Reference your custom validator in the validation rules YAML:

```yaml
rules:
  - type: custom
    validator: "com.example.validation.MinimumAgeValidator"
    message: "Must be at least 18 years old"
```

### Multiple Custom Validators

You can combine multiple custom validators:

```yaml
rules:
  - type: custom
    validator: "com.example.validation.MinimumAgeValidator"
    message: "Must be at least 18 years old"
  
  - type: custom
    validator: "com.example.validation.AddressValidator"
    message: "Invalid address format"
```

## Best Practices

### 1. Single Responsibility

Each validator should validate one specific aspect:

```java
// Good - Single responsibility
public class EmailFormatValidator implements ValidationRule<String> {
    @Override
    public boolean validate(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

// Bad - Multiple responsibilities
public class UserValidator implements ValidationRule<User> {
    @Override
    public boolean validate(User user) {
        // Validates email, age, and address in one validator
        return validateEmail(user.getEmail()) && 
               validateAge(user.getAge()) && 
               validateAddress(user.getAddress());
    }
}
```

### 2. Error Messages

Make error messages clear and specific:

```java
// Good
@Override
public String getMessage() {
    return "Person must be at least " + MINIMUM_AGE + " years old";
}

// Bad
@Override
public String getMessage() {
    return "Invalid age";
}
```

### 3. Null Safety

Always handle null values appropriately:

```java
@Override
public boolean validate(Person person) {
    if (person == null || person.getDateOfBirth() == null) {
        return false;  // or throw ValidationException
    }
    // Continue with validation
}
```

### 4. Performance Considerations

- Cache expensive computations
- Avoid unnecessary object creation
- Use efficient algorithms

```java
public class OptimizedValidator implements ValidationRule<Data> {
    private final Pattern pattern = Pattern.compile("^[A-Z]+$"); // Cached pattern

    @Override
    public boolean validate(Data data) {
        return pattern.matcher(data.getValue()).matches();
    }
}
```

## Advanced Topics

### 1. Parameterized Validators

Create validators that can be configured:

```java
public class MinimumValueValidator implements ValidationRule<Number> {
    private final double minimum;

    public MinimumValueValidator(double minimum) {
        this.minimum = minimum;
    }

    @Override
    public boolean validate(Number value) {
        return value != null && value.doubleValue() >= minimum;
    }
}
```

### 2. Composite Validators

Combine multiple validators:

```java
public class CompositeValidator<T> implements ValidationRule<T> {
    private final List<ValidationRule<T>> validators;
    private final Class<T> targetClass;

    public CompositeValidator(Class<T> targetClass, List<ValidationRule<T>> validators) {
        this.targetClass = targetClass;
        this.validators = validators;
    }

    @Override
    public boolean validate(T object) {
        return validators.stream().allMatch(v -> v.validate(object));
    }
}
```

### 3. Context-Aware Validation

Create validators that use external context:

```java
public class UniqueUsernameValidator implements ValidationRule<User> {
    private final UserRepository userRepository;

    public UniqueUsernameValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean validate(User user) {
        return !userRepository.existsByUsername(user.getUsername());
    }
}
```

## Testing Custom Validators

### Unit Test Example

```java
class MinimumAgeValidatorTest {
    private MinimumAgeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MinimumAgeValidator();
    }

    @Test
    void shouldPassForAdult() {
        Person person = new Person();
        person.setDateOfBirth(LocalDate.now().minusYears(20));
        assertTrue(validator.validate(person));
    }

    @Test
    void shouldFailForMinor() {
        Person person = new Person();
        person.setDateOfBirth(LocalDate.now().minusYears(17));
        assertFalse(validator.validate(person));
    }
} 