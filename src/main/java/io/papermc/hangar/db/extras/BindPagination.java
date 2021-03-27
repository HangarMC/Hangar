package io.papermc.hangar.db.extras;

import io.papermc.hangar.model.api.requests.RequestPagination;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@SqlStatementCustomizingAnnotation(BindPagination.BindPaginationFactory.class)
public @interface BindPagination {

    boolean isCount() default false;

    class BindPaginationFactory implements SqlStatementCustomizerFactory {

        @Override
        public SqlStatementParameterCustomizer createForParameter(final Annotation annotation, final Class<?> sqlObjectType, final Method method, final Parameter param, final int index, final Type paramType) {
            return (q, arg) -> {
                RequestPagination pagination = (RequestPagination) arg;
                BindPagination paginationConfig = param.getAnnotation(BindPagination.class);
                filter(pagination, q);
                if (!paginationConfig.isCount()) {
                    sorters(pagination, q);
                    offsetLimit(pagination, q);
                }
            };
        }

        private void filter(RequestPagination pagination, SqlStatement<?> q) {
            StringBuilder sb = new StringBuilder();
            pagination.getFilters().forEach(filter -> filter.createSql(sb, q));
            q.define("filters", sb.toString());
        }

        private void sorters(RequestPagination pagination, SqlStatement<?> q) {
            StringBuilder sb = new StringBuilder();
            if (!pagination.getSorters().isEmpty()) {
                sb.append(" ORDER BY ");
            }
            var iter = pagination.getSorters().iterator();
            while (iter.hasNext()) {
                iter.next().accept(sb);
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            q.define("sorters", sb.toString());
        }

        private void offsetLimit(RequestPagination pagination, SqlStatement<?> q) {
            q.bind("limit", pagination.getLimit());
            q.bind("offset", pagination.getOffset());
            q.define("offsetLimit", " LIMIT :limit OFFSET :offset ");
        }
    }
}
