package io.papermc.hangar.controller.extras.converters;

import java.time.OffsetDateTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(final @NotNull String s) {
        return OffsetDateTime.parse(s);
    }
}
