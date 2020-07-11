package me.minidigger.hangar.util;

public class HangarException extends RuntimeException {

    private final String messageKey;

    public HangarException(String messageKey) {
        this.messageKey = messageKey;
    }

    public HangarException(String messageKey, String... args) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getMessage() {
        return messageKey; // TODO implement message api
    }
}
