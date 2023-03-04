package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.StringJoiner;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JarScanResultTable extends Table {

    private long versionId;
    private Platform platform;
    private String highestSeverity;
    private JSONB data;

    @JdbiConstructor
    public JarScanResultTable(final OffsetDateTime createdAt, final long id, final long versionId,  @EnumByOrdinal final Platform platform, final String highestSeverity, final JSONB data) {
        super(createdAt, id);
        this.versionId = versionId;
        this.platform = platform;
        this.highestSeverity = highestSeverity;
        this.data = data;
    }

    public JarScanResultTable(final long versionId, final Platform platform, final String highestSeverity, final JSONB data) {
        this.versionId = versionId;
        this.platform = platform;
        this.highestSeverity = highestSeverity;
        this.data = data;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public void setVersionId(final long versionId) {
        this.versionId = versionId;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return this.platform;
    }

    public void setPlatform(final Platform platform) {
        this.platform = platform;
    }

    public String getHighestSeverity() {
        return this.highestSeverity;
    }

    public void setHighestSeverity(final String highestSeverity) {
        this.highestSeverity = highestSeverity;
    }

    public JSONB getData() {
        return this.data;
    }

    public void setData(final JSONB data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JarScanResultTable.class.getSimpleName() + "[", "]")
            .add("versionId=" + this.versionId)
            .add("platform=" + this.platform)
            .add("highestSeverity='" + this.highestSeverity + "'")
            .add("id=" + this.id)
            .add("createdAt=" + this.createdAt)
            .toString();
    }
}
