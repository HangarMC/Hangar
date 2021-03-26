package io.papermc.hangar.controller.extras.pagination.filters;

import io.papermc.hangar.controller.extras.pagination.filters.Filter.FilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.springframework.web.context.request.NativeWebRequest;

public interface Filter<F extends FilterInstance> {

    String getQueryParamName();

    default boolean supports(NativeWebRequest webRequest) {
        return webRequest.getParameterMap().containsKey(getQueryParamName());
    }

    F create(NativeWebRequest webRequest);

    interface FilterInstance {

        void createSql(StringBuilder sb, SqlStatement<?> q);
    }

}
