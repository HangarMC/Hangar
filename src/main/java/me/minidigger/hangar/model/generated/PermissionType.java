package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets type
 */
public enum PermissionType {
    GLOBAL("global"),

    PROJECT("project"),

    ORGANIZATION("organization");

    private final String value;

    PermissionType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static PermissionType fromValue(String text) {
        for (PermissionType b : PermissionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
