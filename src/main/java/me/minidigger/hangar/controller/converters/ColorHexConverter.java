package me.minidigger.hangar.controller.converters;

import me.minidigger.hangar.model.Color;
import org.springframework.core.convert.converter.Converter;

public class ColorHexConverter implements Converter<String, Color> {
    @Override
    public Color convert(String s) {
        return Color.getByHexStr(s);
    }
}
