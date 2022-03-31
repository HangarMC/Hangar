package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.Version;

import java.time.OffsetDateTime;

public class Review {

    private final Version version;
    private final long userId;
    private final OffsetDateTime endedAt;
    private final String message;

    public Review(Version version, long userId, OffsetDateTime endedAt, String message) {
        this.version = version;
        this.userId = userId;
        this.endedAt = endedAt;
        this.message = message;
    }

    public Version getVersion() {
        return version;
    }

    public long getUserId() {
        return userId;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public String getMessage() {
        return message;
    }
}
