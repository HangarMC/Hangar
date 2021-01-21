package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.internal.HangarUser;
import io.papermc.hangar.modelold.Role;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class User extends Model implements Named {

    private String name;
    private String tagline;
    private OffsetDateTime joinDate;
    private List<Role> roles = new ArrayList<>(); // TODO new Role model

    public User(OffsetDateTime createdAt, String name, String tagline, OffsetDateTime joinDate, List<Role> roles) {
        super(createdAt);
        this.name = name;
        this.tagline = tagline;
        this.joinDate = joinDate;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
