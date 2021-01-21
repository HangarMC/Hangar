package io.papermc.hangar.modelold;

public enum Prompt {

    CHANGE_AVATAR("prompt.changeAvatar.title", "prompt.changeAvatar.message");

    final String titleId;
    final String messageId;

    Prompt(String titleId, String messageId) {
        this.titleId = titleId;
        this.messageId = messageId;
    }

    public String getTitleId() {
        return titleId;
    }

    public String getMessageId() {
        return messageId;
    }
}
