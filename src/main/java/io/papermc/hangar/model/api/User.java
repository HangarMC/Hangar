package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class User extends Model implements Named {

    private final String name;
    private final String tagline;
    private final OffsetDateTime joinDate;
    private final List<GlobalRole> roles;
    private final long projectCount;

    @JdbiConstructor
    public User(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<GlobalRole> roles, long projectCount) {
        super(createdAt);
        this.name = name;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.roles = roles;
        this.projectCount = projectCount;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getTagline() {
        return tagline;
    }

    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public List<GlobalRole> getRoles() {
        return roles;
    }

    public long getProjectCount() {
        return projectCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", tagline='" + tagline + '\'' +
                ", joinDate=" + joinDate +
                ", roles=" + roles +
                ", projectCount=" + projectCount +
                "} " + super.toString();
    }
}
