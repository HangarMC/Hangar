package me.minidigger.hangar.model.viewhelpers;

import java.util.ArrayList;
import java.util.List;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;

public class UserData {

    private HeaderData headerData;
    private UsersTable user;
    private boolean isOrga;
    private int projectCount;
    private List<Organization> orgas;
    private List<Role> globalRoles;
    private Permission userPerm;
    private Permission orgaPerm;

    public static UserData of(UsersTable user, HeaderData headerData) {
        return new UserData(headerData, user, false, 0, new ArrayList<>(), new ArrayList<>(), Permission.None, Permission.None);
    }

    public UserData(HeaderData headerData, UsersTable user, boolean isOrga, int projectCount, List<Organization> orgas, List<Role> globalRoles, Permission userPerm, Permission orgaPerm) {
        this.headerData = headerData;
        this.user = user;
        this.isOrga = isOrga;
        this.projectCount = projectCount;
        this.orgas = orgas;
        this.globalRoles = globalRoles;
        this.userPerm = userPerm;
        this.orgaPerm = orgaPerm;
    }

    public boolean hasUser() {
        return headerData.hasUser();
    }

    public UsersTable currentUser() {
        return headerData.getCurrentUser();
    }

    public boolean isCurrent() {
        return headerData.isCurrentUser(user);
    }

    public HeaderData getHeaderData() {
        return headerData;
    }

    public void setHeaderData(HeaderData headerData) {
        this.headerData = headerData;
    }

    public UsersTable getUser() {
        return user;
    }

    public void setUser(UsersTable user) {
        this.user = user;
    }

    public boolean isOrga() {
        return isOrga;
    }

    public void setOrga(boolean orga) {
        isOrga = orga;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public List<Organization> getOrgas() {
        return orgas;
    }

    public void setOrgas(List<Organization> orgas) {
        this.orgas = orgas;
    }

    public List<Role> getGlobalRoles() {
        return globalRoles;
    }

    public void setGlobalRoles(List<Role> globalRoles) {
        this.globalRoles = globalRoles;
    }

    public Permission getUserPerm() {
        return userPerm;
    }

    public void setUserPerm(Permission userPerm) {
        this.userPerm = userPerm;
    }

    public Permission getOrgaPerm() {
        return orgaPerm;
    }

    public void setOrgaPerm(Permission orgaPerm) {
        this.orgaPerm = orgaPerm;
    }
}
