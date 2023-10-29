package io.papermc.hangar.components.query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;

// TODO we need one query builder per query mapping or use subqueries
public class QueryBuilder {
    String rootTable = "";
    String from = "";
    String condition = "";
    Set<String> fields = new HashSet<>();
    Set<String> joins = new LinkedHashSet<>();
    Map<String, Function<Map<String, Object>, Object>> resolver = new HashMap<>();
    Map<String, Object> staticFields = new HashMap<>();

    public String buildSql() {
        return STR."""
           SELECT

           \{this.fields.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + ",\n" + b).orElse("")}

           \{this.from}

           \{this.joins.stream().reduce((a, b) -> a + "\n" + b).orElse("")}

           \{this.condition};
           """;
    }

    public Object execute(final Handle handle, final String sql, final Map<String, Object> variables) {
        Query select = handle.select(sql);
        // bind the arguments
        for (final var entry : variables.entrySet()) {
            select = select.bind(entry.getKey(), entry.getValue());
        }

        // execute the query and merge the result
        return QueryMerger.merge(select.mapToMap(String.class).collectIntoList());
    }
}
