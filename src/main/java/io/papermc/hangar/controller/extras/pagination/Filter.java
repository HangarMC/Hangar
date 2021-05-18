package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

public interface Filter<F extends FilterInstance> {

    Set<String> getQueryParamNames();

    @NotNull
    default String getSingleQueryParam() {
        return getQueryParamNames().stream().findFirst().orElseThrow();
    }

    String getDescription();

    default boolean supports(NativeWebRequest webRequest) {
        return webRequest.getParameterMap().containsKey(getQueryParamNames());
    }

    @NotNull
    F create(NativeWebRequest webRequest);

    interface FilterInstance {

        void createSql(StringBuilder sb, SqlStatement<?> q);
    }

}
