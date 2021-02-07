package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@EnumByOrdinal
public enum Visibility {
    PUBLIC("public", false, ""),

    NEW("new", false, "project-new"),

    NEEDSCHANGES("needsChanges", true, "striped project-needsChanges"),

    NEEDSAPPROVAL("needsApproval", false, "striped project-needsChanges"),

    SOFTDELETE("softDelete", true, "striped project-hidden");

    private final String name;
    private final boolean showModal;
    private final String cssClass;

    Visibility(String name, boolean showModal, String cssClass) {
        this.name = name;
        this.showModal = showModal;
        this.cssClass = cssClass;
    }

    public String getName() {
        return name;
    }

    public boolean getShowModal() {
        return showModal;
    }

    public String getCssClass() {
        return cssClass;
    }

    @Override
    @JsonValue
    public String toString() {
        return name;
    }

    @JsonCreator
    public static Visibility fromValue(String text) {
        for (Visibility b : Visibility.values()) {
            if (b.name.equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static Visibility fromId(long visibility) {
        for (Visibility b : Visibility.values()) {
            if (b.ordinal() == visibility) {
                return b;
            }
        }
        return null;
    }

    private static final Visibility[] VALUES = values();

    public static Visibility[] getValues() {
        return VALUES;
    }
}
