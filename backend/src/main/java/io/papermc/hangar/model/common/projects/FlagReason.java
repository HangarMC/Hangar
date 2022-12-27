package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum FlagReason {

    INAPPROPRIATE_CONTENT("project.flag.flags.inappropriateContent"),
    IMPERSONATION("project.flag.flags.impersonation"),
    SPAM("project.flag.flags.spam"),
    MAL_INTENT("project.flag.flags.malIntent"),
    OTHER("project.flag.flags.other");

    private final String title;

    FlagReason(final String title) {
        this.title = title;
    }

    @JsonValue
    public String getTitle() {
        return this.title;
    }

    @JsonCreator
    public static FlagReason creator(final String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static final FlagReason[] VALUES = values();

    public static FlagReason[] getValues() {
        return VALUES;
    }
}
