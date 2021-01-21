package io.papermc.hangar.service.sso;

import io.papermc.hangar.modelold.Role;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuthUser {
    private final long id;
    private final String username;
    private final String email;
    private final String avatarUrl;
    private final Locale lang;
    private final String addGroups;

    @JsonCreator
    public AuthUser(long id, String username, String email, @JsonProperty("avatar_url") String avatarUrl, Locale lang, String addGroups) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.lang = lang;
        this.addGroups = addGroups;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Locale getLang() {
        return lang == null ? Locale.ENGLISH : lang;
    }

    public String getAddGroups() {
        return addGroups;
    }

    public List<Role> getGlobalRoles() {
        if (addGroups == null || addGroups.isBlank()) return new ArrayList<>();
        return Arrays.stream(addGroups.trim().split(",")).map(Role::fromValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", lang=" + lang +
                ", addGroups='" + addGroups + '\'' +
                '}';
    }
}
