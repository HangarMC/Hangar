package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.springframework.web.context.request.NativeWebRequest;

public interface Filter<F extends FilterInstance> {

    boolean supports(NativeWebRequest webRequest);

    F create(NativeWebRequest webRequest);

    interface FilterInstance {
        void createSql(StringBuilder sb, SqlStatement<?> q);
    }

}
