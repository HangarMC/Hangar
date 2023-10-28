package io.papermc.hangar.components.query;

import java.util.ArrayList;
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

        // execute the query
        return select.mapToMap().collectIntoList()
            .stream()
            // flatten the map
            .map(inputMap -> {
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
                // outputMap.remove("ext");

                return outputMap;
            })
            // combine the list of maps into one map
            .reduce(this::reduce);
    }

    private Map<String, Object> reduce(final Map<String, Object> input, final Map<String, Object> result) {
        return this.reduce(input, result, "");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> reduce(final Map<String, Object> input, final Map<String, Object> result, String parentKey) {
        for (final var entry : input.entrySet()) {
            // do we need to merge?
            if (result.containsKey(entry.getKey())) {
                final Object resultValue = result.get(entry.getKey());
                // if its a map and should stay a map, recurse
                String combinedKey = parentKey + entry.getKey();
                System.out.println("check " + combinedKey);
                if (resultValue instanceof Map && !(combinedKey.endsWith("projects.pages") /* || combinedKey.endsWith("projects")*/)) { // todo cheat
                    result.put(entry.getKey(), this.reduce((Map<String, Object>) entry.getValue(), (Map<String, Object>) resultValue, combinedKey + "."));
                } else if (resultValue instanceof final Set set) {
                    // found a new value for the existing set
                    set.add(entry.getValue());
                } else if (resultValue.equals(entry.getValue())) {
                    // same value, no need to merge
                    result.put(entry.getKey(), entry.getValue());
                } else /*if (entry.getValue() instanceof List list) {
                    list.add(resultValue); // TODO fix
                } else if (combinedKey.endsWith("projects")) {
                    // make a list
                    final List<Object> list = new ArrayList<>();
                    list.add(resultValue);
                    list.add(entry.getValue());
                    result.put(entry.getKey(), list);
                } else */ {
                    // create a new set
                    final Set<Object> set = new HashSet<>();
                    set.add(resultValue);
                    set.add(entry.getValue());
                    result.put(entry.getKey(), set);
                }
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
