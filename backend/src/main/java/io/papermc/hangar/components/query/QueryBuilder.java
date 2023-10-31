package io.papermc.hangar.components.query;

import graphql.GraphQLContext;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;

public class QueryBuilder {

    public static final String QUERY_BUILDER = "queryBuilder";

    String rootTable = "";
    String from = "";
    String condition = "";
    Set<String> fields = new HashSet<>();
    Set<String> joins = new LinkedHashSet<>();
    Map<String, Function<Map<String, String>, String>> resolver = new HashMap<>();
    Map<String, Object> variables = new HashMap<>();

    public static List<QueryBuilder> getAllQueryBuilders(final GraphQLContext context) {
        return context.getOrDefault(QUERY_BUILDER, List.of());
    }

    public static QueryBuilder getActiveQueryBuilder(final GraphQLContext context) {
        return context.<List<QueryBuilder>>get(QUERY_BUILDER).getLast();
    }

    public static QueryBuilder newQueryBuilder(final GraphQLContext context) {
        if (!context.hasKey(QUERY_BUILDER)) {
            context.put(QUERY_BUILDER, new LinkedList<>());
        }
        final QueryBuilder newBuilder = new QueryBuilder();
        context.<List<QueryBuilder>>get(QUERY_BUILDER).add(newBuilder);
        return newBuilder;
    }

    public String buildSql() {
        return STR."""
           SELECT

           \{this.fields.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + ",\n" + b).orElse("")}

           \{this.from}

           \{this.joins.stream().reduce((a, b) -> a + "\n" + b).orElse("")}

           \{this.condition};
           """;
    }

    public List<Map<String, String>> execute(final Handle handle, final String sql) {
        Query select = handle.select(sql);
        // bind the arguments
        for (final var entry : this.variables.entrySet()) {
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
