package io.papermc.hangar.controller.validations;

import io.papermc.hangar.util.PatternWrapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Validations {

    private final Map<String, Pattern> regexCache = new HashMap<>();

    public boolean regex(final String value, final String regex) {
        if (this.isEmpty(value)) return true;
        final Pattern pattern = this.regexCache.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(value).matches();
    }

    public boolean regex(final String value, final PatternWrapper regex) {
        if (this.isEmpty(value)) return true;
        return regex.test(value);
    }

    public boolean required(final String value) {
        return !this.isEmpty(value);
    }

    public boolean max(final Collection<?> value, final int max) {
        if (value != null) {
            return value.size() <= max;
        }
        return true;
    }

    public boolean max(final String value, final int max) {
        if (value != null) {
            return value.length() <= max;
        }
        return true;
    }

    public boolean min(final String value, final int min) {
        if (value != null) {
            return value.length() >= min;
        }
        return true;
    }

    private boolean isEmpty(final String value) {
        return value == null || value.isBlank() || value.isEmpty();
    }

    @Bean("validate")
    public Validations validate() {
        return new Validations();
    }
}
