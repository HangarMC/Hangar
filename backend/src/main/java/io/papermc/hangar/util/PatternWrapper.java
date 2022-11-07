package io.papermc.hangar.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public record PatternWrapper(Pattern pattern, Predicate<String> matcherPredicate) implements Predicate<String> {

    public PatternWrapper(final Pattern pattern) {
        this(pattern, pattern.asPredicate());
    }

    public String strPattern() {
        return this.pattern.pattern();
    }

    @Override
    public boolean test(final String s) {
        return this.matcherPredicate.test(s);
    }
}
