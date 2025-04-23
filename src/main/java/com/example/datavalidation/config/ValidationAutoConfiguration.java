package com.example.datavalidation.config;

import com.example.datavalidation.engine.ValidationEngine;
import com.example.datavalidation.engine.ValidationRuleLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
@ComponentScan(basePackages = {"com.example.datavalidation.engine"})
public class ValidationAutoConfiguration {

    private final ValidationProperties properties;

    public ValidationAutoConfiguration(ValidationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationRuleLoader validationRuleLoader() {
        String rulesLocation = properties.getRules().getLocation();
        if (!StringUtils.hasText(rulesLocation)) {
            rulesLocation = "classpath:validation/";
        }
        return new ValidationRuleLoader(rulesLocation);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationEngine validationEngine(ValidationRuleLoader ruleLoader) {
        return new ValidationEngine(ruleLoader);
    }
} 