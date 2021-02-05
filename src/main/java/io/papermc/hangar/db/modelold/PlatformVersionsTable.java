package io.papermc.hangar.db.modelold;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class PlatformVersionsTable {
    private long id;
    private OffsetDateTime createdAt;
    private Platform platform;
    private String version;

    public PlatformVersionsTable(Platform platform, String version) {
        this.platform = platform;
        this.version = version;
    }

    public PlatformVersionsTable() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return platform;
    }

    @EnumByOrdinal
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
