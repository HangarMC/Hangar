package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Visibility {
    PUBLIC(1, "public", false, ""),

    NEW(2, "new", false, "project-new"),

    NEEDSCHANGES(3, "needsChanges", true, "striped project-needsChanges"),

    NEEDSAPPROVAL(4, "needsApproval", false, "striped project-needsChanges"),

    SOFTDELETE(5, "softDelete", true, "striped project-hidden");

    private long value;
    private String name;
    private boolean showModal;
    private String cssClass;

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

    public boolean isShowModal() {
        return showModal;
    }

    public String getCssClass() {
        return cssClass;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Visibility fromValue(String text) {
        for (Visibility b : Visibility.values()) {
            if (String.valueOf(b.value).equals(text)) {
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
