package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.customtypes.RoleCategory;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.service.MarkdownService;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectData {

    private ProjectsTable joinable;
    private UsersTable projectOwner;
    private int publicVersions;
    private Map<ProjectMember, UsersTable> members;
    private List<ProjectFlag> flags;
    private int noteCount;
    private ProjectVisibilityChangesTable lastVisibilityChange;
    private String lastVisibilityChangeUser;
    private ProjectVersionsTable recommendedVersion;
    private String iconUrl;
    private long starCount;
    private long watcherCount;
    private String namespace;
    private ProjectViewSettings settings;

    public ProjectData() {
        //
    }

    public ProjectData(ProjectsTable joinable, UsersTable projectOwner, int publicVersions, Map<ProjectMember, UsersTable> members, List<ProjectFlag> flags, int noteCount, ProjectVisibilityChangesTable lastVisibilityChange, String lastVisibilityChangeUser, ProjectVersionsTable recommendedVersion, String iconUrl, long starCount, long watcherCount, ProjectViewSettings settings) {
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
        namespace = projectOwner.getName() + "/" + joinable.getSlug();
        this.settings = settings;
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

    public Map<ProjectMember, UsersTable> getMembers() {
        return members;
    }

    public List<ProjectFlag> getFlags() {
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

    public String getNamespace() {
        return namespace;
    }

    public ProjectViewSettings getSettings() {
        return settings;
    }

    public Map<ProjectMember, UsersTable> filteredMembers(HeaderData headerData) {
        boolean hasEditMembers = headerData.globalPerm(Permission.ManageSubjectMembers);
        boolean userIsOwner = headerData.isAuthenticated() && headerData.getCurrentUser().getId() == projectOwner.getId();
        if (hasEditMembers || userIsOwner) return members;
        else return members.entrySet().stream().filter(member -> member.getKey().getIsAccepted()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public String renderVisibilityChange(MarkdownService markdownService, String fallback) {
        if (lastVisibilityChange != null) {
            if (!lastVisibilityChange.getComment().isBlank()) {
                return markdownService.render(lastVisibilityChange.getComment());
            }
        }
        return fallback;
    }
}
