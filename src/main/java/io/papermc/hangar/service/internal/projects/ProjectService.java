package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProject.HangarProjectInfo;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.FileUtils;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class ProjectService extends HangarService {

    private final ProjectsDAO projectsDAO;
    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final OrganizationService organizationService;
    private final ProjectPageService projectPageService;
    private final ProjectFiles projectFiles;
    private final ProjectInviteService projectInviteService;
    private final ProjectMemberService projectMemberService;
    private final ProjectRoleService projectRoleService;
    private final PermissionService permissionService;

    @Autowired
    public ProjectService(HangarDao<ProjectsDAO> projectDAO, HangarDao<UserDAO> userDAO, HangarDao<HangarUsersDAO> hangarUsersDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO, ProjectVisibilityService projectVisibilityService, OrganizationService organizationService, ProjectPageService projectPageService, ProjectFiles projectFiles, NotificationService notificationService, ProjectInviteService projectInviteService, ProjectMemberService projectMemberService, ProjectRoleService projectRoleService, PermissionService permissionService) {
        this.projectsDAO = projectDAO.get();
        this.userDAO = userDAO.get();
        this.hangarUsersDAO = hangarUsersDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.projectFiles = projectFiles;
        this.projectInviteService = projectInviteService;
        this.projectMemberService = projectMemberService;
        this.projectRoleService = projectRoleService;
        this.permissionService = permissionService;
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
        var members = hangarProjectsDAO.getProjectMembers(project.getLeft(), getHangarUserId(), permissionService.getProjectPermissions(getHangarUserId(), project.getLeft()).has(Permission.EditProjectSettings));
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
        return new HangarProject(project.getRight(), project.getLeft(), projectOwner, members, lastVisibilityChangeComment, lastVisibilityChangeUserName, info, pages.values());
    }

    public void saveSettings(String author, String slug, ProjectSettingsForm settingsForm) {
        ProjectTable projectTable = getProjectTable(author, slug);
        projectTable.setCategory(settingsForm.getCategory());
        projectTable.setKeywords(settingsForm.getSettings().getKeywords());
        projectTable.setHomepage(settingsForm.getSettings().getHomepage());
        projectTable.setIssues(settingsForm.getSettings().getIssues());
        projectTable.setSource(settingsForm.getSettings().getSource());
        projectTable.setSupport(settingsForm.getSettings().getSupport());
        projectTable.setLicenseName(settingsForm.getSettings().getLicense().getName());
        projectTable.setLicenseUrl(settingsForm.getSettings().getLicense().getUrl());
        projectTable.setForumSync(settingsForm.getSettings().isForumSync());
        projectTable.setDescription(settingsForm.getDescription());
        projectsDAO.update(projectTable);
        refreshHomeProjects();
        // TODO what settings changed
        userActionLogService.project(LogAction.PROJECT_SETTINGS_CHANGED.create(ProjectContext.of(projectTable.getId()), "", ""));
    }

    public void saveIcon(String author, String slug, MultipartFile icon) {
        ProjectTable projectTable = getProjectTable(author, slug);
        if (icon.getContentType() == null || (!icon.getContentType().equals(MediaType.IMAGE_PNG_VALUE) && !icon.getContentType().equals(MediaType.IMAGE_JPEG_VALUE))) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "project.settings.error.invalidFile", icon.getContentType());
        }
        if (icon.getOriginalFilename() == null || icon.getOriginalFilename().isBlank()) {
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
            userActionLogService.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(projectTable.getId()), newBase64, oldBase64));
        } catch (IOException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void resetIcon(String author, String slug) {
        ProjectTable projectTable = getProjectTable(author, slug);
        String base64 = getBase64(author, slug, "old", projectFiles.getIconPath(author, slug));
        if (FileUtils.delete(projectFiles.getIconPath(author, slug))) {
            userActionLogService.project(LogAction.PROJECT_ICON_CHANGED.create(ProjectContext.of(projectTable.getId()), "#empty", base64));
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

    public void editMembers(String author, String slug, EditMembersForm<ProjectRole> editMembersForm) {
        ProjectTable projectTable = getProjectTable(author, slug);
        List<HangarApiException> errors = new ArrayList<>();

        projectInviteService.sendInvites(errors, editMembersForm.getNewInvitees(), projectTable);
        projectMemberService.editMembers(errors, editMembersForm.getEditedMembers(), projectTable);
        projectMemberService.removeMembers(errors, editMembersForm.getDeletedMembers(), projectTable);

        if (!errors.isEmpty()) {
            throw new MultiHangarApiException(errors);
        }
    }

    private void handleEditOrDelete(Member<ProjectRole> member, List<HangarApiException> errors, ProjectTable projectTable, BiConsumer<Member<ProjectRole>, ProjectRoleTable> consumer) {
        UserTable userTable = userDAO.getUserTable(member.getName());
        if (userTable == null) {
            errors.add(new HangarApiException("project.settings.error.members.invalidUser", member.getName()));
            return;
        }
        ProjectRoleTable projectRoleTable = projectRoleService.getRole(projectTable.getId(), userTable.getId());
        if (projectRoleTable == null) {
            errors.add(new HangarApiException("project.settings.error.members.notMember", member.getName()));
            return;
        }
        if (projectRoleTable.getRole() == ProjectRole.PROJECT_OWNER) {
            errors.add(new HangarApiException("project.settings.error.members.invalidRole", projectRoleTable.getRole().getTitle()));
            return;
        }
        consumer.accept(member, projectRoleTable);
    }

    public void refreshHomeProjects() {
        hangarProjectsDAO.refreshHomeProjects();
    }

    public List<UserTable> getProjectWatchers(long projectId) {
        return projectsDAO.getProjectWatchers(projectId);
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
