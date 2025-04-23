package com.example.datavalidation.engine;

import com.example.datavalidation.annotation.ValidatedBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.nio.file.Path;

/**
 * Engine for validating objects against validation rules.
 */
@Component
public class ValidationEngine {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ValidationRuleLoader ruleLoader;

    public ValidationEngine(ValidationRuleLoader ruleLoader) {
        this.ruleLoader = ruleLoader;
    }

    public List<String> validate(Map<String, Object> data, Class<?> entityClass) {
        List<String> errors = new ArrayList<>();
        if (data == null) {
            errors.add("Cannot validate null data");
            return errors;
        }

        try {
            // Get the validation rules file name from the ValidatedBy annotation
            ValidatedBy validatedBy = entityClass.getAnnotation(ValidatedBy.class);
            if (validatedBy == null) {
                errors.add("No validation rules specified for class: " + entityClass.getName());
                return errors;
            }

            String rulesFileName = validatedBy.value();
            List<Map<String, Object>> rules = ruleLoader.loadRules(rulesFileName);
            for (Map<String, Object> rule : rules) {
                String field = (String) rule.get("field");
                Object value = getFieldValue(data, field);
                validateField(field, value, rule, errors);
            }
        } catch (Exception e) {
            errors.add("Error during validation: " + e.getMessage());
        }

        return errors;
    }

    public List<String> validate(Object object) {
        List<String> errors = new ArrayList<>();
        if (object == null) {
            errors.add("Cannot validate null object");
            return errors;
        }

        try {
            // Get the validation rules file name from the ValidatedBy annotation
            ValidatedBy validatedBy = object.getClass().getAnnotation(ValidatedBy.class);
            if (validatedBy == null) {
                errors.add("No validation rules specified for class: " + object.getClass().getName());
                return errors;
            }

            String rulesFileName = validatedBy.value();
            List<Map<String, Object>> rules = ruleLoader.loadRules(rulesFileName);
            for (Map<String, Object> rule : rules) {
                String field = (String) rule.get("field");
                Object value = getObjectFieldValue(object, field);
                validateField(field, value, rule, errors);
            }
        } catch (Exception e) {
            errors.add("Error during validation: " + e.getMessage());
        }

        return errors;
    }

    private Object getFieldValue(Map<String, Object> data, String fieldPath) {
        String[] fields = fieldPath.split("\\.");
        Object current = data;
        for (String field : fields) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(field);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object getObjectFieldValue(Object object, String fieldPath) {
        String[] fields = fieldPath.split("\\.");
        Object current = object;
        for (String field : fields) {
            try {
                Field declaredField = current.getClass().getDeclaredField(field);
                declaredField.setAccessible(true);
                current = declaredField.get(current);
                if (current == null) {
                    return null;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return null;
            }
        }
        return current;
    }

    private void validateField(String fieldName, Object value, Map<String, Object> rule, List<String> errors) {
        String type = (String) rule.get("type");
        String message = (String) rule.get("message");

        switch (type.toLowerCase()) {
            case "notblank":
                if (value == null || value.toString().trim().isEmpty()) {
                    errors.add(message);
                }
                break;

            case "notnull":
                if (value == null) {
                    errors.add(message);
                }
                break;

            case "size":
                if (value != null) {
                    String str = value.toString();
                    int min = ((Number) rule.get("min")).intValue();
                    int max = ((Number) rule.get("max")).intValue();
                    if (str.length() < min || str.length() > max) {
                        errors.add(message);
                    }
                }
                break;

            case "min":
                if (value != null) {
                    try {
                        int numValue = Integer.parseInt(value.toString());
                        int minValue = ((Number) rule.get("value")).intValue();
                        if (numValue < minValue) {
                            errors.add(message);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Invalid number format for field: " + fieldName);
                    }
                }
                break;

            case "pattern":
                if (value != null) {
                    String pattern = (String) rule.get("pattern");
                    if (!value.toString().matches(pattern)) {
                        errors.add(message);
                    }
                }
                break;

            case "enum":
                if (value != null) {
                    @SuppressWarnings("unchecked")
                    List<String> validValues = (List<String>) rule.get("values");
                    if (!validValues.contains(value.toString())) {
                        errors.add(message);
                    }
                }
                break;
        }
    }
} 