package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Prompt {
    CHANGE_AVATAR("prompts.changeAvatar.title", "prompts.changeAvatar.message");

    final String titleKey;
    final String messageKey;

    Prompt(final String titleKey, final String messageKey) {
        this.titleKey = titleKey;
        this.messageKey = messageKey;
    }

    public int getOrdinal() {
        return this.ordinal();
    }

    public String getName() {
        return this.name();
    }

    public String getTitleKey() {
        return this.titleKey;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    private static final Prompt[] VALUES = values();

    public static Prompt[] getValues() {
        return VALUES;
    }
}
