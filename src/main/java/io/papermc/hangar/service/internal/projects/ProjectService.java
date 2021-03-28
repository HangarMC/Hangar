package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
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
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProject.HangarProjectInfo;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.roles.MemberService.ProjectMemberService;
import io.papermc.hangar.service.internal.roles.RoleService.ProjectRoleService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.users.NotificationService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class ProjectService extends HangarService {

    public static final String AUTHOR = "author";
    public static final String SLUG = "slug";

    private final ProjectsDAO projectsDAO;
    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final OrganizationService organizationService;
    private final ProjectPageService projectPageService;
    private final ProjectFiles projectFiles;
    private final NotificationService notificationService;
    private final ProjectMemberService projectMemberService;
    private final ProjectRoleService projectRoleService;
    private final PermissionService permissionService;

    @Autowired
    public ProjectService(HangarDao<ProjectsDAO> projectDAO, HangarDao<UserDAO> userDAO, HangarDao<HangarUsersDAO> hangarUsersDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO, ProjectVisibilityService projectVisibilityService, OrganizationService organizationService, ProjectPageService projectPageService, ProjectFiles projectFiles, NotificationService notificationService, ProjectMemberService projectMemberService, ProjectRoleService projectRoleService, PermissionService permissionService) {
        this.projectsDAO = projectDAO.get();
        this.userDAO = userDAO.get();
        this.hangarUsersDAO = hangarUsersDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
        this.projectVisibilityService = projectVisibilityService;
        this.organizationService = organizationService;
        this.projectPageService = projectPageService;
        this.projectFiles = projectFiles;
        this.notificationService = notificationService;
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
        userActionLogService.project(LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(projectTable.getId())), "", "");
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
            if (Files.notExists(iconDir)) {
                Files.createDirectories(iconDir);
            }
            FileUtils.deletedFiles(iconDir);
            Files.copy(icon.getInputStream(), iconDir.resolve(icon.getOriginalFilename()));
            // TODO store old images in log somehow?
            userActionLogService.project(LoggedActionType.PROJECT_ICON_CHANGED.with(ProjectContext.of(projectTable.getId())), "", "");
        } catch (IOException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void resetIcon(String author, String slug) {
        ProjectTable projectTable = getProjectTable(author, slug);
        if (FileUtils.delete(projectFiles.getIconPath(author, slug))) {
            // TODO store old images in log somehow?
            userActionLogService.project(LoggedActionType.PROJECT_ICON_CHANGED.with(ProjectContext.of(projectTable.getId())), "", "");
        }
    }

    public void editMembers(String author, String slug, EditMembersForm<ProjectRole> editMembersForm) {
        ProjectTable projectTable = getProjectTable(author, slug);
        List<HangarApiException> errors = new ArrayList<>();
        editMembersForm.getNewMembers().forEach(member -> {
            UserTable userTable = userDAO.getUserTable(member.getName());
            if (userTable == null) {
                errors.add(new HangarApiException("project.settings.error.members.invalidUser", member.getName()));
                return;
            }
            if (projectMemberService.addMember(projectTable.getId(), member.getRole().create(projectTable.getId(), userTable.getId(), false)) == null) {
                errors.add(new HangarApiException("project.settings.error.members.alreadyMember", member.getName()));
                return;
            }
            notificationService.notifyNewProjectMember(member, userTable.getId(), projectTable);
        });

        editMembersForm.getEditedMembers().forEach(member -> {
            handleEditOrDelete(member, errors, projectTable, (pm, prt) -> {
                prt.setRole(pm.getRole());
                projectRoleService.updateRole(prt);
                // TODO notification of updated role
            });
        });

        editMembersForm.getDeletedMembers().forEach(member -> {
            handleEditOrDelete(member, errors, projectTable, (pm, prt) -> {
                projectMemberService.removeMember(prt);
                // TODO notification of removed role (if not accepted and prev notification is unread, remove that notif?)
            });
        });

        if (!errors.isEmpty()) {
            throw new MultiHangarApiException(errors);
        }
        // TODO user action logging
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
            errors.add(new HangarApiException("project.settings.error.members.isOwner"));
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

    public void sendProjectForApproval(long projectId) {
        ProjectTable projectTable = getProjectTable(projectId);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (projectTable.getVisibility() != Visibility.NEEDSCHANGES) {
            throw new HangarApiException();
        }
        projectVisibilityService.changeVisibility(projectTable, Visibility.NEEDSAPPROVAL, "");
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
