package io.papermc.hangar.model.db;

import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class PlatformVersionTable extends Table {

    private final Platform platform;
    private final String version;

    @JdbiConstructor
    public PlatformVersionTable(final OffsetDateTime createdAt, final long id, @EnumByOrdinal final Platform platform, final String version) {
        super(createdAt, id);
        this.platform = platform;
        this.version = version;
    }

    public PlatformVersionTable(final Platform platform, final String version) {
        this.platform = platform;
        this.version = version;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return this.platform;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "PlatformVersionTable{" +
            "platform=" + this.platform +
            ", version='" + this.version + '\'' +
            "} " + super.toString();
    }
}
