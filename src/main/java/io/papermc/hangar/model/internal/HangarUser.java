package io.papermc.hangar.model.internal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.modelold.Role;

import java.time.OffsetDateTime;
import java.util.List;

public class HangarUser extends User implements Identified {

    private final long id;

    public HangarUser(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<Role> roles, long id) {
        super(createdAt, name, tagline, joinDate, roles);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public User toUser() {
        return new User(
                this.getCreatedAt(),
                this.getName(),
                this.getTagline(),
                this.getJoinDate(),
                this.getRoles()
        );
    }
}
