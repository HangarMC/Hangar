package io.papermc.hangar.model.db;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Model;

import java.time.OffsetDateTime;

public abstract class Table extends Model implements Identified {

    protected final long id;

    // for creating rows that are not in the table yet
    protected Table() {
        this(-1);
    }

    // for creating fake users
    protected Table(long id) {
        super(null);
        this.id = id;
    }

    // for JDBI
    protected Table(OffsetDateTime createdAt, long id) {
        super(createdAt);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }
}
