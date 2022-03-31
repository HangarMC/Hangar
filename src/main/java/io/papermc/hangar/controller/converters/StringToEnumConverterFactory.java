package io.papermc.hangar.controller.converters;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    private static class StringtoEnumConverter<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        public StringtoEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            // search for json creator first
            try {
                for (Method declaredMethod : enumType.getDeclaredMethods()) {
                    if (declaredMethod.isAnnotationPresent(JsonCreator.class)) {
                        return (T) declaredMethod.invoke(null, source);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // ignored
            }

            // try ordinal
            int ordinal;
            try {
                ordinal = Integer.parseInt(source);
                return enumType.getEnumConstants()[ordinal];
            } catch (NumberFormatException ignored) {
                // ignored
            }

            // fall back to value of
            return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
        }
    }

    @Override
    public <T extends Enum> @NotNull Converter<String, T> getConverter(@NotNull Class<T> targetType) {
        return new StringtoEnumConverter<>(targetType);
    }
}
