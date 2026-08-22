package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.springframework.web.context.request.NativeWebRequest;

public interface Filter<F extends FilterInstance, V> {

    Set<String> getQueryParamNames();

    default Map<String, String> getDeprecatedQueryParamNames() {
        return Map.of();
    }

    default String getSingleQueryParam() {
        return this.getQueryParamNames().stream().findFirst().orElseThrow();
    }

    V getValue(final NativeWebRequest webRequest);

    String getDescription();

    default boolean supports(final NativeWebRequest webRequest) {
        return this.getQueryParamNames().stream().anyMatch(webRequest.getParameterMap()::containsKey);
    }

    F create(NativeWebRequest webRequest);

    /**
     * Quotes and escapes a value for use as a string literal in a MeiliSearch filter expression, so
     * user-controlled input cannot break out of the filter (e.g. to bypass the visibility clause).
     */
    static String escapeMeili(final String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    interface FilterInstance {

        void createSql(StringBuilder sb, SqlStatement<?> q);

        default void createMeili(StringBuilder sb) {
            throw new UnsupportedOperationException("not implemented yet: " + this.getClass().getSimpleName() + "#createMeili");
        }
    }
}
