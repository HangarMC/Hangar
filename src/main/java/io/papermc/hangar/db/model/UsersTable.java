package io.papermc.hangar.db.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdbi.v3.core.annotation.Unmappable;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.StringJoiner;

public class UsersTable implements Serializable {

    private long id;
    private OffsetDateTime createdAt;
    private String fullName;
    private String name;
    private String email;
    private String tagline;
    private OffsetDateTime joinDate;
    private List<Integer> readPrompts;
    private boolean isLocked;
    private String language;


    public UsersTable() {

    }

    public UsersTable(long id, String fullName, String name, String email, String tagline, List<Integer> readPrompts, boolean isLocked, String language) {
        this.id = id;
        this.fullName = fullName;
        this.name = name;
        this.email = email;
        this.tagline = tagline;
        this.readPrompts = readPrompts;
        this.isLocked = isLocked;
        this.language = language;
    }

    public UsersTable(String fullName, String name, String email, String tagline, List<Integer> readPrompts, boolean isLocked, String language) {
        this.fullName = fullName;
        this.name = name;
        this.email = email;
        this.tagline = tagline;
        this.readPrompts = readPrompts;
        this.isLocked = isLocked;
        this.language = language;
    }

    @JsonIgnore
    @Unmappable
    public UsersTable getUser() {
        return this; // TODO dummy to fix frontend
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


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

    public boolean getIsLocked() {
        return isLocked;
    } // Apparently the userDao didn't like not having this?

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UsersTable.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("fullName='" + fullName + "'")
                .add("name='" + name + "'")
                .add("email='" + email + "'")
                .add("tagline='" + tagline + "'")
                .add("joinDate=" + joinDate)
                .add("readPrompts=" + readPrompts)
                .add("isLocked=" + isLocked)
                .add("language='" + language + "'")
                .toString();
    }
}
