package io.papermc.hangar.components.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class QueryMerger {

    private QueryMerger() {
    }

    public static Map<String, Object> merge(final List<Map<String, String>> input) {
        final Map<String, Object> result = new HashMap<>();
        try {
            merge(input, result, "");
        } catch (final Exception e) {
            System.out.println("error while merging");
            e.printStackTrace();
        }
        return result;
    }

    private static void merge(final List<Map<String, String>> input, final Map<String, Object> result, final String prefix) {
        System.out.println(prefix + "input: " + input);
        System.out.println(prefix + "result: " + result);

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
            System.out.println(prefix + "no sub keys found");
            for (final String key : input.getFirst().keySet()) {
                result.put(key, input.getFirst().get(key));
            }
            return;
        }

        System.out.println(prefix + "commonKeys: " + commonKeys);

        // create structure
        for (final String commonKey : commonKeys) {
            final Map<String, Map<String, Object>> current = new HashMap<>();
            result.put(commonKey, current);

            System.out.println(prefix + "check common key " + commonKey);

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
                        System.out.println(prefix + "primary key: " + pkValue);
                        newResult = current.computeIfAbsent(pkValue, _ -> new HashMap<>());
                        newResult.put(pk, row.get(key));
                    } else {
                        final String shortenedKey = key.replace(commonKey + "_", "");
                        System.out.println(prefix + "not primary key: " + shortenedKey);
                        others.put(shortenedKey, row.get(key));
                    }
                }

                if (pkValue == null) {
                    System.out.println(prefix + "no primary key found: " + others);
                    // no more nesting
                    for (final String key : others.keySet()) {
                        result.put(key, others.get(key));
                    }
                } else {
                    System.out.println(prefix + "others: " + others);
                    newInputs.computeIfAbsent(pkValue, _ -> new ArrayList<>()).add(others);
                }
            }

            System.out.println(prefix + "new inputs: " + newInputs);

            for (final String key : newInputs.keySet()) {
                System.out.println(prefix + "    recurse: " + key);
                merge(newInputs.get(key), current.get(key), prefix + "    ");
            }
        }

        // flatten map<primary key, values> to list<values>
        for (final String key : result.keySet()) {
            final Object entry = result.get(key);
            if (entry instanceof final Map map) {
                if (key.equals("pages") || key.equals("projects")) { // TODO cheat
                    result.put(key, map.values());
                } else {
                    result.put(key, map.values().stream().findFirst().orElseThrow());
                }
            }
        }

        System.out.println(prefix + "result: " + result);
    }
}
