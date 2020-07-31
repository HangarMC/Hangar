package me.minidigger.hangar.model.generated;

public class SsoSyncData {

    private int externalId;
    private String email;
    private String username;
    private String name;
    private String addGroups; // todo: this is a comma-separated list of global roles, map these to Roles

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
}
