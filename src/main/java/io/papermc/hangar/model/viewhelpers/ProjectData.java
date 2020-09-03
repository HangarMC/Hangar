package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectVisibilityChangesTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.service.MarkdownService;

import java.util.List;
import java.util.Map;

public class ProjectData extends JoinableData<UserProjectRolesTable, ProjectsTable> {

    private final UsersTable projectOwner;
    private final int publicVersions;
    private final List<ProjectFlag> flags;
    private final int noteCount;
    private final ProjectVisibilityChangesTable lastVisibilityChange;
    private final String lastVisibilityChangeUser;
    private final ProjectVersionsTable recommendedVersion;
    private final String iconUrl;
    private final long starCount;
    private final long watcherCount;
    private final String namespace;
    private final ProjectViewSettings settings;


    public ProjectData(ProjectsTable joinable, UsersTable projectOwner, int publicVersions, Map<UserProjectRolesTable, UsersTable> members, List<ProjectFlag> flags, int noteCount, ProjectVisibilityChangesTable lastVisibilityChange, String lastVisibilityChangeUser, ProjectVersionsTable recommendedVersion, String iconUrl, long starCount, long watcherCount, ProjectViewSettings settings) {
        super(joinable, projectOwner.getId(), members, RoleCategory.PROJECT);
        this.projectOwner = projectOwner;
        this.publicVersions = publicVersions;
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
        return this.joinable;
    }

    public boolean isOwner(UsersTable usersTable) {
        if (usersTable == null) return false;
        return projectOwner.getId() == usersTable.getId();
    }

    public Visibility getVisibility() {
        return getProject().getVisibility();
    }

    public String getFullSlug() {
        return "/" + getProject().getOwnerName() + "/" + getProject().getSlug();
    }

    public UsersTable getProjectOwner() {
        return projectOwner;
    }

    public int getPublicVersions() {
        return publicVersions;
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

    public String renderVisibilityChange(MarkdownService markdownService, String fallback) {
        if (lastVisibilityChange != null) {
            if (!lastVisibilityChange.getComment().isBlank()) {
                return markdownService.render(lastVisibilityChange.getComment());
            }
        }
        return fallback;
    }
}
