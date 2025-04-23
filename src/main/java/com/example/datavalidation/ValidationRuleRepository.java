package com.example.datavalidation;

import com.example.datavalidation.config.ValidationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class ValidationRuleRepository {
    private final String rulesLocation;
    private final String rulesFile;
    private Path tempDirectory;

    @Autowired
    public ValidationRuleRepository(ValidationProperties properties) {
        this.rulesLocation = properties.getRules().getLocation();
        this.rulesFile = properties.getRules().getFile();
        initializeTempDirectory();
    }

    private void initializeTempDirectory() {
        try {
            if (rulesLocation.startsWith("file:")) {
                String path = rulesLocation.substring("file:".length());
                tempDirectory = Paths.get(path);
                Files.createDirectories(tempDirectory);
            } else {
                // For both classpath and regular paths, use a temp directory
                tempDirectory = Files.createTempDirectory("validation-rules-");
                
                // If it's a classpath resource, copy the files
                if (rulesLocation.startsWith("classpath:")) {
                    String path = rulesLocation.substring("classpath:".length());
                    copyClasspathResources(path);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize rules directory", e);
        }
    }

    private void copyClasspathResources(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        if (resource.exists() && resource.isReadable()) {
            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, tempDirectory.resolve(rulesFile), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        if (tempDirectory != null && Files.exists(tempDirectory)) {
            try {
                Files.walk(tempDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Log but don't throw
                            System.err.println("Failed to delete: " + p);
                        }
                    });
            } catch (IOException e) {
                System.err.println("Failed to cleanup temp directory: " + tempDirectory);
            }
        }
    }

    private Path getRulesDirectory() {
        return tempDirectory;
    }

    public List<String> listRuleNames() throws IOException {
        Path dir = getRulesDirectory();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{json,yml,yaml}")) {
            List<String> names = new ArrayList<>();
            for (Path entry : stream) {
                names.add(entry.getFileName().toString());
            }
            return names;
        }
    }

    public String loadRule(String fileName) throws IOException {
        Path rulePath = getRulesDirectory().resolve(fileName);
        if (!Files.exists(rulePath)) {
            // Try loading from classpath if file doesn't exist in temp directory
            if (rulesLocation.startsWith("classpath:")) {
                Resource resource = new ClassPathResource(rulesLocation.substring("classpath:".length()) + "/" + fileName);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        byte[] content = is.readAllBytes();
                        String ruleContent = new String(content, StandardCharsets.UTF_8);
                        // Cache the content in temp directory
                        Files.writeString(rulePath, ruleContent);
                        return ruleContent;
                    }
                }
            }
            throw new NoSuchFileException(rulePath.toString());
        }
        return Files.readString(rulePath);
    }

    public void saveRule(String ruleName, String content) throws IOException {
        Path rulePath = getRulesDirectory().resolve(ruleName);
        Files.writeString(rulePath, content);
    }

    public void deleteRule(String ruleName) throws IOException {
        Path rulePath = getRulesDirectory().resolve(ruleName);
        Files.deleteIfExists(rulePath);
    }
}
