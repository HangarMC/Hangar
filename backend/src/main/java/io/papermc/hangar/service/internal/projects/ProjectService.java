package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
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
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import java.io.IOException;
import java.util.Arrays;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
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
    private final TaskExecutor taskExecutor;

    @Autowired
    public ProjectService(final ProjectsDAO projectDAO, final HangarProjectsDAO hangarProjectsDAO, final ProjectVisibilityService projectVisibilityService, final OrganizationService organizationService, final ProjectPageService projectPageService, final PermissionService permissionService, final PinnedVersionService pinnedVersionService, final VersionsApiDAO versionsApiDAO, @Lazy final AvatarService avatarService, @Lazy final TaskExecutor taskScheduler) {
        this.projectsDAO = projectDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.permissionService = permissionService;
        this.pinnedVersionService = pinnedVersionService;
        this.versionsApiDAO = versionsApiDAO;
        this.avatarService = avatarService;
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
        // TODO All of this is dumb and needs to be redone into as little queries as possible
        final Long hangarUserId = this.getHangarUserId();
        final Project project = this.hangarProjectsDAO.getProject(projectTable.getId(), hangarUserId);
        if (project == null) {
            // some view hasn't updated yet
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Project is still creating...");
        }
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

        final CompletableFuture<List<JoinableMember<ProjectRoleTable>>> membersFuture = this.supply(() -> {
            return this.hangarProjectsDAO.getProjectMembers(projectId, hangarUserId, this.permissionService.getProjectPermissions(hangarUserId, projectId).has(Permission.EditProjectSettings));
        });

        final Map<Platform, Version> mainChannelVersions = new EnumMap<>(Platform.class);
        CompletableFuture<Void> mainChannelFuture = CompletableFuture.allOf(
            Arrays.stream(Platform.getValues())
                .map(platform -> CompletableFuture.runAsync(() -> {
                    final Version version = this.getLastVersion(projectTable.getProjectId(), platform);
                    if (version != null) {
                        mainChannelVersions.put(platform, version);
                    }
                }, taskExecutor))
                .toArray(CompletableFuture[]::new)
        );

        final HangarProject.HangarProjectInfo info = this.hangarProjectsDAO.getHangarProjectInfo(projectId);
        final Map<Long, HangarProjectPage> pages = this.projectPageService.getProjectPages(projectId);
        final CompletableFuture<List<HangarProject.PinnedVersion>> pinnedVersions = this.supply(() -> this.pinnedVersionService.getPinnedVersions(projectId));
        final ProjectPageTable projectPage = this.projectPageService.getProjectHomePage(projectId);

        mainChannelFuture.join();
        return new HangarProject(
            project,
            membersFuture.join(),
            lastVisibilityChangeComment,
            lastVisibilityChangeUserName,
            info,
            pages.values(),
            pinnedVersions.join(),
            mainChannelVersions,
            projectPage
        );
    }

    private <T> CompletableFuture<T> supply(final Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.taskExecutor);
    }

    /**
     * Returns the last release version for the given platform. If no release version exists, the last version of any channel will be returned.
     *
     * @param projectId project id
     * @param platform platform
     * @return the last version for the given platform, prioritizing release versions, or null if no version exists
     */
    public @Nullable Version getLastVersion(final long projectId, final Platform platform) {
        final Version version = this.getLastVersion(projectId, platform, this.config.channels().nameDefault());
        return version != null ? version : this.getLastVersion(projectId, platform, null);
    }

    private @Nullable Version getLastVersion(final long projectId, final Platform platform, final @Nullable String channel) {
        final Long lastVersion = this.versionsApiDAO.getLatestVersionId(projectId, channel, platform);
        return lastVersion == null ? null : this.versionsApiDAO.getVersion(lastVersion, false, null);
    }

    public void validateSettings(final ProjectSettingsForm settingsForm) {
        this.validateLinks(settingsForm.getSettings().getLinks());

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

    public void saveSettings(final ProjectTable projectTable, final ProjectSettingsForm settingsForm) {
        this.validateSettings(settingsForm);

        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setTags(new LinkedHashSet<>(settingsForm.getSettings().getTags()));
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        projectTable.setLinks(new JSONB(settingsForm.getSettings().getLinks()));
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
        this.projectsDAO.update(projectTable);

        // TODO what settings changed
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

    public void changeAvatar(final ProjectTable table, final byte[] avatar) throws IOException {
        this.avatarService.changeProjectAvatar(table.getProjectId(), avatar);
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()), Base64.getEncoder().encodeToString(avatar), "#unknown"));
    }

    public void deleteAvatar(final ProjectTable table) {
        this.avatarService.deleteProjectAvatar(table.getProjectId());
        this.actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(table.getId()), "#empty", "#unknown"));
    }

    public List<UserTable> getProjectWatchers(final long projectId) {
        return this.projectsDAO.getProjectWatchers(projectId);
    }

    private @Nullable <T> ProjectTable getProjectTable(final @Nullable T identifier, final @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return this.projectVisibilityService.checkVisibility(projectTableFunction.apply(identifier));
    }
}
