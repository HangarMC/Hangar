package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.StringJoiner;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JarScanResultTable extends Table {

    private long versionId;
    private int scannerVersion;
    private Platform platform;
    private String highestSeverity;

    @JdbiConstructor
    public JarScanResultTable(final OffsetDateTime createdAt, final long id, final long versionId, final int scannerVersion, @EnumByOrdinal final Platform platform, final String highestSeverity) {
        super(createdAt, id);
        this.versionId = versionId;
        this.scannerVersion = scannerVersion;
        this.platform = platform;
        this.highestSeverity = highestSeverity;
    }

    public JarScanResultTable(final long versionId, final int scannerVersion, final Platform platform, final String highestSeverity) {
        this.versionId = versionId;
        this.scannerVersion = scannerVersion;
        this.platform = platform;
        this.highestSeverity = highestSeverity;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public void setVersionId(final long versionId) {
        this.versionId = versionId;
    }

    public int getScannerVersion() {
        return this.scannerVersion;
    }

    public void setScannerVersion(final int scannerVersion) {
        this.scannerVersion = scannerVersion;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", JarScanResultTable.class.getSimpleName() + "[", "]")
            .add("versionId=" + this.versionId)
            .add("scannerVersion=" + this.scannerVersion)
            .add("platform=" + this.platform)
            .add("highestSeverity='" + this.highestSeverity + "'")
            .add("id=" + this.id)
            .add("createdAt=" + this.createdAt)
            .toString();
    }
}
