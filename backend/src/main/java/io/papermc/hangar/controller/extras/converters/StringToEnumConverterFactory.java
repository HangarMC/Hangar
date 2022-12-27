package io.papermc.hangar.controller.extras.converters;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.exceptions.HangarApiException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum> @NotNull Converter<String, T> getConverter(final @NotNull Class<T> targetType) {
        return s -> {
            // search for json creator first
            try {
                for (final Method declaredMethod : targetType.getDeclaredMethods()) {
                    if (declaredMethod.isAnnotationPresent(JsonCreator.class)) {
                        return (T) declaredMethod.invoke(null, s);
                    }
                }
            } catch (final IllegalAccessException | InvocationTargetException ignored) {
                // ignored
            }

            // try ordinal
            final int ordinal;
            try {
                ordinal = Integer.parseInt(s);
                return targetType.getEnumConstants()[ordinal];
            } catch (final NumberFormatException ignored) {
                // ignored
            }

            // fall back to value of
            try {
                return (T) Enum.valueOf(targetType, s.trim().toUpperCase());
            } catch (final IllegalArgumentException e) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, s + " did not match a valid " + targetType);
            }

        };
    }
}
