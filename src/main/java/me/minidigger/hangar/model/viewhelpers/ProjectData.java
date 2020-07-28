package me.minidigger.hangar.model.viewhelpers;

import java.util.List;
import java.util.Map;

import me.minidigger.hangar.db.customtypes.RoleCategory;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Visibility;

public class ProjectData {

    private ProjectsTable joinable;
    private UsersTable projectOwner;
    private int publicVersions;
    private Map<UsersTable, UserProjectRolesTable> members;
    private List<Object> flags; // TODO flags, flag, user.name, resolvedBy
    private int noteCount;
    private ProjectVisibilityChangesTable lastVisibilityChange;
    private String lastVisibilityChangeUser;
    private ProjectVersionsTable recommendedVersion;
    private String iconUrl;
    private long starCount;
    private long watcherCount;

    public ProjectData() {
        //
    }

    public ProjectData(ProjectsTable joinable, UsersTable projectOwner, int publicVersions, Map<UsersTable, UserProjectRolesTable> members, List<Object> flags, int noteCount, ProjectVisibilityChangesTable lastVisibilityChange, String lastVisibilityChangeUser, ProjectVersionsTable recommendedVersion, String iconUrl, long starCount, long watcherCount) {
        this.joinable = joinable;
        this.projectOwner = projectOwner;
        this.publicVersions = publicVersions;
        this.members = members;
        this.flags = flags;
        this.noteCount = noteCount;
        this.lastVisibilityChange = lastVisibilityChange;
        this.lastVisibilityChangeUser = lastVisibilityChangeUser;
        this.recommendedVersion = recommendedVersion;
        this.iconUrl = iconUrl;
        this.starCount = starCount;
        this.watcherCount = watcherCount;
    }

    public int getFlagCount() {
        return flags.size();
    }

    public ProjectsTable getProject() {
        return joinable;
    }

    public boolean isOwner(UsersTable usersTable) {
        return projectOwner.getId() == usersTable.getId();
    }

    public Visibility getVisibility() {
        return getProject().getVisibility();
    }

    public String getFullSlug() {
        return "/" + getProject().getOwnerName() + "/" + getProject().getSlug();
    }

    public RoleCategory getRoleCategory() {
        return RoleCategory.PROJECT;
    }

    public ProjectsTable getJoinable() {
        return joinable;
    }

    public UsersTable getProjectOwner() {
        return projectOwner;
    }

    public int getPublicVersions() {
        return publicVersions;
    }

    public Map<UsersTable, UserProjectRolesTable> getMembers() {
        return members;
    }

    public List<Object> getFlags() {
        return flags;
    }

    public int getNoteCount() {
        return noteCount;
    }

    public ProjectVisibilityChangesTable getLastVisibilityChange() {
        return lastVisibilityChange;
    }

    public String getLastVisibilityChangeUser() {
        return lastVisibilityChangeUser;
    }

    public ProjectVersionsTable getRecommendedVersion() {
        return recommendedVersion;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public long getStarCount() {
        return starCount;
    }

    public long getWatcherCount() {
        return watcherCount;
    }
}
