package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.generated.Version;

import java.time.OffsetDateTime;

public class Review {
    
    private Version version;
    private long userId;
    private OffsetDateTime endedAt;
    private String message;

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
