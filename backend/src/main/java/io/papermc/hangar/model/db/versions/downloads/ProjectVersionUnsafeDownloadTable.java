package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionUnsafeDownloadTable extends Table {

    private final Long userId;
    private final InetAddress address;

    public ProjectVersionUnsafeDownloadTable(Long userId, InetAddress address) {
        this.userId = userId;
        this.address = address;
    }

    @JdbiConstructor
    public ProjectVersionUnsafeDownloadTable(OffsetDateTime createdAt, long id, Long userId, InetAddress address) {
        super(createdAt, id);
        this.userId = userId;
        this.address = address;
    }

    public Long getUserId() {
        return userId;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "ProjectVersionUnsafeDownloadTable{" +
                "userId=" + userId +
                ", address=" + address +
                "} " + super.toString();
    }
}
