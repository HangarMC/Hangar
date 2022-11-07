package io.papermc.hangar.config.hangar.converters;

import io.papermc.hangar.util.PatternWrapper;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@DefaultQualifier(NonNull.class)
@Component
@ConfigurationPropertiesBinding
public class StringToPatternWrapperConverter implements Converter<String, PatternWrapper> {

    @Override
    public @Nullable PatternWrapper convert(final String source) {
        return new PatternWrapper(Pattern.compile(source));
    }
}
