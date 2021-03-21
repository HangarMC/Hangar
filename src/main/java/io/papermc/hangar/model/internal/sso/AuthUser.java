package io.papermc.hangar.model.internal.sso;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthUser {
    private final long id;
    private final String userName;
    private final String email;
    private final String avatarUrl;
    private final Locale lang;
    private final List<GlobalRole> globalRoles;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AuthUser(long id, String userName, String email, String avatarUrl, Locale lang, String addGroups) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.lang = lang;
        if (addGroups == null || addGroups.isBlank()) {
            this.globalRoles = new ArrayList<>();
        } else {
            this.globalRoles = parseRoles(addGroups.trim().split(","));
        }
    }

    public AuthUser(String userName, String email) {
        this.id = -100;
        this.userName = userName;
        this.email = email;
        this.avatarUrl = "";
        this.lang = Locale.ENGLISH;
        this.globalRoles = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Locale getLang() {
        return lang;
    }

    public List<GlobalRole> getGlobalRoles() {
        return globalRoles;
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", lang=" + lang +
                ", globalRoles=" + globalRoles +
                '}';
    }

    public static List<GlobalRole> parseRoles(String[] roleNames) {
        return Stream.of(roleNames).map(Role.VALUE_ROLES::get).filter(Objects::nonNull).filter(GlobalRole.class::isInstance).map(GlobalRole.class::cast).collect(Collectors.toList());
    }
}
