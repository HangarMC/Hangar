package io.papermc.hangar.model.internal.sso;

import io.papermc.hangar.model.roles.GlobalRole;
import io.papermc.hangar.util.SsoUtil;

import java.util.List;
import java.util.Map;

public class SsoSyncData {

    private final int externalId;
    private final String email;
    private final String username;
    private final String fullName;
    private final List<GlobalRole> addGroups;
    private final List<GlobalRole> removeGroups;
    private final boolean admin;
    private final boolean moderator;
    private final boolean requireActivation;

    private SsoSyncData(int externalId, String email, String username, String fullName, String[] addGroups, String[] removeGroups, boolean admin, boolean moderator, boolean requireActivation) {
        this.externalId = externalId;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.addGroups = AuthUser.parseRoles(addGroups);
        this.removeGroups = AuthUser.parseRoles(removeGroups);
        this.admin = admin;
        this.moderator = moderator;
        this.requireActivation = requireActivation;
    }

    public int getExternalId() {
        return externalId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public List<GlobalRole> getAddGroups() {
        return addGroups;
    }

    public List<GlobalRole> getRemoveGroups() {
        return removeGroups;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isModerator() {
        return moderator;
    }

    public boolean isRequireActivation() {
        return requireActivation;
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

}
