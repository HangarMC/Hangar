package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.db.Table;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public abstract class IndividualTable extends Table {

    private final InetAddress address;
    private final String cookie;
    private final Long userId;
    private final int processed;

    protected IndividualTable(InetAddress address, String cookie, Long userId) {
        this.address = address;
        this.cookie = cookie;
        this.userId = userId;
        this.processed = 0;
    }

    protected IndividualTable(OffsetDateTime createdAt, long id, InetAddress address, String cookie, Long userId, int processed) {
        super(createdAt, id);
        this.address = address;
        this.cookie = cookie;
        this.userId = userId;
        this.processed = processed;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getCookie() {
        return cookie;
    }

    public Long getUserId() {
        return userId;
    }

    public int getProcessed() {
        return processed;
    }

    @Override
    public String toString() {
        return "IndividualTable{" +
                "address=" + address +
                ", cookie='" + cookie + '\'' +
                ", userId=" + userId +
                ", processed=" + processed +
                '}';
    }
}
