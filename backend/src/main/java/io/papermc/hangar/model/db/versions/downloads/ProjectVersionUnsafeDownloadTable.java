package io.papermc.hangar.model.db.versions.downloads;

import io.papermc.hangar.model.db.Table;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionUnsafeDownloadTable extends Table {

    private final Long userId;
    private final InetAddress address;

    public ProjectVersionUnsafeDownloadTable(final Long userId, final InetAddress address) {
        this.userId = userId;
        this.address = address;
    }

    @JdbiConstructor
    public ProjectVersionUnsafeDownloadTable(final OffsetDateTime createdAt, final long id, final Long userId, final InetAddress address) {
        super(createdAt, id);
        this.userId = userId;
        this.address = address;
    }

    public Long getUserId() {
        return this.userId;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return "ProjectVersionUnsafeDownloadTable{" +
            "userId=" + this.userId +
            ", address=" + this.address +
            "} " + super.toString();
    }
}
