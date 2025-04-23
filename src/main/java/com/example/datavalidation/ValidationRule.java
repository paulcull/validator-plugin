package com.example.datavalidation;

/**
 * Interface defining the contract for validation rules.
 * @param <T> The type of object this rule validates
 */
public interface ValidationRule<T> {
    /**
     * Validates the given object against this rule.
     * @param object The object to validate
     * @return true if the object passes validation, false otherwise
     */
    boolean validate(T object);

    /**
     * Gets the error message to display when validation fails.
     * @return The validation error message
     */
    String getMessage();

    /**
     * Gets the class of objects this rule validates.
     * @return The target class
     */
    Class<T> getTargetClass();
} 