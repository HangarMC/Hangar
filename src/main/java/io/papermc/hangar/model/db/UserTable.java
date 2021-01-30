package io.papermc.hangar.model.db;

import io.papermc.hangar.model.db.projects.ProjectOwner;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class UserTable extends Table implements ProjectOwner {

    private String fullName;
    private String name;
    private String email;
    private String tagline;
    private OffsetDateTime joinDate;
    private List<Integer> readPrompts;
    private boolean isLocked;
    private String language;

    @JdbiConstructor
    public UserTable(@PropagateNull long id, String fullName, String name, String email, String tagline, OffsetDateTime joinDate, List<Integer> readPrompts, boolean isLocked, String language) {
        super(id);
        this.fullName = fullName;
        this.name = name;
        this.email = email;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.readPrompts = readPrompts;
        this.isLocked = isLocked;
        this.language = language;
    }

    public UserTable(long id, String fullName, String name, String email, List<Integer> readPrompts, boolean isLocked, String language) {
        super(id);
        this.fullName = fullName;
        this.name = name;
        this.email = email;
        this.readPrompts = readPrompts;
        this.isLocked = isLocked;
        this.language = language;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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
        return isLocked;
    }

    public boolean getIsLocked() { return isLocked; } // JDBI needs this annoyingly

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public long getUserId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserTable{" +
                "fullName='" + fullName + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", tagline='" + tagline + '\'' +
                ", joinDate=" + joinDate +
                ", readPrompts=" + readPrompts +
                ", isLocked=" + isLocked +
                ", language='" + language + '\'' +
                "} " + super.toString();
    }
}
