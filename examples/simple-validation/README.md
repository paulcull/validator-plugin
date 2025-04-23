# Simple Validation Example

This example demonstrates how to use the Data Validation Plugin with the `@ValidatedBy` annotation to validate user and address data.

## Project Structure

```
src/main/java/com/example/validation/
├── User.java                 # User model with @ValidatedBy annotation
├── Address.java              # Address model with @ValidatedBy annotation
└── UserController.java       # REST controller using validation

src/main/resources/validation/
├── user-validation.yml       # Validation rules for User class
└── address-validation.yml    # Validation rules for Address class
```

## Validation Rules

### User Validation Rules

The `user-validation.yml` file defines rules for the User class:

```yaml
rules:
  - name: username-format
    description: "Username must contain only alphanumeric characters and underscores"
    type: "pattern"
    field: "username"
    pattern: "^[a-zA-Z0-9_]+$"
    message: "Username must contain only alphanumeric characters and underscores"
    
  - name: password-complexity
    description: "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    type: "pattern"
    field: "password"
    pattern: "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"
    message: "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    
  - name: email-domain
    description: "Email must be from a valid domain"
    type: "pattern"
    field: "email"
    pattern: "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    message: "Email must be from a valid domain"
    
  - name: address-required
    description: "Address is required"
    type: "notNull"
    field: "address"
    message: "Address is required"
```

### Address Validation Rules

The `address-validation.yml` file defines rules for the Address class:

```yaml
rules:
  - name: street-required
    description: "Street address is required"
    type: "notBlank"
    field: "street"
    message: "Street address is required"
    
  - name: city-required
    description: "City is required"
    type: "notBlank"
    field: "city"
    message: "City is required"
    
  - name: state-format
    description: "State must be a valid US state code"
    type: "pattern"
    field: "state"
    pattern: "^[A-Z]{2}$"
    message: "State must be a valid US state code"
    
  - name: zip-code-format
    description: "Zip code must be in valid US format"
    type: "pattern"
    field: "zipCode"
    pattern: "^\\d{5}(-\\d{4})?$"
    message: "Zip code must be in valid US format"
```

## Usage Example

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

## Running the Example

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Test the validation:
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "Password123",
       "email": "test@example.com",
       "address": {
         "street": "123 Main St",
         "city": "Anytown",
         "state": "CA",
         "zipCode": "12345"
       }
     }'
   ```

## Expected Output

For valid data:
```json
{}
```

For invalid data:
```json
[
  "Username must contain only alphanumeric characters and underscores",
  "Password must contain at least one uppercase letter, one lowercase letter, and one number",
  "Email must be from a valid domain",
  "Street address is required",
  "State must be a valid US state code",
  "Zip code must be in valid US format"
]
``` 