package io.papermc.hangar.db.extras;

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
import java.util.regex.Pattern;

import io.papermc.hangar.model.api.requests.RequestPagination;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@SqlStatementCustomizingAnnotation(BindPagination.BindPaginationFactory.class)
public @interface BindPagination {

    class BindPaginationFactory implements SqlStatementCustomizerFactory {

        private static final Pattern valid = Pattern.compile("[a-zA-Z_]+");
        private static final Pattern dot = Pattern.compile("\\.");

        @Override
        public SqlStatementParameterCustomizer createForParameter(final Annotation annotation, final Class<?> sqlObjectType, final Method method, final Parameter param, final int index, final Type paramType) {
            return (q, arg) -> {
                RequestPagination pagination = (RequestPagination) arg;
                StringBuilder sb = new StringBuilder();

                filter(pagination, sb, q);
                sort(pagination, sb);
                offsetLimit(pagination, sb, q);

                // set the sql
                q.define("pagination", sb.toString());
            };
        }

        private void filter(RequestPagination pagination, StringBuilder sb, SqlStatement<?> q) {
            pagination.getFilters().forEach((key, value) -> {
                if (validate(key)) {
                    sb.append(" AND ").append(key).append(" = :").append(key);
                    q.bind(key, value);
                }
            });
        }

        private void sort(RequestPagination pagination, StringBuilder sb) {
            if (!pagination.getSorts().isEmpty()) {
                sb.append(" ORDER BY ");
                boolean first = true;
                for (String sort : pagination.getSorts()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }

                    boolean desc = sort.startsWith("-");
                    if (desc) {
                        sort = sort.substring(1);
                    }
                    if (validate(sort)) {
                        sb.append(sort).append(" ").append(desc ? "DESC" : "ASC");
                    }
                }
            }
        }

        private void offsetLimit(RequestPagination pagination, StringBuilder sb, SqlStatement<?> q) {
            sb.append(" LIMIT :limit OFFSET :offset ");
            q.bind("limit", pagination.getLimit());
            q.bind("offset", pagination.getOffset());
        }

        private boolean validate(String key) {
            if (key.contains(".")) {
                String[] keys = dot.split(key);
                if (keys.length == 2) {
                    return validate(keys[0]) && validate(keys[1]);
                } else {
                    return false;
                }
            } else return valid.matcher(key).matches();
        }
    }
}
