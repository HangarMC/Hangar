package me.minidigger.hangar.util;

public class HangarException extends RuntimeException {

    private final String messageKey;
    private final String[] args;

    public HangarException(String messageKey) {
        this.messageKey = messageKey;
        this.args = null;
    }

    public HangarException(String messageKey, String... args) {
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getMessage() {
        return messageKey; // TODO implement message api
    }

    public String[] getArgs() {
        return args;
    }
}
