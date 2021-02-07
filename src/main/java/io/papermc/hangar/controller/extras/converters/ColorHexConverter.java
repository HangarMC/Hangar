package io.papermc.hangar.controller.extras.converters;

import io.papermc.hangar.model.common.Color;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ColorHexConverter implements Converter<String, Color> {
    @Override
    public Color convert(@NotNull String s) {
        return Color.getByHexStr(s);
    }
}
