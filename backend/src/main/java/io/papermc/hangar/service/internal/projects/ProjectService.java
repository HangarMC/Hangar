package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.components.index.IndexService;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.settings.LinkSection;
import io.papermc.hangar.model.api.project.settings.LinkSectionType;
import io.papermc.hangar.model.api.project.settings.Tag;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectLinksForm;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.ProjectData;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService extends HangarComponent {

    private static final Pattern KEYWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private final ProjectsDAO projectsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final OrganizationService organizationService;
    private final ProjectPageService projectPageService;
    private final PermissionService permissionService;
    private final PinnedVersionService pinnedVersionService;
    private final VersionsApiDAO versionsApiDAO;
    private final AvatarService avatarService;
    private final IndexService indexService;
    private final TaskExecutor taskExecutor;

    @Autowired
    public ProjectService(final ProjectsDAO projectDAO, final HangarProjectsDAO hangarProjectsDAO, final ProjectVisibilityService projectVisibilityService, final OrganizationService organizationService, final ProjectPageService projectPageService, final PermissionService permissionService, final PinnedVersionService pinnedVersionService, final VersionsApiDAO versionsApiDAO, @Lazy final AvatarService avatarService, @Lazy final IndexService indexService, @Lazy final TaskExecutor taskScheduler) {
        this.projectsDAO = projectDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.permissionService = permissionService;
        this.pinnedVersionService = pinnedVersionService;
        this.versionsApiDAO = versionsApiDAO;
        this.avatarService = avatarService;
        this.indexService = indexService;
        this.taskExecutor = taskScheduler;
    }

    public @Nullable ProjectTable getProjectTable(final @Nullable Long projectId) {
        return this.getProjectTable(projectId, this.projectsDAO::getById);
    }

    public ProjectTable getProjectTable(final String slug) {
        return this.getProjectTable(slug, this.projectsDAO::getBySlug);
    }

    public List<ProjectTable> getProjectTables(final long userId) {
        return this.projectsDAO.getUserProjects(userId, true);
    }

    public @Nullable ProjectOwner getProjectOwner(final long userId) {
        if (Objects.equals(this.getHangarUserId(), userId)) {
            return this.getHangarPrincipal();
        }
        return this.organizationService.getOrganizationTableWithPermission(this.getHangarPrincipal().getId(), userId, Permission.CreateProject);
    }

    public Long getProjectId(String slug) {
        return this.projectsDAO.getIdBySlug(slug);
    }

    public String getProjectUrlFromSlug(final ProjectTable project) {
        return "/" + project.getOwnerName() + "/" + project.getSlug();
    }

    public HangarProject getHangarProject(final ProjectTable projectTable) {
        // TODO Most of this is dumb and needs to be redone into as little queries as possible
        final Long hangarUserId = this.getHangarUserId();
        final ProjectData projectData = this.hangarProjectsDAO.getProject(projectTable.getId(), hangarUserId);
        if (projectData == null) {
            // some view hasn't updated yet
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Project is still being created...");
        }

        final Project project = projectData.project();
        final long projectId = project.getId();

        String lastVisibilityChangeComment = "";
        String lastVisibilityChangeUserName = "";
        if (project.getVisibility() == Visibility.NEEDSCHANGES || project.getVisibility() == Visibility.SOFTDELETE) {
            final var projectVisibilityChangeTable = this.projectVisibilityService.getLastVisibilityChange(projectId);
            lastVisibilityChangeComment = projectVisibilityChangeTable.getValue().getComment();
            if (project.getVisibility() == Visibility.SOFTDELETE) {
                lastVisibilityChangeUserName = projectVisibilityChangeTable.getKey();
            }
        }

        // resolved here rather than in the async task below: it reads the security context, which does not follow onto the executor's threads
        final boolean canSeePending = this.permissionService.getProjectPermissions(hangarUserId, projectId).has(Permission.EditProjectSettings);

        final CompletableFuture<Map<Platform, Version>> mainChannelVersions = this.supply(() -> this.getLastVersions(projectId));
        final CompletableFuture<List<JoinableMember<ProjectRoleTable>>> members = this.supply(() -> this.hangarProjectsDAO.getProjectMembers(projectId, hangarUserId, canSeePending));
        final CompletableFuture<List<HangarProject.PinnedVersion>> pinnedVersions = this.supply(() -> this.pinnedVersionService.getPinnedVersions(projectId));

        final ProjectPageService.Pages pages = this.projectPageService.getPages(projectId);

        return new HangarProject(
            project,
            members.join(),
            lastVisibilityChangeComment,
            lastVisibilityChangeUserName,
            projectData.info(),
            pages.pages().values(),
            pinnedVersions.join(),
            mainChannelVersions.join(),
            pages.homePage()
        );
    }

    private <T> CompletableFuture<T> supply(final Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.taskExecutor);
    }

    /**
     * Returns the last version per platform, prioritizing release versions over those of any other channel.
     *
     * @param projectId project id
     * @return the last version per platform, only containing platforms the project has a version for
     */
    public Map<Platform, Version> getLastVersions(final long projectId) {
        final Map<Platform, Long> versionIds = this.versionsApiDAO.getLatestVersionIds(projectId, this.config.channels().nameDefault());
        if (versionIds.isEmpty()) {
            return Map.of();
        }

        final Map<Long, Version> versions = this.versionsApiDAO.getVersions(Set.copyOf(versionIds.values()), false, null);
        final Map<Platform, Version> lastVersions = new EnumMap<>(Platform.class);
        versionIds.forEach((platform, versionId) -> {
            final Version version = versions.get(versionId);
            if (version != null) {
                lastVersions.put(platform, version);
            }
        });
        return lastVersions;
    }

    public void validateSettings(final ProjectSettingsForm settingsForm) {
        this.validateLinks(settingsForm.getSettings().getLinks());
        this.validateGeneralSettings(settingsForm);
    }

    private void validateGeneralSettings(final ProjectSettingsForm settingsForm) {
        for (final String keyword : settingsForm.getSettings().getKeywords()) {
            if (keyword.length() < 3) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.keywordTooShort", keyword);
            } else if (keyword.length() > this.config.projects().maxKeywordLen()) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.keywordTooLong", keyword);
            } else if (!KEYWORD_PATTERN.matcher(keyword).matches()) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.keywordInvalid", keyword);
            }
        }

        final Set<Tag> tags = new LinkedHashSet<>(settingsForm.getSettings().getTags());
        if (tags.stream().anyMatch(Objects::isNull)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.invalidTag");
        }
    }

    // links belong to saveLinks; they are neither validated nor written here so a broken link can't block a general settings save
    public void saveSettings(final ProjectTable projectTable, final ProjectSettingsForm settingsForm) {
        this.validateGeneralSettings(settingsForm);

        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setTags(new LinkedHashSet<>(settingsForm.getSettings().getTags()));
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        String licenseName = org.apache.commons.lang3.StringUtils.stripToNull(settingsForm.getSettings().getLicense().name());
        if (licenseName == null) {
            licenseName = settingsForm.getSettings().getLicense().type();
        }
        projectTable.setLicenseType(settingsForm.getSettings().getLicense().type());
        projectTable.setLicenseName(licenseName);
        projectTable.setLicenseUrl(settingsForm.getSettings().getLicense().url());
        projectTable.setDescription(settingsForm.getDescription());
        projectTable.setDonationEnabled(settingsForm.getSettings().getDonation().isEnable());
        projectTable.setDonationSubject(settingsForm.getSettings().getDonation().getSubject());
        projectTable.setUnlisted(settingsForm.getSettings().isUnlisted());
        this.projectsDAO.update(projectTable);
        this.indexService.updateProject(projectTable.getId());

        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    @Transactional
    public void saveLinks(final ProjectTable projectTable, final ProjectLinksForm linksForm) {
        this.validateLinks(linksForm.getLinks());

        projectTable.setLinks(new JSONB(linksForm.getLinks()));
        this.projectsDAO.updateLinks(projectTable.getId(), projectTable.getLinks());

        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    private void validateLinks(final List<LinkSection> sections) {
        int topSections = 0;
        for (final LinkSection section : sections) {
            final LinkSectionType type;
            try {
                type = LinkSectionType.valueOf(section.type().toUpperCase(Locale.ROOT));
            } catch (final IllegalArgumentException e) {
                throw new HangarApiException("Invalid link type " + section.type());
            }

            if (section.links().size() > type.maxLinks()) {
                throw new HangarApiException("Cannot have more than " + type.maxLinks() + " links in a " + type.name() + " section");
            }

            if (section.title() == null && type.hasTitle()) {
                throw new HangarApiException("Section " + type.name() + " must have a title");
            }

            if (type == LinkSectionType.TOP && ++topSections > 1) {
                throw new HangarApiException("Cannot have multiple top sections");
            }
        }
    }

    @Transactional
    public void saveSponsors(final ProjectTable projectTable, final @Nullable String content) {
        final String trimmedContent = content != null ? content.trim() : "";
        if (trimmedContent.length() > this.config.projects().maxSponsorsLen()) {
            throw new HangarApiException("page.new.error.maxLength");
        }

        projectTable.setSponsors(trimmedContent);
        this.projectsDAO.update(projectTable);
        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    public String changeAvatar(final ProjectTable table, final byte[] avatar) throws IOException {
        final String avatarUrl = this.avatarService.changeProjectAvatar(table.getProjectId(), avatar);
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()), Base64.getEncoder().encodeToString(avatar), "#unknown"));
        this.indexService.updateProject(table.getId());
        return avatarUrl;
    }

    public String deleteAvatar(final ProjectTable table) {
        this.avatarService.deleteProjectAvatar(table.getProjectId());
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()), "#empty", "#unknown"));
        this.indexService.updateProject(table.getId());
        return this.avatarService.getProjectAvatarUrl(table.getProjectId(), table.getOwnerName());
    }

    public List<UserTable> getProjectWatchers(final long projectId) {
        return this.projectsDAO.getProjectWatchers(projectId);
    }

    private @Nullable <T> ProjectTable getProjectTable(final @Nullable T identifier, final Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return this.projectVisibilityService.checkVisibility(projectTableFunction.apply(identifier));
    }
}
