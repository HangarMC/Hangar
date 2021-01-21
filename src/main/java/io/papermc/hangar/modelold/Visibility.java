package io.papermc.hangar.modelold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Visibility {
    PUBLIC(0, "public", false, ""),

    NEW(1, "new", false, "project-new"),

    NEEDSCHANGES(2, "needsChanges", true, "striped project-needsChanges"),

    NEEDSAPPROVAL(3, "needsApproval", false, "striped project-needsChanges"),

    SOFTDELETE(4, "softDelete", true, "striped project-hidden");

    private final long value;
    private final String name;
    private final boolean showModal;
    private final String cssClass;

    Visibility(long value, String name, boolean showModal, String cssClass) {
        this.value = value;
        this.name = name;
        this.showModal = showModal;
        this.cssClass = cssClass;
    }

    public long getValue() {
        return value;
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
            if (b.value == visibility) {
                return b;
            }
        }
        return null;
    }
}
