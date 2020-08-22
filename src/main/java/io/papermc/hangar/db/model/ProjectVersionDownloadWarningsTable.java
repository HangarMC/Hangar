package io.papermc.hangar.db.model;


import java.time.OffsetDateTime;

public class ProjectVersionDownloadWarningsTable {

    private long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiration;
    private String token;
    private long versionId;
    private String address; // inet
    private boolean isConfirmed;
    private Long downloadId;


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


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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

    public static String cookieKey(long versionId) {
        return "_warning_" + versionId;
    }

    public boolean hasExpired() {
        return expiration.isBefore(OffsetDateTime.now());
    }
}
