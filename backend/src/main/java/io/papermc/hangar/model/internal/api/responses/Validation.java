package io.papermc.hangar.model.internal.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.papermc.hangar.util.PatternWrapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Validation(@Nullable String regex, @Nullable Integer max, @Nullable Integer min) {

    public Validation(final PatternWrapper wrapper, final @Nullable Integer max, final @Nullable Integer min) {
        this(wrapper.strPattern(), max, min);
    }

    public static Validation max(final int max) {
        return new Validation((String) null, max, null);
    }

    public static Validation size(final int max, final int min) {
        return new Validation((String) null, max, min);
    }
}

