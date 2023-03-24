package io.papermc.hangar.model.api.project.settings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Tag {

    ADDON,
    LIBRARY,
    SUPPORTS_FOLIA;

    private static final Map<String, Tag> TAGS = new HashMap<>();

    static {
        for (final Tag tag : values()) {
            TAGS.put(tag.name(), tag);
        }
    }

    public static Tag byName(final String name) {
        return TAGS.get(name.toUpperCase(Locale.ROOT));
    }
}
