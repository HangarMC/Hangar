package io.papermc.hangar.controller.extras;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

@DefaultQualifier(NonNull.class)
public final class ApiUtils {

    public static final int DEFAULT_MAX_LIMIT = 50;
    public static final int DEFAULT_LIMIT = 25;

    private ApiUtils() {
    }

    /**
     * Gets the pagination limit or the max configured
     *
     * @param limit requested limit
     * @return actual limit
     */
    public static long limitOrDefault(final @Nullable Long limit) {
        return limitOrDefault(limit, DEFAULT_LIMIT);
    }

    public static long limitOrDefault(final @Nullable Long limit, final long maxLimit) {
        if (limit != null && limit < 1)
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Limit should be greater than 0");
        return Math.min(limit == null ? maxLimit : limit, maxLimit);
    }

    /**
     * Gets the pagination offset or 0
     *
     * @param offset the requested offset
     * @return actual offset
     */
    public static long offsetOrZero(final @Nullable Long offset) {
        return Math.max(offset == null ? 0 : offset, 0);
    }

    public static <T> @Nullable T mapParameter(final NativeWebRequest webRequest, final String param, final Function<String, T> map) {
        final @Nullable String value = webRequest.getParameter(param);
        if (StringUtils.hasText(value)) {
            return map.apply(value);
        }
        return null;
    }

}
