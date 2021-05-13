package io.papermc.hangar.model.internal.job;

public class JobException extends RuntimeException {
    private final String descriptor;

    public JobException(String message, String descriptor) {
        super(message);
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
