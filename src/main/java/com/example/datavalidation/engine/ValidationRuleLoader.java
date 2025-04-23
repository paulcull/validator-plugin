package com.example.datavalidation.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Loads validation rules from YAML files.
 */
public class ValidationRuleLoader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final String rulesLocation;

    public ValidationRuleLoader(String rulesLocation) {
        this.rulesLocation = StringUtils.cleanPath(rulesLocation);
    }

    /**
     * Loads validation rules from a YAML file.
     *
     * @param ruleFile the name of the YAML file containing validation rules
     * @return a list of validation rules
     * @throws IOException if there is an error reading the rules file
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadRules(String ruleFile) throws IOException {
        Resource resource;
        
        // First try to load from the rules location
        if (rulesLocation.startsWith("classpath:")) {
            resource = new ClassPathResource(rulesLocation.substring(10) + ruleFile);
        } else if (rulesLocation.startsWith("file:")) {
            resource = new FileSystemResource(rulesLocation.substring(5) + ruleFile);
        } else {
            resource = new FileSystemResource(rulesLocation + ruleFile);
        }

        // If not found in rules location, try to load from classpath
        if (!resource.exists()) {
            resource = new ClassPathResource("validation/" + ruleFile);
        }

        // If still not found, try to load from file system directly
        if (!resource.exists()) {
            resource = new FileSystemResource(ruleFile);
        }

        if (!resource.exists()) {
            throw new IOException("Rule file not found: " + ruleFile);
        }

        Map<String, Object> ruleSet = yamlMapper.readValue(resource.getInputStream(), Map.class);
        return (List<Map<String, Object>>) ruleSet.get("rules");
    }
} 