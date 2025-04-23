package com.example.datavalidation;

import com.example.datavalidation.annotation.ValidatedBy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.io.IOException;
import java.nio.file.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() throws IOException {
        // Create temporary directory for test files
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "validation-test");
        Files.createDirectories(tempDir);
        
        // Copy test files from classpath to temporary directory
        Resource resource = new ClassPathResource("validation/test-validation.yml");
        Files.copy(
            resource.getInputStream(),
            tempDir.resolve("test-validation.yml"),
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    @AfterEach
    void cleanup() throws IOException {
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
    void testValidateEndpointSuccess() throws Exception {
        String requestBody = "{\"name\":\"Test User\",\"age\":25}";
        mockMvc.perform(post("/api/validation/validate/" + TestEntity.class.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testValidateEndpointFailure() throws Exception {
        String requestBody = "{\"age\":10}";
        mockMvc.perform(post("/api/validation/validate/" + TestEntity.class.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*]").value(org.hamcrest.Matchers.hasItem("name: must not be blank")));
    }

    @ValidatedBy("test-validation.yml")
    static class TestEntity {
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
