package io.papermc.hangar.model.internal;

import io.papermc.hangar.model.IDed;
import io.papermc.hangar.modelold.Role;

import java.time.OffsetDateTime;
import java.util.List;

public class User extends io.papermc.hangar.model.api.User implements IDed {

    private final long id;

    public User(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<Role> roles, long id) {
        super(createdAt, name, tagline, joinDate, roles);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }
}
