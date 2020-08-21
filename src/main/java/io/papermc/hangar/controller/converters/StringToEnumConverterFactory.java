package io.papermc.hangar.controller.converters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

@SuppressWarnings("rawtypes")
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    private static class StringtoEnumConverter<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        public StringtoEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String s) {
            int ordinal;
            try {
                ordinal = Integer.parseInt(s);
                return enumType.getEnumConstants()[ordinal];
            } catch (NumberFormatException e) {
                return (T) Enum.valueOf(this.enumType, s.trim().toUpperCase());
            }
        }
    }

    @Override
    public <T extends Enum> @NotNull Converter<String, T> getConverter(@NotNull Class<T> targetType) {
        return new StringtoEnumConverter<>(targetType);
    }
}
