package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.db.Table;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

public abstract class IndividualTable extends Table {

    private final InetAddress address;
    private final UUID cookie;
    private final Long userId;
    private final int processed;

    protected IndividualTable(final InetAddress address, final UUID cookie, final Long userId) {
        this.address = address;
        this.cookie = cookie;
        this.userId = userId;
        this.processed = 0;
    }

    protected IndividualTable(final OffsetDateTime createdAt, final long id, final InetAddress address, final UUID cookie, final Long userId, final int processed) {
        super(createdAt, id);
        this.address = address;
        this.cookie = cookie;
        this.userId = userId;
        this.processed = processed;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public UUID getCookie() {
        return this.cookie;
    }

    public Long getUserId() {
        return this.userId;
    }

    public int getProcessed() {
        return this.processed;
    }

    @Override
    public String toString() {
        return "IndividualTable{" +
            "address=" + this.address +
            ", cookie='" + this.cookie + '\'' +
            ", userId=" + this.userId +
            ", processed=" + this.processed +
            '}';
    }
}
