package io.papermc.hangar.components.query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
    Map<String, Function<Map<String, String>, String>> resolver = new HashMap<>();

    public String buildSql() {
        return STR."""
           SELECT

           \{this.fields.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + ",\n" + b).orElse("")}

           \{this.from}

           \{this.joins.stream().reduce((a, b) -> a + "\n" + b).orElse("")}

           \{this.condition};
           """;
    }

    public List<Map<String, String>> execute(final Handle handle, final String sql, final Map<String, Object> variables) {
        Query select = handle.select(sql);
        // bind the arguments
        for (final var entry : variables.entrySet()) {
            select = select.bind(entry.getKey(), entry.getValue());
        }

        // execute the query
        return select.mapToMap(String.class).collectIntoList();
    }

    public void handleResolvers(final List<Map<String, String>> result) {
        Set<String> keysToRemove = null;
        for (final Map<String, String> inputMap : result) {
            // run the resolvers
            for (final var entry : this.resolver.entrySet()) {
                inputMap.put(entry.getKey(), entry.getValue().apply(inputMap));
            }
            // first time: find the ext keys
            if (keysToRemove == null) {
                keysToRemove = new HashSet<>();
                for (final String key : inputMap.keySet()) {
                    if (key.startsWith("ext_")) {
                        keysToRemove.add(key);
                    }
                }
            }
            // remove th ext keys
            for (final String key : keysToRemove) {
                inputMap.remove(key);
            }
        }
    }
}
