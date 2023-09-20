package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Set;

public enum PermissionType {
    GLOBAL("global", 0),

    PROJECT("project", 1),

    ORGANIZATION("organization", 1);

    private final String value;
    private final Set<Integer> argCounts;

    PermissionType(final String value, final Integer... argCounts) {
        this.value = value;
        this.argCounts = Set.of(argCounts);
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(this.value);
    }

    public Set<Integer> getArgCounts() {
        return this.argCounts;
    }

    @JsonCreator
    public static PermissionType fromValue(final String text) {
        for (final PermissionType b : values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    private static final PermissionType[] VALUES = values();

    public static PermissionType[] getValues() {
        return VALUES;
    }
}
