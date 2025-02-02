package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter;
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
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
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
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public ProjectService(final ProjectsDAO projectDAO, final HangarProjectsDAO hangarProjectsDAO, final ProjectVisibilityService projectVisibilityService, final OrganizationService organizationService, final ProjectPageService projectPageService, final PermissionService permissionService, final PinnedVersionService pinnedVersionService, final VersionsApiDAO versionsApiDAO, @Lazy final AvatarService avatarService, @Lazy final VersionDependencyService versionDependencyService) {
        this.projectsDAO = projectDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.permissionService = permissionService;
        this.pinnedVersionService = pinnedVersionService;
        this.versionsApiDAO = versionsApiDAO;
        this.avatarService = avatarService;
        this.versionDependencyService = versionDependencyService;
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

    public String getProjectUrlFromSlug(final ProjectTable project) {
        return "/" + project.getOwnerName() + "/" + project.getSlug();
    }

    public HangarProject getHangarProject(final ProjectTable projectTable) {
        // TODO All of this is dumb and needs to be redone into as little queries as possible
        final Long hangarUserId = this.getHangarUserId();
        final Project project = this.hangarProjectsDAO.getProject(projectTable.getId(), hangarUserId);
        final long projectId = project.getId();
        final String ownerName = project.getNamespace().getOwner();

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
        final CompletableFuture<Void> mainChannelFuture = CompletableFuture.runAsync(() -> Arrays.stream(Platform.getValues()).parallel().forEach(platform -> {
            final Version version = this.getLastVersion(projectTable.getProjectId(), platform, this.config.channels.nameDefault());
            if (version != null) {
                this.versionDependencyService.addDownloadsAndDependencies(ownerName, projectTable.getSlug(), version.getName(), version.getId()).applyTo(version);
                mainChannelVersions.put(platform, version);
            }
        }));

        final HangarProject.HangarProjectInfo info = this.hangarProjectsDAO.getHangarProjectInfo(projectId);
        final Map<Long, HangarProjectPage> pages = this.projectPageService.getProjectPages(projectId);
        final CompletableFuture<List<HangarProject.PinnedVersion>> pinnedVersions = this.supply(() -> this.pinnedVersionService.getPinnedVersions(ownerName, project.getNamespace().getSlug(), projectId));
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectHomePage(projectId);

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
        return CompletableFuture.supplyAsync(supplier);
    }

    public @Nullable Version getLastVersion(final long projectId, final Platform platform, final @Nullable String channel) {
        final RequestPagination pagination = new RequestPagination(1L, 0L);
        pagination.getFilters().put("platform", new VersionPlatformFilter.VersionPlatformFilterInstance(new Platform[]{platform}));
        if (channel != null) {
            // Find the last version with the specified channel
            pagination.getFilters().put("channel", new VersionChannelFilter.VersionChannelFilterInstance(new String[]{channel}));
        }

        final Long userId = this.getHangarUserId();
        final SortedMap<Long, Version> versions = this.versionsApiDAO.getVersions(projectId, this.getGlobalPermissions().has(Permission.SeeHidden), userId, pagination);
        if (!versions.isEmpty()) {
            return versions.values().iterator().next();
        }

        // Try again with any channel, else empty
        return channel != null ? this.getLastVersion(projectId, platform, null) : null;
    }

    public void validateSettings(final ProjectSettingsForm settingsForm) {
        this.validateLinks(settingsForm.getSettings().getLinks());

        for (final String keyword : settingsForm.getSettings().getKeywords()) {
            if (keyword.length() < 3) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.keywordTooShort", keyword);
            } else if (keyword.length() > this.config.projects.maxKeywordLen()) {
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

        // final boolean requiresHomepageUpdate = !projectTable.getKeywords().equals(settingsForm.getSettings().getKeywords())
        //    || !projectTable.getDescription().equals(settingsForm.getDescription());

        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setTags(new LinkedHashSet<>(settingsForm.getSettings().getTags()));
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        projectTable.setLinks(new JSONB(settingsForm.getSettings().getLinks()));
        String licenseName = org.apache.commons.lang3.StringUtils.stripToNull(settingsForm.getSettings().getLicense().getName());
        if (licenseName == null) {
            licenseName = settingsForm.getSettings().getLicense().getType();
        }
        projectTable.setLicenseType(settingsForm.getSettings().getLicense().getType());
        projectTable.setLicenseName(licenseName);
        projectTable.setLicenseUrl(settingsForm.getSettings().getLicense().getUrl());
        projectTable.setDescription(settingsForm.getDescription());
        projectTable.setDonationEnabled(settingsForm.getSettings().getDonation().isEnable());
        projectTable.setDonationSubject(settingsForm.getSettings().getDonation().getSubject());
        this.projectsDAO.update(projectTable);

        /*if (requiresHomepageUpdate) {
            this.refreshHomeProjects();
        }*/

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
        if (trimmedContent.length() > this.config.projects.maxSponsorsLen()) {
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

    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void refreshHomeProjects() {
        this.hangarProjectsDAO.refreshHomeProjects();
    }

    private @Nullable <T> ProjectTable getProjectTable(final @Nullable T identifier, final @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return this.projectVisibilityService.checkVisibility(projectTableFunction.apply(identifier));
    }
}
