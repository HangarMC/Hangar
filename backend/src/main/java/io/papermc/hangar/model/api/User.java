package io.papermc.hangar.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.roles.GlobalRole;
import jakarta.annotation.Nullable;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class User extends Model implements Named, Identified {

    private final long id;
    private final String name;
    private final String tagline;
    private final List<Long> roles;
    private final long projectCount;
    private final boolean isOrganization;
    private final boolean locked;
    private List<UserNameChange> nameHistory;
    private String avatarUrl;
    private final JSONB socials;

    @JdbiConstructor
    public User(final OffsetDateTime createdAt, final long id, final String name, final String tagline, final List<Long> roles, final long projectCount, final boolean locked, @Nullable final List<UserNameChange> nameHistory, final JSONB socials) {
        super(createdAt);
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.roles = roles;
        this.projectCount = projectCount;
        this.isOrganization = roles.contains(GlobalRole.ORGANIZATION.getRoleId());
        this.locked = locked;
        this.nameHistory = nameHistory;
        this.socials = socials;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getTagline() {
        return this.tagline;
    }

    public List<Long> getRoles() {
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

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public JSONB getSocials() {
        return socials;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final User user = (User) o;
        return this.projectCount == user.projectCount && this.isOrganization == user.isOrganization && this.locked == user.locked && this.name.equals(user.name) && Objects.equals(this.tagline, user.tagline) && this.roles.equals(user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.name, this.tagline, this.roles, this.projectCount, this.isOrganization, this.locked);
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + this.name + '\'' +
            ", tagline='" + this.tagline + '\'' +
            ", roles=" + this.roles +
            ", projectCount=" + this.projectCount +
            ", isOrganization=" + this.isOrganization +
            ", locked=" + this.locked +
            "} " + super.toString();
    }
}
