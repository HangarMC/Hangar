package io.papermc.hangar.model;

import io.papermc.hangar.util.SsoUtil;

import java.util.Map;

public class SsoSyncData {

    private int externalId;
    private String email;
    private String username;
    private String fullName;
    private String[] addGroups; // todo: this is a comma-separated list of global roles the user has
    private String[] removeGroups; // todo: list of global roles the user *doesn't* have
    private boolean admin;
    private boolean moderator;
    private boolean requireActivation;

    public SsoSyncData(int externalId, String email, String username, String fullName, String[] addGroups, String[] removeGroups, boolean admin, boolean moderator, boolean requireActivation) {
        this.externalId = externalId;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.addGroups = addGroups;
        this.removeGroups = removeGroups;
        this.admin = admin;
        this.moderator = moderator;
        this.requireActivation = requireActivation;
    }

    public static SsoSyncData fromSignedPayload(Map<String, String> payload) {
        return new SsoSyncData(
                Integer.parseInt(payload.get("external_id")),
                payload.get("email"),
                payload.get("username"),
                SsoUtil.parsePythonNullable(payload.get("name")),
                payload.get("add_groups").split(","),
                payload.get("remove_groups").split(","),
                Boolean.parseBoolean(payload.get("admin")),
                Boolean.parseBoolean(payload.get("moderator")),
                Boolean.parseBoolean(payload.get("require_activation"))
        );
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String[] getAddGroups() {
        return addGroups;
    }

    public void setAddGroups(String[] addGroups) {
        this.addGroups = addGroups;
    }

    public String[] getRemoveGroups() {
        return removeGroups;
    }

    public void setRemoveGroups(String[] removeGroups) {
        this.removeGroups = removeGroups;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isModerator() {
        return moderator;
    }

    public void setModerator(boolean moderator) {
        this.moderator = moderator;
    }

    public boolean isRequireActivation() {
        return requireActivation;
    }

    public void setRequireActivation(boolean requireActivation) {
        this.requireActivation = requireActivation;
    }
}
