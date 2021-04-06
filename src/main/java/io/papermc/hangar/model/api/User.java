package io.papermc.hangar.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class User extends Model implements Named {

    private final String name;
    private final String tagline;
    private final OffsetDateTime joinDate;
    private final List<GlobalRole> roles;
    private final long projectCount;
    private final boolean isOrganization;
    private final boolean locked;

    @JdbiConstructor
    public User(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<GlobalRole> roles, long projectCount, boolean locked) {
        super(createdAt);
        this.name = name;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.roles = roles;
        this.projectCount = projectCount;
        this.isOrganization = roles.contains(GlobalRole.ORGANIZATION);
        this.locked = locked;
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

    @JsonProperty("isOrganization")
    public boolean isOrganization() {
        return isOrganization;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return projectCount == user.projectCount && isOrganization == user.isOrganization && locked == user.locked && name.equals(user.name) && Objects.equals(tagline, user.tagline) && joinDate.equals(user.joinDate) && roles.equals(user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, tagline, joinDate, roles, projectCount, isOrganization, locked);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", tagline='" + tagline + '\'' +
                ", joinDate=" + joinDate +
                ", roles=" + roles +
                ", projectCount=" + projectCount +
                ", isOrganization=" + isOrganization +
                ", locked=" + locked +
                "} " + super.toString();
    }
}
