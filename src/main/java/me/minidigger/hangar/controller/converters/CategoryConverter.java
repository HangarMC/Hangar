package me.minidigger.hangar.controller.converters;

import me.minidigger.hangar.model.Category;
import org.springframework.core.convert.converter.Converter;

public class CategoryConverter implements Converter<String, Category> {
    @Override
    public Category convert(String s) {
        return Category.fromTitle(s);
    }
}
