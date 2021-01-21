package io.papermc.hangar.model.db;

import io.papermc.hangar.model.IDed;
import io.papermc.hangar.model.Model;

import java.time.OffsetDateTime;

public abstract class DBModel extends Model implements IDed {

    protected final long id;

    // For creating users that match hangar-auths id
    public DBModel(long id) {
        super(null);
        this.id = id;
    }
    public DBModel(OffsetDateTime createdAt, long id) {
        super(createdAt);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }
}
