package io.papermc.hangar.db.modelold;


import org.jdbi.v3.core.annotation.Unmappable;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionDownloadWarningsTable {

    private long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiration;
    private String token;
    private long versionId;
    private InetAddress address; // inet
    private boolean isConfirmed;
    private Long downloadId;

    public ProjectVersionDownloadWarningsTable(OffsetDateTime expiration, String token, long versionId, InetAddress address) {
        this.expiration = expiration;
        this.token = token;
        this.versionId = versionId;
        this.address = address;
    }

    public ProjectVersionDownloadWarningsTable() { }

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


    public OffsetDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(OffsetDateTime expiration) {
        this.expiration = expiration;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }


    public boolean getIsConfirmed() {
        return isConfirmed;
    }

    public void setIsConfirmed(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }


    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    @Unmappable
    public static String cookieKey(long versionId) {
        return "_warning_" + versionId;
    }

    @Unmappable
    public boolean hasExpired() {
        return expiration.isBefore(OffsetDateTime.now());
    }
}
