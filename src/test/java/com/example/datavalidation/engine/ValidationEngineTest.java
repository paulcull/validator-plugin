package com.example.datavalidation.engine;

import com.example.datavalidation.annotation.ValidatedBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {
    private ValidationEngine validationEngine;
    private ValidationRuleLoader ruleLoader;
    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary directory for test files
        tempDir = new File(System.getProperty("java.io.tmpdir"), "validation-test");
        tempDir.mkdirs();
        
        // Copy test validation file to temp directory
        ClassPathResource resource = new ClassPathResource("validation/test-validation.yml");
        String content = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
        File testFile = new File(tempDir, "test-validation.yml");
        FileCopyUtils.copy(content.getBytes(), testFile);

        // Initialize validation components
        ruleLoader = new ValidationRuleLoader(tempDir.getAbsolutePath() + "/");
        validationEngine = new ValidationEngine(ruleLoader);
    }

    @Test
    void testValidateWithValidData() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test User");
        data.put("age", 25);
        
        List<String> errors = validationEngine.validate(data, TestEntity.class);
        assertTrue(errors.isEmpty(), "No validation errors should be present");
    }

    @Test
    void testValidateWithInvalidData() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", ""); // Empty name should fail @NotBlank validation
        data.put("age", -1); // Negative age should fail @Min validation
        
        List<String> errors = validationEngine.validate(data, TestEntity.class);
        assertEquals(3, errors.size(), "Should have three validation errors");
        assertTrue(errors.contains("name: must not be blank"), "Should have name blank error");
        assertTrue(errors.contains("Name must be between 2 and 50 characters"), "Should have name length error");
        assertTrue(errors.contains("age: must be greater than or equal to 0"), "Should have age minimum error");
    }

    @ValidatedBy("test-validation.yml")
    private static class TestEntity {
        @NotBlank
        private String name;
        
        @Min(0)
        private int age;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
} 