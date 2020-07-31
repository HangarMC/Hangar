package me.minidigger.hangar.controller.converters;

import me.minidigger.hangar.model.Visibility;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

public class VisibilityConverter implements Converter<String, Visibility> {
    @Override
    public Visibility convert(@NotNull String visibilityValue) {
        return Visibility.fromId(Long.parseLong(visibilityValue));
    }
}
