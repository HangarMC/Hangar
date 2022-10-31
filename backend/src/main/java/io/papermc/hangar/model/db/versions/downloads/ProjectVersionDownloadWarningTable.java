package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.db.Table;

import org.jdbi.v3.core.annotation.JdbiProperty;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProjectVersionDownloadWarningTable extends Table {

    private final OffsetDateTime expiresAt;
    private final UUID token;
    private final long versionId;
    private final InetAddress address;
    private boolean confirmed;
    private Long downloadId;

    public ProjectVersionDownloadWarningTable(OffsetDateTime expiresAt, UUID token, long versionId, InetAddress address) {
        this.expiresAt = expiresAt;
        this.token = token;
        this.versionId = versionId;
        this.address = address;
        this.confirmed = false;
        this.downloadId = null;
    }

    @JdbiConstructor
    public ProjectVersionDownloadWarningTable(OffsetDateTime createdAt, long id, OffsetDateTime expiresAt, UUID token, long versionId, InetAddress address, boolean confirmed, Long downloadId) {
        super(createdAt, id);
        this.expiresAt = expiresAt;
        this.token = token;
        this.versionId = versionId;
        this.address = address;
        this.confirmed = confirmed;
        this.downloadId = downloadId;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public UUID getToken() {
        return token;
    }

    public long getVersionId() {
        return versionId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    @JdbiProperty(map=false)
    public static String cookieKey(long versionId) {
        return "_warning_" + versionId;
    }

    @JdbiProperty(map=false)
    public boolean hasExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadWarningTable{" +
                "expiresAt=" + expiresAt +
                ", token=" + token +
                ", versionId=" + versionId +
                ", address=" + address +
                ", confirmed=" + confirmed +
                ", downloadId=" + downloadId +
                "} " + super.toString();
    }
}
