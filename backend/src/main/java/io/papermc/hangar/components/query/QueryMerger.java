package io.papermc.hangar.components.query;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class QueryMerger {

    private final GraphQLSchema schema;

    public QueryMerger(final GraphQLSchema schema) {
        this.schema = schema;
    }

    public Map<String, Object> merge(final List<Map<String, String>> input) {
        final Map<String, Object> result = new HashMap<>();
        try {
            this.merge(input, result, "query", "");
        } catch (final Exception e) {
            System.out.println("error while merging");
            e.printStackTrace();
        }
        return result;
    }

    private void merge(final List<Map<String, String>> input, final Map<String, Object> result, final String parentKey, final String logPrefix) {
        System.out.println(logPrefix + "input: " + input);
        System.out.println(logPrefix + "result: " + result);

        // find common sub-keys in input
        boolean foundOneSub = false;
        final Set<String> commonKeys = new HashSet<>();
        for (final String key : input.getFirst().keySet()) {
            String shortenedKey = key;
            if (key.contains("_")) {
                foundOneSub = true;
                shortenedKey = key.substring(0, key.indexOf("_"));
            }
            commonKeys.add(shortenedKey);
        }

        // check if we really need to go one level deeper
        if (!foundOneSub) {
            System.out.println(logPrefix + "no sub keys found");
            for (final String key : input.getFirst().keySet()) {
                result.put(key, input.getFirst().get(key));
            }
            return;
        }

        System.out.println(logPrefix + "commonKeys: " + commonKeys);

        // create structure
        for (final String commonKey : commonKeys) {
            final Map<String, Map<String, Object>> current = new HashMap<>();
            result.put(commonKey, current);

            System.out.println(logPrefix + "check common key " + commonKey);

            final Map<String, List<Map<String, String>>> newInputs = new HashMap<>();

            // find primary keys
            for (final Map<String, String> row : input) {
                final Map<String, String> others = new HashMap<>();
                Map<String, Object> newResult;
                String pkValue = null;
                for (final String key : row.keySet()) {
                    if (!key.startsWith(commonKey)) continue;
                    final String pk = "name"; // TODO generic
                    if (key.equals(commonKey + "_" + pk)) {
                        pkValue = row.get(key);
                        System.out.println(logPrefix + "primary key: " + pkValue);
                        newResult = current.computeIfAbsent(pkValue, dum -> new HashMap<>());
                        newResult.put(pk, row.get(key));
                    } else {
                        final String shortenedKey = key.replaceFirst(commonKey + "_", "");
                        System.out.println(logPrefix + "not primary key: " + shortenedKey);
                        others.put(shortenedKey, row.get(key));
                    }
                }

                if (pkValue == null) {
                    System.out.println(logPrefix + "no primary key found: " + others);
                    result.put(commonKey, others);
                } else {
                    System.out.println(logPrefix + "others: " + others);
                    newInputs.computeIfAbsent(pkValue, dum -> new ArrayList<>()).add(others);
                }
            }

            System.out.println(logPrefix + "new inputs: " + newInputs);

            for (final String key : newInputs.keySet()) {
                System.out.println(logPrefix + "    recurse: " + key);
                this.merge(newInputs.get(key), current.get(key), parentKey + "_" + commonKey, logPrefix + "    ");
            }
        }

        // flatten map<primary key, values> to list<values>
        for (final String key : result.keySet()) {
            final Object entry = result.get(key);
            if (entry instanceof final Map map) {
                final GraphQLFieldDefinition fieldDefinition = this.getFieldDefinition(parentKey + "_" + key);
                if (fieldDefinition.getType() instanceof GraphQLList) {
                    // lists get flattened
                    result.put(key, map.values());
                } else if (fieldDefinition.getType() instanceof final GraphQLObjectType objectType) {
                    // virtual objects stay as map
                    if (objectType.getFieldDefinition("_virtual") == null) {
                        // normal objects should be a single value
                        result.put(key, map.values().stream().findFirst().orElseThrow());
                    }
                } else if (fieldDefinition.getType() instanceof GraphQLScalarType) {
                    // just get the scalar
                    result.put(key, map.get(key));
                } else {
                    throw new RuntimeException("should never reach " + parentKey + "_" + key);
                }
            }
        }

        System.out.println(logPrefix + "result: " + result);
    }

    private GraphQLFieldDefinition getFieldDefinition(final String key) {
        final String[] parts = key.split("_");
        GraphQLOutputType type = this.schema.getQueryType();
        GraphQLFieldDefinition queryFieldDefinition = null;
        outer:
        for (int i = 1; i < parts.length; i++) {
            if (type instanceof final GraphQLObjectType objectType) {
                for (final GraphQLFieldDefinition fieldDefinition : objectType.getFieldDefinitions()) {
                    if (fieldDefinition.getName().equalsIgnoreCase(parts[i])) {
                        queryFieldDefinition = fieldDefinition;
                        type = queryFieldDefinition.getType();
                        continue outer;
                    }
                }
                System.out.println("no field found: " + parts[i]);
            } else if (type instanceof final GraphQLList list) {
                var newType = list.getWrappedType();
                if (newType instanceof final GraphQLObjectType objectType) {
                    for (final GraphQLFieldDefinition fieldDefinition : objectType.getFieldDefinitions()) {
                        if (fieldDefinition.getName().equalsIgnoreCase(parts[i])) {
                            queryFieldDefinition = fieldDefinition;
                            type = queryFieldDefinition.getType();
                            continue outer;
                        }
                    }
                    System.out.println("no list field found: " + parts[i]);
                    return GraphQLFieldDefinition.newFieldDefinition().name("Dummy").type(GraphQLObjectType.newObject().name("Dummy")).build();
                } else {
                    System.out.println("unknown list type: " + type);
                }
            } else {
                System.out.println("unknown type: " + type);
            }
        }
        return queryFieldDefinition;
    }

}
