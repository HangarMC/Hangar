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

    Visibility(final String name, final boolean showModal, final String cssClass, final String title) {
        this.name = name;
        this.showModal = showModal;
        this.cssClass = cssClass;
        this.title = title;
    }

    public String getName() {
        return this.name;
    }

    public boolean getShowModal() {
        return this.showModal;
    }

    public String getCssClass() {
        return this.cssClass;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name;
    }

    @JsonCreator
    public static Visibility fromValue(final String text) {
        for (final Visibility b : values()) {
            if (b.name.equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static Visibility fromId(final long visibility) {
        for (final Visibility b : values()) {
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
