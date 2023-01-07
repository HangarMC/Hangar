package io.papermc.hangar.db.extras;

import io.papermc.hangar.model.api.requests.RequestPagination;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation;
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer;

/**
 * Configure filters, sorters, offset, and limit from a web request
 * into a db query
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@SqlStatementCustomizingAnnotation(BindPagination.BindPaginationFactory.class)
public @interface BindPagination {

    /**
     * set to true to disable the injection of sorters, offset, and limit.<br>
     * used for getting the total count of all entries
     */
    boolean isCount() default false;

    class BindPaginationFactory implements SqlStatementCustomizerFactory {

        @Override
        public SqlStatementParameterCustomizer createForParameter(final Annotation annotation, final Class<?> sqlObjectType, final Method method, final Parameter param, final int index, final Type paramType) {
            return (q, arg) -> {
                final RequestPagination pagination = (RequestPagination) arg;
                final BindPagination paginationConfig = param.getAnnotation(BindPagination.class);
                this.filter(pagination, q);
                if (!paginationConfig.isCount()) {
                    this.sorters(pagination, q);
                    this.offsetLimit(pagination, q);
                }
            };
        }

        private void filter(final RequestPagination pagination, final SqlStatement<?> q) {
            final StringBuilder sb = new StringBuilder();
            pagination.getFilters().values().forEach(filter -> filter.createSql(sb, q));
            q.define("filters", sb.toString());
        }

        private void sorters(final RequestPagination pagination, final SqlStatement<?> q) {
            final StringBuilder sb = new StringBuilder();
            if (!pagination.getSorters().isEmpty()) {
                sb.append(" ORDER BY ");
            }
            final var iter = pagination.getSorters().entrySet().iterator();
            while (iter.hasNext()) {
                iter.next().getValue().accept(sb);
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            q.define("sorters", sb.toString());
        }

        private void offsetLimit(final RequestPagination pagination, final SqlStatement<?> q) {
            q.bind("limit", pagination.getLimit());
            q.bind("offset", pagination.getOffset());
            q.define("offsetLimit", " LIMIT :limit OFFSET :offset ");
        }
    }
}
