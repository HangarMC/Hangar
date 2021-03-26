package io.papermc.hangar.controller.extras;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.util.StaticContextAccessor;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiUtils {

    private ApiUtils() { }

    private static final HangarConfig hangarConfig = StaticContextAccessor.getBean(HangarConfig.class);

    /**
     * Gets the pagination limit or the max configured
     * @param limit requested limit
     * @return actual limit
     */
    public static long limitOrDefault(@Nullable Long limit) {
        if (limit != null && limit < 1) throw new HangarApiException(HttpStatus.BAD_REQUEST, "Limit should be greater than 0");
        return Math.min(limit == null ? hangarConfig.projects.getInitLoad() : limit, hangarConfig.projects.getInitLoad());
    }

    /**
     * Gets the pagination offset or 0
     *
     * @param offset the requested offset
     * @return actual offset
     */
    public static long offsetOrZero(Long offset) {
        return Math.max(offset == null ? 0 : offset, 0);
    }

}
