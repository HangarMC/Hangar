package io.papermc.hangar.exceptions;

public class InternalHangarException extends RuntimeException {

    public InternalHangarException(String message) {
        super(message);
    }

    public InternalHangarException(String message, Throwable cause) {
        super(message, cause);
    }
}
