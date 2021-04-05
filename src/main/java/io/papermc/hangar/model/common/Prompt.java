package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum Prompt {
    CHANGE_AVATAR("prompt.changeAvatar.title", "prompt.changeAvatar.message");

    final String titleKey;
    final String messageKey;

    Prompt(String titleKey, String messageKey) {
        this.titleKey = titleKey;
        this.messageKey = messageKey;
    }

    public String getName() { return name(); }

    public String getTitleKey() {
        return titleKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    private static final Prompt[] VALUES = values();

    public static Prompt[] getValues() { return VALUES; }
}
