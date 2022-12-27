package io.papermc.hangar.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class User extends Model implements Named {

    private final String name;
    private final String tagline;
    private final OffsetDateTime joinDate;
    private final List<GlobalRole> roles;
    private final long projectCount;
    private final boolean isOrganization;
    private final boolean locked;
    private List<UserNameChange> nameHistory;

    @JdbiConstructor
    public User(final OffsetDateTime createdAt, final String name, final String tagline, final OffsetDateTime joinDate, final List<GlobalRole> roles, final long projectCount, final boolean locked, @Nullable final List<UserNameChange> nameHistory) {
        super(createdAt);
        this.name = name;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.roles = roles;
        this.projectCount = projectCount;
        this.isOrganization = roles.contains(GlobalRole.ORGANIZATION);
        this.locked = locked;
        this.nameHistory = nameHistory;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getTagline() {
        return this.tagline;
    }

    public OffsetDateTime getJoinDate() {
        return this.joinDate;
    }

    public List<GlobalRole> getRoles() {
        return this.roles;
    }

    public long getProjectCount() {
        return this.projectCount;
    }

    @JsonProperty("isOrganization")
    public boolean isOrganization() {
        return this.isOrganization;
    }

    public boolean isLocked() {
        return this.locked;
    }

    @Nullable
    public List<UserNameChange> getNameHistory() {
        return this.nameHistory;
    }

    public void setNameHistory(final List<UserNameChange> nameHistory) {
        this.nameHistory = nameHistory;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final User user = (User) o;
        return this.projectCount == user.projectCount && this.isOrganization == user.isOrganization && this.locked == user.locked && this.name.equals(user.name) && Objects.equals(this.tagline, user.tagline) && this.joinDate.equals(user.joinDate) && this.roles.equals(user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.name, this.tagline, this.joinDate, this.roles, this.projectCount, this.isOrganization, this.locked);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + this.name + '\'' +
                ", tagline='" + this.tagline + '\'' +
                ", joinDate=" + this.joinDate +
                ", roles=" + this.roles +
                ", projectCount=" + this.projectCount +
                ", isOrganization=" + this.isOrganization +
                ", locked=" + this.locked +
                "} " + super.toString();
    }
}
