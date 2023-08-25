package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The visibility of a project or version", example = "PUBLIC")
public enum Visibility {
    PUBLIC("public", false, true, "", "visibility.name.public"),

    NEW("new", false, false, "project-new", "visibility.name.new"),

    NEEDSCHANGES("needsChanges", true, true, "striped project-needsChanges", "visibility.name.needsChanges"),

    NEEDSAPPROVAL("needsApproval", false, false, "striped project-needsChanges", "visibility.name.needsApproval"),

    SOFTDELETE("softDelete", true, true, "striped project-hidden", "visibility.name.softDelete");

    private final String name;
    private final boolean showModal;
    private final boolean canChangeTo;
    private final String cssClass;
    private final String title;

    Visibility(final String name, final boolean showModal, final boolean canChangeTo, final String cssClass, final String title) {
        this.name = name;
        this.showModal = showModal;
        this.canChangeTo = canChangeTo;
        this.cssClass = cssClass;
        this.title = title;
    }

    public String getName() {
        return this.name;
    }

    public boolean shouldShowModal() {
        return this.showModal;
    }

    public boolean canChangeTo() {
        return this.canChangeTo;
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
