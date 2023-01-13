package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.AvatarService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.versions.DownloadService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService extends HangarComponent {

    private final ProjectsDAO projectsDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final OrganizationService organizationService;
    private final ProjectPageService projectPageService;
    private final PermissionService permissionService;
    private final PinnedVersionService pinnedVersionService;
    private final VersionsApiDAO versionsApiDAO;
    private final HangarVersionsDAO hangarVersionsDAO;
    private final DownloadService downloadService;
    private final AvatarService avatarService;

    @Autowired
    public ProjectService(final ProjectsDAO projectDAO, final HangarUsersDAO hangarUsersDAO, final HangarProjectsDAO hangarProjectsDAO, final ProjectVisibilityService projectVisibilityService, final OrganizationService organizationService, final ProjectPageService projectPageService, final PermissionService permissionService, final PinnedVersionService pinnedVersionService, final VersionsApiDAO versionsApiDAO, final HangarVersionsDAO hangarVersionsDAO, final DownloadService downloadService, final AvatarService avatarService) {
        this.projectsDAO = projectDAO;
        this.hangarUsersDAO = hangarUsersDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.permissionService = permissionService;
        this.pinnedVersionService = pinnedVersionService;
        this.versionsApiDAO = versionsApiDAO;
        this.hangarVersionsDAO = hangarVersionsDAO;
        this.downloadService = downloadService;
        this.avatarService = avatarService;
    }

    public @Nullable ProjectTable getProjectTable(final @Nullable Long projectId) {
        return this.getProjectTable(projectId, this.projectsDAO::getById);
    }

    public ProjectTable getProjectTable(final @Nullable String author, final @Nullable String slug) {
        return this.getProjectTable(author, slug, this.projectsDAO::getBySlug);
    }

    public @Nullable ProjectOwner getProjectOwner(final long userId) {
        if (Objects.equals(this.getHangarUserId(), userId)) {
            return this.getHangarPrincipal();
        }
        return this.organizationService.getOrganizationTablesWithPermission(this.getHangarPrincipal().getId(), Permission.CreateProject).stream().filter(ot -> ot.getUserId() == userId).findFirst().orElse(null);
    }

    public ProjectOwner getProjectOwner(final String userName) {
        final Pair<UserTable, OrganizationTable> pair = this.hangarUsersDAO.getUserAndOrg(userName);
        if (pair.getRight() != null) {
            return pair.getRight();
        }
        return pair.getLeft();
    }

    public HangarProject getHangarProject(final String author, final String slug) {
        final Pair<Long, Project> project = this.hangarProjectsDAO.getProject(author, slug, this.getHangarUserId());
        final ProjectOwner projectOwner = this.getProjectOwner(author);
        final List<JoinableMember<ProjectRoleTable>> members = this.hangarProjectsDAO.getProjectMembers(project.getLeft(), this.getHangarUserId(), this.permissionService.getProjectPermissions(this.getHangarUserId(), project.getLeft()).has(Permission.EditProjectSettings));
        members.forEach((member) -> member.setAvatarUrl(this.avatarService.getUserAvatarUrl(member.getUser())));
        String lastVisibilityChangeComment = "";
        String lastVisibilityChangeUserName = "";
        if (project.getRight().getVisibility() == Visibility.NEEDSCHANGES || project.getRight().getVisibility() == Visibility.SOFTDELETE) {
            final var projectVisibilityChangeTable = this.projectVisibilityService.getLastVisibilityChange(project.getLeft());
            lastVisibilityChangeComment = projectVisibilityChangeTable.getValue().getComment();
            if (project.getRight().getVisibility() == Visibility.SOFTDELETE) {
                lastVisibilityChangeUserName = projectVisibilityChangeTable.getKey();
            }
        }
        final HangarProject.HangarProjectInfo info = this.hangarProjectsDAO.getHangarProjectInfo(project.getLeft());
        final Map<Long, HangarProjectPage> pages = this.projectPageService.getProjectPages(project.getLeft());
        final List<HangarProject.PinnedVersion> pinnedVersions = this.pinnedVersionService.getPinnedVersions(project.getRight().getNamespace().getOwner(), project.getRight().getNamespace().getSlug(), project.getLeft());

        final Map<Platform, HangarVersion> mainChannelVersions = new EnumMap<>(Platform.class);
        for (final Platform platform : Platform.getValues()) {
            final HangarVersion version = this.getLastVersion(author, slug, platform, this.config.channels.nameDefault());
            if (version != null) {
                if (version.getPlatformDependencies().isEmpty()) {
                    final Map<Platform, SortedSet<String>> platformDependencies = this.versionsApiDAO.getPlatformDependencies(version.getId());
                    version.getPlatformDependencies().putAll(platformDependencies);
                    for (final Map.Entry<Platform, SortedSet<String>> entry : platformDependencies.entrySet()) {
                        version.getPlatformDependenciesFormatted().put(entry.getKey(), io.papermc.hangar.util.StringUtils.formatVersionNumbers(new ArrayList<>(entry.getValue())));
                    }
                    this.downloadService.addDownloads(project.getRight().getNamespace().getOwner(), project.getRight().getNamespace().getSlug(), version.getName(), version.getId(), version.getDownloads());
                }

                mainChannelVersions.put(platform, version);
            }
        }

        final HangarProject hangarProject = new HangarProject(project.getRight(), projectOwner, members, lastVisibilityChangeComment, lastVisibilityChangeUserName, info, pages.values(), pinnedVersions, mainChannelVersions);
        hangarProject.setAvatarUrl(this.avatarService.getProjectAvatarUrl(hangarProject.getProjectId(), hangarProject.getNamespace().getOwner()));
        return hangarProject;
    }

    public @Nullable HangarVersion getLastVersion(final String author, final String slug, final Platform platform, final @Nullable String channel) {
        final RequestPagination pagination = new RequestPagination(1L, 0L);
        pagination.getFilters().put("platform", new VersionPlatformFilter.VersionPlatformFilterInstance(new Platform[]{platform}));
        if (channel != null) {
            // Find the last version with the specified channel
            pagination.getFilters().put("channel", new VersionChannelFilter.VersionChannelFilterInstance(new String[]{channel}));
        }

        final Long versionId = this.versionsApiDAO.getVersions(author, slug, false, this.getHangarUserId(), pagination).keySet().stream().findAny().orElse(null);
        if (versionId != null) {
            return this.hangarVersionsDAO.getVersion(versionId, this.getGlobalPermissions().has(Permission.SeeHidden), this.getHangarUserId());
        }

        // Try again with any channel, else empty
        return channel != null ? this.getLastVersion(author, slug, platform, null) : null;
    }

    public void saveSettings(final String author, final String slug, final ProjectSettingsForm settingsForm) {
        final ProjectTable projectTable = this.getProjectTable(author, slug);
        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        projectTable.setHomepage(settingsForm.getSettings().getHomepage());
        projectTable.setIssues(settingsForm.getSettings().getIssues());
        projectTable.setSource(settingsForm.getSettings().getSource());
        projectTable.setSupport(settingsForm.getSettings().getSupport());
        projectTable.setWiki(settingsForm.getSettings().getWiki());
        String licenseName = org.apache.commons.lang3.StringUtils.stripToNull(settingsForm.getSettings().getLicense().getName());
        if (licenseName == null) {
            licenseName = settingsForm.getSettings().getLicense().getType();
        }
        projectTable.setLicenseType(settingsForm.getSettings().getLicense().getType());
        projectTable.setLicenseName(licenseName);
        projectTable.setLicenseUrl(settingsForm.getSettings().getLicense().getUrl());
        projectTable.setForumSync(settingsForm.getSettings().isForumSync());
        projectTable.setDescription(settingsForm.getDescription());
        projectTable.setDonationEnabled(settingsForm.getSettings().getDonation().isEnable());
        projectTable.setDonationSubject(settingsForm.getSettings().getDonation().getSubject());
        this.projectsDAO.update(projectTable);
        this.refreshHomeProjects();
        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    @Transactional
    public void saveSponsors(final String author, final String slug, final String content) {
        final ProjectTable projectTable = this.getProjectTable(author, slug);
        projectTable.setSponsors(content);
        this.projectsDAO.update(projectTable);
        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    public void saveDiscourseData(final ProjectTable projectTable, final long topicId, final long postId) {
        projectTable.setTopicId(topicId);
        projectTable.setPostId(postId);
        this.projectsDAO.update(projectTable);
    }

    public void changeAvatar(final String author, final String slug, final byte[] avatar) {
        final ProjectTable table = this.getProjectTable(author, slug);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown project " + author + "/" + slug);
        }
        this.avatarService.changeProjectAvatar(table.getProjectId(), avatar);
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()),  Base64.getEncoder().encodeToString(avatar), "#unknown"));
    }

    public void deleteAvatar(final String author, final String slug) {
        final ProjectTable table = this.getProjectTable(author, slug);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown project " + author + "/" + slug);
        }
        this.avatarService.deleteProjectAvatar(table.getProjectId());
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()), "#empty", "#unknown"));
    }

    public List<UserTable> getProjectWatchers(final long projectId) {
        return this.projectsDAO.getProjectWatchers(projectId);
    }

    @Async
    public void refreshHomeProjects() {
        this.hangarProjectsDAO.refreshHomeProjects();
    }

    private @Nullable <T> ProjectTable getProjectTable(final @Nullable T identifier, final @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return this.projectVisibilityService.checkVisibility(projectTableFunction.apply(identifier));
    }

    private @Nullable <T> ProjectTable getProjectTable(final @Nullable T identifierOne, final @Nullable T identifierTwo, final @NotNull BiFunction<T, T, ProjectTable> projectTableFunction) {
        if (identifierOne == null || identifierTwo == null) {
            return null;
        }
        return this.projectVisibilityService.checkVisibility(projectTableFunction.apply(identifierOne, identifierTwo));
    }
}
