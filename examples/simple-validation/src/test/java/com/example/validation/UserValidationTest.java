package com.example.validation;

import com.example.datavalidation.engine.ValidationEngine;
import com.example.datavalidation.engine.ValidationRuleLoader;
import com.example.datavalidation.repository.ValidationRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private ValidationEngine validationEngine;
    private Path tempDir;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("validation-test");
        
        // Copy validation rules to temp directory
        File validationFile = new File(tempDir.toFile(), "user-validation.yml");
        FileCopyUtils.copy(
            new ClassPathResource("validation/user-validation.yml").getInputStream(),
            Files.newOutputStream(validationFile.toPath())
        );

        // Initialize components
        ValidationRuleRepository repository = new ValidationRuleRepository(tempDir.toString());
        ValidationRuleLoader loader = new ValidationRuleLoader(repository, new ObjectMapper());
        validationEngine = new ValidationEngine(loader, new ObjectMapper());
        objectMapper = new ObjectMapper();
    }

    @Test
    void testValidUser() throws Exception {
        // Create a valid user
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("Password123");
        user.setEmail("john.doe@example.com");
        
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("New York");
        address.setState("NY");
        address.setZipCode("10001");
        user.setAddress(address);

        // Convert to JSON
        String json = objectMapper.writeValueAsString(user);

        // Validate
        var result = validationEngine.validate(User.class, json);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testInvalidUser() throws Exception {
        // Create an invalid user
        User user = new User();
        user.setUsername("john@doe"); // Invalid username format
        user.setPassword("password"); // Missing uppercase and number
        user.setEmail("invalid-email"); // Invalid email format
        
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("New York");
        address.setState("XX"); // Invalid state
        address.setZipCode("1234"); // Invalid zip code
        user.setAddress(address);

        // Convert to JSON
        String json = objectMapper.writeValueAsString(user);

        // Validate
        var result = validationEngine.validate(User.class, json);
        assertFalse(result.isValid());
        assertEquals(5, result.getErrors().size()); // Expect 5 validation errors
    }
} 