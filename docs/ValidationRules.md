# Validation Rules Reference

This document provides a comprehensive reference for all available validation rules and their configuration options.

## Common Structure

All validation rules follow this basic structure:

```yaml
rules:
  - type: [validation-type]
    message: "Error message"
    # Additional parameters specific to the validation type
```

## Available Validation Types

### 1. Not Blank Validation

Ensures a string value is not null and not empty.

```yaml
- type: notBlank
  message: "Field cannot be blank"
```

### 2. Not Null Validation

Ensures a value is not null.

```yaml
- type: notNull
  message: "Field is required"
```

### 3. Size Validation

Validates the length of strings or collections.

```yaml
- type: size
  min: 2          # Minimum length (optional)
  max: 100        # Maximum length (optional)
  message: "Length must be between ${min} and ${max}"
```

### 4. Pattern Validation

Validates string values against a regular expression.

```yaml
- type: pattern
  regex: "^[A-Za-z0-9]+$"
  message: "Only alphanumeric characters allowed"
```

### 5. Date Validations

#### Past Date

Ensures a date is in the past.

```yaml
- type: past
  message: "Date must be in the past"
```

### 6. Custom Validation

Executes a custom validator class.

```yaml
- type: custom
  validator: "com.example.validation.CustomValidator"
  message: "Custom validation failed"
```

## Combining Multiple Rules

You can combine multiple rules for a single field:

```yaml
rules:
  - type: notBlank
    message: "Name is required"
  
  - type: size
    min: 2
    max: 50
    message: "Name must be between 2 and 50 characters"
  
  - type: pattern
    regex: "^[a-zA-Z\\s]+$"
    message: "Name can only contain letters and spaces"
```

## Rule Organization

### File Structure

Organize validation rules by entity and field:

```
validation/
├── person/
│   ├── name.yml
│   ├── email.yml
│   └── dateOfBirth.yml
└── order/
    ├── orderNumber.yml
    └── amount.yml
```

### Naming Conventions

- Use lowercase for file names
- Use descriptive names that match field names
- Group related validations in directories named after entities

## Examples

### Person Name Validation

```yaml
# validation/person/name.yml
rules:
  - type: notBlank
    message: "Name cannot be blank"
  
  - type: size
    min: 2
    max: 100
    message: "Name must be between 2 and 100 characters"
  
  - type: pattern
    regex: "^[a-zA-Z\\s\\-']+$"
    message: "Name can only contain letters, spaces, hyphens, and apostrophes"
```

### Date of Birth Validation

```yaml
# validation/person/dateOfBirth.yml
rules:
  - type: notNull
    message: "Date of birth is required"
  
  - type: past
    message: "Date of birth must be in the past"
  
  - type: custom
    validator: "com.example.validation.MinimumAgeValidator"
    message: "Must be at least 18 years old"
```

### Email Validation

```yaml
# validation/person/email.yml
rules:
  - type: notBlank
    message: "Email is required"
  
  - type: pattern
    regex: "^[A-Za-z0-9+_.-]+@(.+)$"
    message: "Invalid email format"
```

## Best Practices

1. **Message Templates**
   - Use clear, actionable messages
   - Include field name in message
   - Use placeholders for dynamic values

2. **Validation Order**
   - Place existence checks first (notNull, notBlank)
   - Follow with format validations (pattern)
   - End with complex validations (custom)

3. **Performance**
   - Use appropriate validation types
   - Avoid complex regex patterns when simple ones suffice
   - Consider caching implications for custom validators 

## Model Association

Validation rules are associated with model elements through a combination of class-level annotations and field-level specifications.

### Class-Level Association

Each model class must be annotated with `@ValidatedBy` to specify which YAML file contains its validation rules:

```java
@ValidatedBy("user-validation.yml")
public class User {
    private String username;
    private Address address;
    // ... other fields
}

@ValidatedBy("address-validation.yml")
public class Address {
    private String state;
    private String zipCode;
    // ... other fields
}
```

### Field-Level Association

In the YAML validation files, each rule specifies the field it applies to using the `field` property:

```yaml
# user-validation.yml
rules:
  - name: username-format
    field: "username"  # Applies to User.username
    type: "pattern"
    pattern: "^[a-zA-Z0-9_]+$"
    message: "Username must contain only alphanumeric characters and underscores"
    
  - name: address-state
    field: "address.state"  # Applies to User.address.state
    type: "enum"
    values: ["AL", "AK", "AZ", ...]
    message: "State must be a valid US state code"
```

### Nested Object Validation

The validation engine automatically handles nested objects. When validating a parent object:
- Rules in the parent's YAML file are applied to its direct fields
- Rules in the nested object's YAML file are applied to the nested object
- The `field` property can use dot notation to reference nested fields

Example:
```yaml
# user-validation.yml
rules:
  - name: address-required
    field: "address"  # Validates the entire Address object
    type: "notNull"
    message: "Address is required"
    
  - name: address-state
    field: "address.state"  # Validates a specific field in Address
    type: "enum"
    values: ["AL", "AK", "AZ", ...]
    message: "State must be a valid US state code"
``` 