package me.minidigger.hangar.db.model;


import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionUnsafeDownloadsTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private InetAddress address;
    private long downloadType;


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


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }


    public long getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(long downloadType) {
        this.downloadType = downloadType;
    }

}
