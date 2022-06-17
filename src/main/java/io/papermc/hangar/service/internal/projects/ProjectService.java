package io.papermc.hangar.service.internal.projects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProject.HangarProjectInfo;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.versions.RecommendedVersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.FileUtils;

@Service
public class ProjectService extends HangarComponent {

    private final ProjectsDAO projectsDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final OrganizationService organizationService;
    private final ProjectPageService projectPageService;
    private final ProjectFiles projectFiles;
    private final PermissionService permissionService;
    private final RecommendedVersionService recommendedVersionService;

    @Autowired
    public ProjectService(ProjectsDAO projectDAO, HangarUsersDAO hangarUsersDAO, HangarProjectsDAO hangarProjectsDAO, ProjectVisibilityService projectVisibilityService, OrganizationService organizationService, ProjectPageService projectPageService, ProjectFiles projectFiles, PermissionService permissionService, RecommendedVersionService recommendedVersionService) {
        this.projectsDAO = projectDAO;
        this.hangarUsersDAO = hangarUsersDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.projectFiles = projectFiles;
        this.permissionService = permissionService;
        this.recommendedVersionService = recommendedVersionService;
    }

    @Nullable
    public ProjectTable getProjectTable(@Nullable Long projectId) {
        return getProjectTable(projectId, projectsDAO::getById);
    }

    public ProjectTable getProjectTable(@Nullable String author, @Nullable String slug) {
        return getProjectTable(author, slug, projectsDAO::getBySlug);
    }

    @Nullable
    public ProjectOwner getProjectOwner(long userId) {
        if (Objects.equals(getHangarUserId(), userId)) {
            return getHangarPrincipal();
        }
        return organizationService.getOrganizationTablesWithPermission(getHangarPrincipal().getId(), Permission.CreateProject).stream().filter(ot -> ot.getUserId() == userId).findFirst().orElse(null);
    }

    public ProjectOwner getProjectOwner(String userName) {
        Pair<UserTable, OrganizationTable> pair = hangarUsersDAO.getUserAndOrg(userName);
        if (pair.getRight() != null) {
            return pair.getRight();
        }
        return pair.getLeft();
    }

    public HangarProject getHangarProject(String author, String slug) {
        Pair<Long, Project> project = hangarProjectsDAO.getProject(author, slug, getHangarUserId());
        ProjectOwner projectOwner = getProjectOwner(author);
        List<JoinableMember<ProjectRoleTable>> members = hangarProjectsDAO.getProjectMembers(project.getLeft(), getHangarUserId(), permissionService.getProjectPermissions(getHangarUserId(), project.getLeft()).has(Permission.EditProjectSettings));
        String lastVisibilityChangeComment = "";
        String lastVisibilityChangeUserName = "";
        if (project.getRight().getVisibility() == Visibility.NEEDSCHANGES || project.getRight().getVisibility() == Visibility.SOFTDELETE) {
            var projectVisibilityChangeTable = projectVisibilityService.getLastVisibilityChange(project.getLeft());
            lastVisibilityChangeComment = projectVisibilityChangeTable.getValue().getComment();
            if (project.getRight().getVisibility() == Visibility.SOFTDELETE) {
                lastVisibilityChangeUserName = projectVisibilityChangeTable.getKey();
            }
        }
        HangarProjectInfo info = hangarProjectsDAO.getHangarProjectInfo(project.getLeft());
        Map<Long, HangarProjectPage> pages = projectPageService.getProjectPages(project.getLeft());
        Map<Platform, String> recommendedVersions = recommendedVersionService.getRecommendedVersions(project.getLeft());
        return new HangarProject(project.getRight(), project.getLeft(), projectOwner, members, lastVisibilityChangeComment, lastVisibilityChangeUserName, info, pages.values(), recommendedVersions);
    }

    public void saveSettings(String author, String slug, ProjectSettingsForm settingsForm) {
        ProjectTable projectTable = getProjectTable(author, slug);
        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        projectTable.setHomepage(settingsForm.getSettings().getHomepage());
        projectTable.setIssues(settingsForm.getSettings().getIssues());
        projectTable.setSource(settingsForm.getSettings().getSource());
        projectTable.setSupport(settingsForm.getSettings().getSupport());
        String licenseName = org.apache.commons.lang3.StringUtils.stripToNull(settingsForm.getSettings().getLicense().getName());
        if (licenseName == null) {
            licenseName = settingsForm.getSettings().getLicense().getType();
        }
        projectTable.setLicenseName(licenseName);
        projectTable.setLicenseUrl(settingsForm.getSettings().getLicense().getUrl());
        projectTable.setForumSync(settingsForm.getSettings().isForumSync());
        projectTable.setDescription(settingsForm.getDescription());
        projectTable.setDonationEnabled(settingsForm.getSettings().getDonation().isEnable());
        projectTable.setDonationSubject(settingsForm.getSettings().getDonation().getSubject());
        projectsDAO.update(projectTable);
        refreshHomeProjects();
        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    public void saveSponsors(String author, String slug, StringContent content) {
        ProjectTable projectTable = getProjectTable(author, slug);
        projectTable.setSponsors(content.getContent());
        projectsDAO.update(projectTable);
        // TODO what settings changed
        projectTable.logAction(this.actionLogger, LogAction.PROJECT_SETTINGS_CHANGED, "", "");
    }

    public void saveDiscourseData(ProjectTable projectTable, long topicId, long postId) {
        projectTable.setTopicId(topicId);
        projectTable.setPostId(postId);
        projectsDAO.update(projectTable);
    }

    public void saveIcon(String author, String slug, MultipartFile icon) {
        ProjectTable projectTable = getProjectTable(author, slug);
        if (!StringUtils.equalsAny(icon.getContentType(), MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.error.invalidFile", icon.getContentType());
        }
        if (StringUtils.isBlank(icon.getOriginalFilename())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.error.noFile");
        }
        try {
            Path iconDir = projectFiles.getIconDir(author, slug);
            String oldBase64 = getBase64(author, slug, "old", projectFiles.getIconPath(author, slug));
            if (Files.notExists(iconDir)) {
                Files.createDirectories(iconDir);
            }
            FileUtils.deletedFiles(iconDir);
            Files.copy(icon.getInputStream(), iconDir.resolve(icon.getOriginalFilename()));
            String newBase64 = getBase64(author, slug, "new", iconDir.resolve(icon.getOriginalFilename()));
            actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(projectTable.getId()), newBase64, oldBase64));
        } catch (IOException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void resetIcon(String author, String slug) {
        ProjectTable projectTable = getProjectTable(author, slug);
        String base64 = getBase64(author, slug, "old", projectFiles.getIconPath(author, slug));
        if (FileUtils.delete(projectFiles.getIconPath(author, slug))) {
            actionLogger.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(projectTable.getId()), "#empty", base64));
        }
    }

    private String getBase64(String author, String slug, String old, Path path) {
        String base64 = "#empty";
        if (path == null || !Files.exists(path)) {
            return base64;
        }
        try {
            base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.warn("Error while loading {} icon for project {}/{}: {}:{}", old, author, slug, e.getClass().getSimpleName(), e.getMessage());
        }
        return base64;
    }

    public List<UserTable> getProjectWatchers(long projectId) {
        return projectsDAO.getProjectWatchers(projectId);
    }

    public void refreshHomeProjects() {
        hangarProjectsDAO.refreshHomeProjects();
    }

    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifier, @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return projectVisibilityService.checkVisibility(projectTableFunction.apply(identifier));
    }

    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifierOne, @Nullable T identifierTwo, @NotNull BiFunction<T, T, ProjectTable> projectTableFunction) {
        if (identifierOne == null || identifierTwo == null) {
            return null;
        }
        return projectVisibilityService.checkVisibility(projectTableFunction.apply(identifierOne, identifierTwo));
    }
}
