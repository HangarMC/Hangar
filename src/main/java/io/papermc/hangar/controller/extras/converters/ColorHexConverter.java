package io.papermc.hangar.controller.extras.converters;

import io.papermc.hangar.modelold.Color;
import org.springframework.core.convert.converter.Converter;

public class ColorHexConverter implements Converter<String, Color> {
    @Override
    public Color convert(String s) {
        return Color.getByHexStr(s);
    }
}
