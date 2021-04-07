package io.papermc.hangar.exceptions;

/**
 * Use to throw/catch exceptions inside hangar.
 * Should <b>never</b> be thrown without catching it later.
 */
public class InternalHangarException extends RuntimeException {

    public InternalHangarException(String message) {
        super(message);
    }

    public InternalHangarException(String message, Throwable cause) {
        super(message, cause);
    }
}
