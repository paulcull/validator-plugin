package com.example.datavalidation;

import com.example.datavalidation.annotation.ValidatedBy;
import com.example.datavalidation.engine.ValidationEngine;
import com.example.datavalidation.engine.ValidationRuleLoader;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {
    private ValidationEngine validationEngine;
    private Path tempDir;

    @BeforeEach
    void setup() throws IOException {
        // Create temporary directory for test files
        tempDir = Path.of(System.getProperty("java.io.tmpdir"), "validation-test");
        Files.createDirectories(tempDir);
        
        // Copy test files from classpath to temporary directory
        Resource resource = new ClassPathResource("validation/test-validation.yml");
        Files.copy(
            resource.getInputStream(),
            tempDir.resolve("test-validation.yml"),
            StandardCopyOption.REPLACE_EXISTING
        );

        // Initialize validation components
        ValidationRuleLoader ruleLoader = new ValidationRuleLoader(tempDir.toString() + "/");
        validationEngine = new ValidationEngine(ruleLoader);
    }

    @AfterEach
    void cleanup() throws IOException {
        // Clean up temporary directory
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Warning: Failed to delete test file: " + path);
                    }
                });
        }
    }

    @Test
    void testValidData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test User");
        data.put("age", 30);

        List<String> errors = validationEngine.validate(data, TestEntity.class);
        assertTrue(errors.isEmpty(), "Expected no validation errors");
    }

    @Test
    void testInvalidData() {
        Map<String, Object> data = new HashMap<>();
        data.put("age", -1); // Missing name and invalid age

        List<String> errors = validationEngine.validate(data, TestEntity.class);
        assertFalse(errors.isEmpty(), "Expected validation errors");
        assertTrue(errors.stream().anyMatch(error -> error.equals("name: must not be blank")), "Expected name validation error");
        assertTrue(errors.stream().anyMatch(error -> error.equals("age: must be greater than or equal to 0")), "Expected age validation error");
    }

    @ValidatedBy("test-validation.yml")
    static class TestEntity {
        private String name;
        private int age;
    }
}
