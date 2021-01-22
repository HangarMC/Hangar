package io.papermc.hangar.model;

public enum NotificationType {

    PROJECT_INVITE(0),
    ORGANIZATION_INVITE(1),
    NEW_PROJECT_VERSION(2),
    VERSION_REVIEWED(3);

    private final long value;

    NotificationType(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
