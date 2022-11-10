package io.papermc.hangar.controller.validations;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import io.papermc.hangar.util.PatternWrapper;

@Component
public class Validations {

    private final Map<String, Pattern> regexCache = new HashMap<>();

    public boolean regex(String value, String regex) {
        if (isEmpty(value)) return true;
        Pattern pattern = regexCache.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(value).matches();
    }

    public boolean regex(String value, PatternWrapper regex) {
        if (isEmpty(value)) return true;
        return regex.test(value);
    }

    public boolean required(String value) {
        return !isEmpty(value);
    }

    public boolean max(Collection<?> value, int max) {
        if (value != null) {
            return value.size() <= max;
        }
        return true;
    }

    public boolean max(String value, int max) {
        if (value != null) {
            return value.length() <= max;
        }
        return true;
    }

    public boolean min(String value, int min) {
        if (value != null) {
            return value.length() >= min;
        }
        return true;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank() || value.isEmpty();
    }

    @Bean("validate")
    public Validations validate() {
        return new Validations();
    }
}
