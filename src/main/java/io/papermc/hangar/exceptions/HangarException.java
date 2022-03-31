package io.papermc.hangar.exceptions;

public class HangarException extends RuntimeException {

    private final String messageKey;
    private final String[] args;

    public HangarException(String messageKey) {
        this.messageKey = messageKey;
        this.args = new String[0];
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
