package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.db.Table;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jdbi.v3.core.annotation.JdbiProperty;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionDownloadWarningTable extends Table {

    private final OffsetDateTime expiresAt;
    private final UUID token;
    private final long versionId;
    private final InetAddress address;
    private boolean confirmed;
    private Long downloadId;

    public ProjectVersionDownloadWarningTable(final OffsetDateTime expiresAt, final UUID token, final long versionId, final InetAddress address) {
        this.expiresAt = expiresAt;
        this.token = token;
        this.versionId = versionId;
        this.address = address;
        this.confirmed = false;
        this.downloadId = null;
    }

    @JdbiConstructor
    public ProjectVersionDownloadWarningTable(final OffsetDateTime createdAt, final long id, final OffsetDateTime expiresAt, final UUID token, final long versionId, final InetAddress address, final boolean confirmed, final Long downloadId) {
        super(createdAt, id);
        this.expiresAt = expiresAt;
        this.token = token;
        this.versionId = versionId;
        this.address = address;
        this.confirmed = confirmed;
        this.downloadId = downloadId;
    }

    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public UUID getToken() {
        return this.token;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public boolean isConfirmed() {
        return this.confirmed;
    }

    public void setConfirmed(final boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Long getDownloadId() {
        return this.downloadId;
    }

    public void setDownloadId(final Long downloadId) {
        this.downloadId = downloadId;
    }

    @JdbiProperty(map = false)
    public static String cookieKey(final long versionId) {
        return "_warning_" + versionId;
    }

    @JdbiProperty(map = false)
    public boolean hasExpired() {
        return this.expiresAt.isBefore(OffsetDateTime.now());
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadWarningTable{" +
            "expiresAt=" + this.expiresAt +
            ", token=" + this.token +
            ", versionId=" + this.versionId +
            ", address=" + this.address +
            ", confirmed=" + this.confirmed +
            ", downloadId=" + this.downloadId +
            "} " + super.toString();
    }
}
