package io.papermc.hangar.model.internal;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.projects.ProjectVisibilityChangeTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.user.JoinableMember;

import java.util.List;

public class HangarProject extends ProjectTable implements Joinable<ProjectRoleTable> {

    private final ProjectOwner owner;
    private final List<JoinableMember<ProjectRoleTable>> members;
    private final int publicVersions;
    private final List<HangarProjectFlag> flags;
    private final int noteCount;
    private final ProjectVisibilityChangeTable lastVisibilityChange;
    private final String lastVisibilityChangeUsername;
    private final ProjectVersionTable recommendedVersion;
    private final String iconUrl;
    private final long starCount;
    private final long watcherCount;
    private final String namespace;
    
    public HangarProject(ProjectTable projectTable, ProjectOwner owner, List<JoinableMember<ProjectRoleTable>> members, int publicVersions, List<HangarProjectFlag> flags, int noteCount, ProjectVisibilityChangeTable lastVisibilityChange, String lastVisibilityChangeUsername, ProjectVersionTable recommendedVersion, String iconUrl, long starCount, long watcherCount) {
        super(projectTable);
        this.owner = owner;
        this.members = members;
        this.publicVersions = publicVersions;
        this.flags = flags;
        this.noteCount = noteCount;
        this.lastVisibilityChange = lastVisibilityChange;
        this.lastVisibilityChangeUsername = lastVisibilityChangeUsername;
        this.recommendedVersion = recommendedVersion;
        this.iconUrl = iconUrl;
        this.starCount = starCount;
        this.watcherCount = watcherCount;
        this.namespace = owner.getName() + "/" + projectTable.getSlug();
    }

    @Override
    public ProjectOwner getOwner() {
        return owner;
    }

    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.PROJECT;
    }

    @Override
    public List<JoinableMember<ProjectRoleTable>> getMembers() {
        return members;
    }

    public int getPublicVersions() {
        return publicVersions;
    }

    public List<HangarProjectFlag> getFlags() {
        return flags;
    }

    public int getNoteCount() {
        return noteCount;
    }

    public ProjectVisibilityChangeTable getLastVisibilityChange() {
        return lastVisibilityChange;
    }

    public String getLastVisibilityChangeUsername() {
        return lastVisibilityChangeUsername;
    }

    public ProjectVersionTable getRecommendedVersion() {
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

    public String getNamespace() {
        return namespace;
    }
}
