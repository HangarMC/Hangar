package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Visibility {
    PUBLIC("public", false, "", "visibility.name.public"),

    NEW("new", false, "project-new", "visibility.name.new"),

    NEEDSCHANGES("needsChanges", true, "striped project-needsChanges", "visibility.name.needsChanges"),

    NEEDSAPPROVAL("needsApproval", false, "striped project-needsChanges", "visibility.name.needsApproval"),

    SOFTDELETE("softDelete", true, "striped project-hidden", "visibility.name.softDelete");

    private final String name;
    private final boolean showModal;
    private final String cssClass;
    private final String title;

    Visibility(String name, boolean showModal, String cssClass, String title) {
        this.name = name;
        this.showModal = showModal;
        this.cssClass = cssClass;
        this.title = title;
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

    public String getTitle() {
        return title;
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
