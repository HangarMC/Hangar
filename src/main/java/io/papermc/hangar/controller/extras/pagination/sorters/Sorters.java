package io.papermc.hangar.controller.extras.pagination.sorters;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sorters {

    private static final Map<String, ApplySorter> SORTERS = new HashMap<>();

    public static final String JOINED_VALUE = "joinDate";
    public static final ApplySorter JOINED = register(JOINED_VALUE, (sb, dir) -> sb.append("u.join_date").append(dir));

    public static final String USERNAME_VALUE = "username";
    public static final ApplySorter USERNAME = register(USERNAME_VALUE, (sb, dir) -> sb.append("u.name").append(dir));

    public static final String PROJECT_COUNT_VALUE = "projectCount";
    public static final ApplySorter PROJECT_COUNT = register(PROJECT_COUNT_VALUE, (sb, dir) -> sb.append("project_count").append(dir));


    private static ApplySorter register(String name, ApplySorter applySorter) {
        if (SORTERS.containsKey(name)) {
            throw new IllegalArgumentException(name + " is already registered");
        }
        SORTERS.put(name, applySorter);
        return applySorter;
    }

    @NotNull
    public static ApplySorter getSorter(String key) {
        if (SORTERS.containsKey(key)) {
            return SORTERS.get(key);
        }
        throw new IllegalArgumentException(key + " is not a registered sorter");
    }

    @FunctionalInterface
    public interface ApplySorter {

        void applySorting(StringBuilder sb, SortDirection dir);

        default Consumer<StringBuilder> ascending() {
            return sb -> applySorting(sb, SortDirection.ASCENDING);
        }

        default Consumer<StringBuilder> descending() {
            return sb -> applySorting(sb, SortDirection.DESCENDING);
        }
    }
}
