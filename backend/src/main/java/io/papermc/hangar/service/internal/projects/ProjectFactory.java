package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.StringUtils;
import java.util.Set;
import org.jdbi.v3.core.enums.EnumByName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectFactory extends HangarComponent {

    public static final String SOFT_DELETION_SUFFIX = "-del_";
    private final ProjectsDAO projectsDAO;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final ProjectPageService projectPageService;
    private final ProjectMemberService projectMemberService;
    private final ProjectVisibilityService projectVisibilityService;
    private final UsersApiService usersApiService;
    private final ProjectFiles projectFiles;
    private final ValidationService validationService;
    private final FileService fileService;
    private final AvatarService avatarService;

    @Autowired
    public ProjectFactory(final ProjectsDAO projectDAO, final ProjectService projectService, final ChannelService channelService, final ProjectPageService projectPageService, final ProjectMemberService projectMemberService, final ProjectVisibilityService projectVisibilityService, final UsersApiService usersApiService, final ProjectFiles projectFiles, final ValidationService validationService, final FileService fileService, final AvatarService avatarService) {
        this.projectsDAO = projectDAO;
        this.projectService = projectService;
        this.channelService = channelService;
        this.projectPageService = projectPageService;
        this.projectMemberService = projectMemberService;
        this.projectVisibilityService = projectVisibilityService;
        this.usersApiService = usersApiService;
        this.projectFiles = projectFiles;
        this.validationService = validationService;
        this.fileService = fileService;
        this.avatarService = avatarService;
    }

    @Transactional
    public ProjectTable createProject(final NewProjectForm newProject) {
        final ProjectOwner projectOwner = this.projectService.getProjectOwner(newProject.getOwnerId());
        if (projectOwner == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.project.ownerNotFound");
        }

        this.checkProjectAvailability(newProject.getName());
        this.projectService.validateSettings(newProject);

        ProjectTable projectTable = null;
        try {
            projectTable = this.projectsDAO.insert(new ProjectTable(projectOwner, newProject));
            this.channelService.createProjectChannel(this.config.channels.nameDefault(), this.config.channels.descriptionDefault(), this.config.channels.colorDefault(), projectTable.getId(), Set.of(ChannelFlag.FROZEN, ChannelFlag.PINNED, ChannelFlag.SENDS_NOTIFICATIONS));
            this.projectMemberService.addNewAcceptedByDefaultMember(ProjectRole.PROJECT_OWNER.create(projectTable.getId(), null, projectOwner.getUserId(), true));
            String newPageContent = newProject.getPageContent();
            if (newPageContent == null) {
                newPageContent = "# " + projectTable.getName() + "\n\n" + this.config.pages.home().message();
            }

            final String defaultName = this.config.pages.home().name();
            this.projectPageService.createPage(projectTable.getId(), defaultName, StringUtils.slugify(defaultName), newPageContent, false, null, true);
            if (newProject.getAvatarUrl() != null) {
                this.avatarService.importProjectAvatar(projectTable.getId(), newProject.getAvatarUrl());
            }
        } catch (final Exception exception) {
            if (projectTable != null) {
                this.projectsDAO.delete(projectTable);
            }
            throw exception;
        }

        this.usersApiService.clearAuthorsCache();
        return projectTable;
    }

    public String renameProject(final String slug, final String newName) {
        return this.renameProject(slug, newName, false);
    }

    @Transactional
    public String renameProject(final String slug, final String newName, final boolean skipNameCheck) {
        final String compactNewName = StringUtils.compact(newName);
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.checkProjectAvailability(compactNewName, skipNameCheck);

        final String oldName = projectTable.getName();
        final String oldSlug = projectTable.getSlug();
        final String newSlug = StringUtils.slugify(compactNewName);
        projectTable.setName(compactNewName);
        projectTable.setSlug(newSlug);
        this.projectsDAO.update(projectTable);
        this.actionLogger.project(LogAction.PROJECT_RENAMED.create(ProjectContext.of(projectTable.getId()), compactNewName, oldName));
        this.projectFiles.renameProject(projectTable.getOwnerName(), oldSlug, newSlug);
        return newSlug;
    }

    public void checkProjectAvailability(final String name) {
        this.checkProjectAvailability(name, false);
    }

    public void checkProjectAvailability(final String name, final boolean skipNameChecking) {
        String errorKey = null;
        if (!skipNameChecking) {
            errorKey = this.validationService.isValidProjectName(name);
        }
        if (errorKey == null) {
            final InvalidProjectReason reason = this.projectsDAO.checkProjectValidity(name, StringUtils.slugify(name));
            if (reason != null) {
                errorKey = reason.key;
            }
        }
        if (errorKey != null) {
            throw new HangarApiException(HttpStatus.CONFLICT, errorKey);
        }
    }

    @Transactional
    public void softDelete(final ProjectTable projectTable, final String comment) {
        if (comment.length() > 300) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Comment is too long");
        }

        if (projectTable.getVisibility() == Visibility.NEW) {
            this.hardDelete(projectTable, comment);
        } else if (projectTable.getVisibility() != Visibility.SOFTDELETE) {
            // Append deletion suffix to allow creation of new projects under the old namespace
            int deletedId = -1;
            for (int i = 0; i < 10; i++) {
                if (this.projectsDAO.getBySlug(projectTable.getSlug() + SOFT_DELETION_SUFFIX + i) == null) {
                    deletedId = i;
                    break;
                }
            }

            if (deletedId == -1) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "Project has been deleted too often");
            }

            final String newName = projectTable.getName() + SOFT_DELETION_SUFFIX + deletedId;
            this.renameProject(projectTable.getSlug(), newName, true);
            projectTable.setName(newName);
            projectTable.setSlug(StringUtils.slugify(newName));
            this.projectVisibilityService.changeVisibility(projectTable, Visibility.SOFTDELETE, comment);
        }
    }

    @Transactional
    public void hardDelete(final ProjectTable projectTable, final String comment) {
        if (comment.length() > 300) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Comment is too long");
        }

        this.actionLogger.project(LogAction.PROJECT_VISIBILITY_CHANGED.create(ProjectContext.of(projectTable.getId()), "Deleted: " + comment, projectTable.getVisibility().getTitle()));
        this.projectsDAO.delete(projectTable);
        this.projectService.refreshHomeProjects();
        this.fileService.deleteDirectory(this.projectFiles.getProjectDir(projectTable.getOwnerName(), projectTable.getSlug()));
    }

    @EnumByName
    public enum InvalidProjectReason {

        NAME_EXISTS("project.new.error.nameExists"),
        SLUG_EXISTS("project.new.error.slugExists"),
        INVALID_NAME("project.new.error.invalidName");

        private final String key;

        InvalidProjectReason(final String key) {
            this.key = key;
        }
    }
}
