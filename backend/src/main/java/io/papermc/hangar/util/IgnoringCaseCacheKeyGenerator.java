package io.papermc.hangar.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

@Component
public class IgnoringCaseCacheKeyGenerator implements KeyGenerator {

    @Override
    public @NotNull Object generate(final @NotNull Object target, final @NotNull Method method, final Object... params) {
        if (params.length == 0) {
            return SimpleKey.EMPTY;
        }
        if (params.length == 1) {
            Object param = params[0];
            if (param instanceof String s) {
                return s.toLowerCase();
            } else if (param != null && !param.getClass().isArray()) {
                return param;
            }
        }

        Object[] elements = Arrays.copyOf(params, params.length);
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof String s) {
                elements[i] = s.toLowerCase();
            }
        }
        return new SimpleKey(elements);
    }
}
