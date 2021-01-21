package io.papermc.hangar.db.modelold;


import io.papermc.hangar.modelold.DownloadType;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionUnsafeDownloadsTable {

    private long id;
    private OffsetDateTime createdAt;
    private Long userId;
    private InetAddress address;
    private DownloadType downloadType;

    public ProjectVersionUnsafeDownloadsTable(Long userId, InetAddress address, DownloadType downloadType) {
        this.userId = userId;
        this.address = address;
        this.downloadType = downloadType;
    }

    public ProjectVersionUnsafeDownloadsTable() { }

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


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @EnumByOrdinal
    public DownloadType getDownloadType() {
        return downloadType;
    }

    @EnumByOrdinal
    public void setDownloadType(DownloadType downloadType) {
        this.downloadType = downloadType;
    }

}
