package io.papermc.hangar.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
    public UserTable(OffsetDateTime createdAt, @PropagateNull long id, UUID uuid, String name, String email, String tagline, OffsetDateTime joinDate, List<Integer> readPrompts, boolean locked, String language, String theme) {
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

    public UserTable(long id, UUID uuid, String name, String email, List<Integer> readPrompts, boolean locked, String language, String theme) {
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
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(OffsetDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public List<Integer> getReadPrompts() {
        return readPrompts;
    }

    public void setReadPrompts(List<Integer> readPrompts) {
        this.readPrompts = readPrompts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public long getUserId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserTable{" +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", tagline='" + tagline + '\'' +
                ", joinDate=" + joinDate +
                ", readPrompts=" + readPrompts +
                ", locked=" + locked +
                ", language='" + language + '\'' +
                ", theme='" + theme + '\'' +
                "} " + super.toString();
    }
}
