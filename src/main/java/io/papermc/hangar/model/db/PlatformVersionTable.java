package io.papermc.hangar.model.db;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class PlatformVersionTable extends Table {

    private final Platform platform;
    private final String version;

    @JdbiConstructor
    public PlatformVersionTable(OffsetDateTime createdAt, long id, @EnumByOrdinal Platform platform, String version) {
        super(createdAt, id);
        this.platform = platform;
        this.version = version;
    }

    public PlatformVersionTable(Platform platform, String version) {
        this.platform = platform;
        this.version = version;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "PlatformVersionTable{" +
                "platform=" + platform +
                ", version='" + version + '\'' +
                "} " + super.toString();
    }
}
