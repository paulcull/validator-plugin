package com.example.datavalidation;

import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rules")
public class ValidationRuleController {
    private final ValidationRuleRepository repository;

    public ValidationRuleController(ValidationRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<String> listRules() throws IOException {
        return repository.listRuleNames();
    }

    @GetMapping("/{ruleName}")
    public String getRule(@PathVariable String ruleName) throws IOException {
        return repository.loadRule(ruleName);
    }

    @PostMapping
    public void saveRule(@RequestParam String ruleName, @RequestBody String content) throws IOException {
        repository.saveRule(ruleName, content);
    }

    @DeleteMapping("/{ruleName}")
    public void deleteRule(@PathVariable String ruleName) throws IOException {
        repository.deleteRule(ruleName);
    }
}
