package com.example.datavalidation;

import com.example.datavalidation.engine.ValidationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {
    private final ValidationEngine validationEngine;
    private final ObjectMapper objectMapper;

    public ValidationController(ValidationEngine validationEngine, ObjectMapper objectMapper) {
        this.validationEngine = validationEngine;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/validate/{entityClass}")
    public List<String> validate(@RequestBody Map<String, Object> data, @PathVariable String entityClass) {
        try {
            Class<?> clazz = Class.forName(entityClass);
            return validationEngine.validate(data, clazz);
        } catch (ClassNotFoundException e) {
            return List.of("Invalid entity class: " + entityClass);
        }
    }
}
