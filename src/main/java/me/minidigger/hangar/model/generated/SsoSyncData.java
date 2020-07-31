package me.minidigger.hangar.model.generated;

// TODO: use this
public class SsoSyncData {

    private int externalId;
    private String email;
    private String username;
    private String name;
    private String addGroups; // todo: this is a comma-separated list of global roles to add on sync, map these to Roles
    private String removeGroups; // todo: same but remove
    private boolean admin;
    private boolean moderator;
    private boolean requireActivation;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddGroups() {
        return addGroups;
    }

    public void setAddGroups(String addGroups) {
        this.addGroups = addGroups;
    }

    public String getRemoveGroups() {
        return removeGroups;
    }

    public void setRemoveGroups(String removeGroups) {
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
