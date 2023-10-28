package io.papermc.hangar.components.query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
    Set<String> joins = new HashSet<>();
    Map<String, Function<Map<String, Object>, Object>> resolver = new HashMap<>();
    Map<String, Object> staticFields = new HashMap<>();


    public String buildSql() {
        // todo sorting the joins by length is a hack to make sure we join the tables in the right order
        return STR."""
           SELECT

           \{this.fields.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + ",\n" + b).orElse("")}

           \{this.from}

           \{this.joins.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + "\n" + b).orElse("")}

           \{this.condition};
           """;
    }

    public Object execute(final Handle handle, final String sql, final Map<String, Object> variables) {
        Query select = handle.select(sql);
        for (final var entry : variables.entrySet()) {
            select = select.bind(entry.getKey(), entry.getValue());
        }
        return select
            .mapToMap()
            .collectIntoList()
            .stream().map(inputMap -> {
                // first put in static fields
                final Map<String, Object> outputMap = new HashMap<>(this.staticFields);
                Map<String, Object> currentMap = outputMap;

                // then run the resolvers
                for (final var entry : this.resolver.entrySet()) {
                    inputMap.put(entry.getKey(), entry.getValue().apply(inputMap));
                }

                // then clean it up into a tree
                for (final String key : inputMap.keySet()) {
                    final String[] parts = key.split("_");
                    for (int i = 0; i < parts.length - 1; i++) {
                        final String part = parts[i];
                        if (!currentMap.containsKey(part)) {
                            currentMap.put(part, new HashMap<String, Object>());
                        }
                        currentMap = (Map<String, Object>) currentMap.get(part);
                    }
                    currentMap.put(parts[parts.length - 1], inputMap.get(key));
                    currentMap = outputMap;
                }

                // TODO cleanup ext
                //outputMap.remove("ext");

                return outputMap;
            }).toList();
    }
}
