package io.papermc.hangar.controller.extras.converters;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.exceptions.HangarApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum> @NotNull Converter<String, T> getConverter(@NotNull Class<T> targetType) {
        return s -> {
            // search for json creator first
            try {
                for (Method declaredMethod : targetType.getDeclaredMethods()) {
                    if (declaredMethod.isAnnotationPresent(JsonCreator.class)) {
                        return (T) declaredMethod.invoke(null, s);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // ignored
            }

            // try ordinal
            int ordinal;
            try {
                ordinal = Integer.parseInt(s);
                return targetType.getEnumConstants()[ordinal];
            } catch (NumberFormatException ignored) {
                // ignored
            }

            // fall back to value of
            try {
                return (T) Enum.valueOf(targetType, s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, s + " did not match a valid " + targetType);
            }

        };
    }
}
