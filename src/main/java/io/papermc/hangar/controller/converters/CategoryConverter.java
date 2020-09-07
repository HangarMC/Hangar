package io.papermc.hangar.controller.converters;

import io.papermc.hangar.model.Category;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

public class CategoryConverter implements Converter<String, List<Category>> {
    @Override
    public List<Category> convert(@NotNull String source) {
        return Category.fromString(source);
    }
}
