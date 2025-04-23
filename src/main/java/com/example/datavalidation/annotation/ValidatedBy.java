package com.example.datavalidation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the validation rules file for a class.
 * The rules file should be in YAML format and contain validation rules
 * for the annotated class's fields.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatedBy {
    /**
     * The name of the validation rules file.
     * The file should be located in the validation rules directory.
     * @return the validation rules file name
     */
    String value();
} 