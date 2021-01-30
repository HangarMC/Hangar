package io.papermc.hangar.controller.extras.converters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.OffsetDateTime;

public class OffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(@NotNull String s) {
        return OffsetDateTime.parse(s);
    }
}
