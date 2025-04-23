package com.example.datavalidation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "validation.rules.location=file:${java.io.tmpdir}/validation-test",
    "validation.rules.file=validation-rules.json"
})
class ValidationRuleRepositoryTest {
    @Autowired
    private ValidationRuleRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary directory for test files
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "validation-test");
        Files.createDirectories(tempDir);
        
        // Copy test files from classpath to temporary directory
        String[] testFiles = {"test-rule.json", "list-rule.json", "delete-rule.json", "validation-rules.json"};
        for (String fileName : testFiles) {
            Resource resource = new ClassPathResource("validation/" + fileName);
            if (!resource.exists()) {
                throw new IllegalStateException("Test file not found: " + fileName);
            }
            
            // Copy the file to the temporary directory
            Files.copy(
                resource.getInputStream(),
                tempDir.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary directory
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "validation-test");
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
    void testSaveAndLoadRule() throws IOException {
        String ruleName = "test-rule.json";
        String content = "{\"type\":\"object\",\"properties\":{}}";
        
        repository.saveRule(ruleName, content);
        String loaded = repository.loadRule(ruleName);
        assertEquals(content, loaded, "Loaded content should match saved content");
    }

    @Test
    void testListRuleNames() throws IOException {
        List<String> rules = repository.listRuleNames();
        assertTrue(rules.contains("test-rule.json"), "Should find test-rule.json");
        assertTrue(rules.contains("list-rule.json"), "Should find list-rule.json");
        assertTrue(rules.contains("delete-rule.json"), "Should find delete-rule.json");
    }

    @Test
    void testDeleteRule() throws IOException {
        String ruleName = "delete-rule.json";
        String content = repository.loadRule(ruleName);
        assertNotNull(content, "Should be able to load rule before deletion");
        
        repository.deleteRule(ruleName);
        
        assertThrows(IOException.class, () -> {
            repository.loadRule(ruleName);
        }, "Should throw exception when loading deleted rule");
    }
}
