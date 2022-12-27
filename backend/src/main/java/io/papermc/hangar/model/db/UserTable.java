package io.papermc.hangar.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

public class UserTable extends Table implements ProjectOwner {

    private UUID uuid;
    private String name;
    private String email;
    private String tagline;
    private OffsetDateTime joinDate;
    private List<Integer> readPrompts;
    private boolean locked;
    private String language;
    private String theme;

    @JdbiConstructor
    public UserTable(final OffsetDateTime createdAt, @PropagateNull final long id, final UUID uuid, final String name, final String email, final String tagline, final OffsetDateTime joinDate, final List<Integer> readPrompts, final boolean locked, final String language, final String theme) {
        super(createdAt, id);
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.readPrompts = readPrompts;
        this.locked = locked;
        this.language = language;
        this.theme = theme;
    }

    public UserTable(final long id, final UUID uuid, final String name, final String email, final List<Integer> readPrompts, final boolean locked, final String language, final String theme) {
        super(id);
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.readPrompts = readPrompts;
        this.locked = locked;
        this.language = language;
        this.theme = theme;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public @Nullable String getTagline() {
        return this.tagline;
    }

    public void setTagline(final String tagline) {
        this.tagline = tagline;
    }

    public OffsetDateTime getJoinDate() {
        return this.joinDate;
    }

    public void setJoinDate(final OffsetDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public List<Integer> getReadPrompts() {
        return this.readPrompts;
    }

    public void setReadPrompts(final List<Integer> readPrompts) {
        this.readPrompts = readPrompts;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    @Override
    public long getUserId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "UserTable{" +
            ", uuid='" + this.uuid + '\'' +
            ", name='" + this.name + '\'' +
            ", email='" + this.email + '\'' +
            ", tagline='" + this.tagline + '\'' +
            ", joinDate=" + this.joinDate +
            ", readPrompts=" + this.readPrompts +
            ", locked=" + this.locked +
            ", language='" + this.language + '\'' +
            ", theme='" + this.theme + '\'' +
            "} " + super.toString();
    }
}
