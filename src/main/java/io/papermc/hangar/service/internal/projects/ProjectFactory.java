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
import io.papermc.hangar.model.internal.job.DeleteDiscourseTopicJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.FileUtils;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.enums.EnumByName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
    private final JobService jobService;
    private final ProjectFiles projectFiles;
    private final ValidationService validationService;

    @Autowired
    public ProjectFactory(final ProjectsDAO projectDAO, final ProjectService projectService, final ChannelService channelService, final ProjectPageService projectPageService, final ProjectMemberService projectMemberService, final ProjectVisibilityService projectVisibilityService, final UsersApiService usersApiService, final JobService jobService, final ProjectFiles projectFiles, final ValidationService validationService) {
        this.projectsDAO = projectDAO;
        this.projectService = projectService;
        this.channelService = channelService;
        this.projectPageService = projectPageService;
        this.projectMemberService = projectMemberService;
        this.projectVisibilityService = projectVisibilityService;
        this.usersApiService = usersApiService;
        this.jobService = jobService;
        this.projectFiles = projectFiles;
        this.validationService = validationService;
    }

    @Transactional
    public ProjectTable createProject(final NewProjectForm newProject) {
        ProjectOwner projectOwner = this.projectService.getProjectOwner(newProject.getOwnerId());
        if (projectOwner == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.project.ownerNotFound");
        }
        this.checkProjectAvailability(projectOwner.getUserId(), newProject.getName());
        ProjectTable projectTable = null;
        try {
            projectTable = this.projectsDAO.insert(new ProjectTable(projectOwner, newProject));
            this.channelService.createProjectChannel(this.config.channels.getNameDefault(), this.config.channels.getColorDefault(), projectTable.getId(), Set.of(ChannelFlag.FROZEN, ChannelFlag.PINNED));
            this.projectMemberService.addNewAcceptedByDefaultMember(ProjectRole.PROJECT_OWNER.create(projectTable.getId(), projectOwner.getUserId(), true));
            String newPageContent = newProject.getPageContent();
            if (newPageContent == null) {
                newPageContent = "# " + projectTable.getName() + "\n\n" + this.config.pages.home.getMessage();
            }
            this.projectPageService.createPage(projectTable.getId(), this.config.pages.home.getName(), StringUtils.slugify(this.config.pages.home.getName()), newPageContent, false, null, true);
            this.jobService.save(new UpdateDiscourseProjectTopicJob(projectTable.getId()));
        } catch (Exception exception) {
            if (projectTable != null) {
                this.projectsDAO.delete(projectTable);
            }
            throw exception;
        }

        this.usersApiService.clearAuthorsCache();
        return projectTable;
    }

    public String renameProject(final String author, final String slug, final String newName) {
        return renameProject(author, slug, newName, false);
    }

    @Transactional
    public String renameProject(final String author, final String slug, final String newName, final boolean skipNameCheck) {
        final String compactNewName = StringUtils.compact(newName);
        final ProjectTable projectTable = this.projectService.getProjectTable(author, slug);
        final String oldName = projectTable.getName();
        final String oldSlug = projectTable.getSlug();
        this.checkProjectAvailability(projectTable.getOwnerId(), compactNewName, skipNameCheck);

        final String newSlug = StringUtils.slugify(compactNewName);
        projectTable.setName(compactNewName);
        projectTable.setSlug(newSlug);
        this.projectsDAO.update(projectTable);
        this.actionLogger.project(LogAction.PROJECT_RENAMED.create(ProjectContext.of(projectTable.getId()), author + "/" + compactNewName, author + "/" + oldName));
        this.jobService.save(new UpdateDiscourseProjectTopicJob(projectTable.getId()));
        this.projectService.refreshHomeProjects();
        projectFiles.renameProject(projectTable.getOwnerName(), oldSlug, newSlug);
        return newSlug;
    }

    public void checkProjectAvailability(final long userId, final String name) {
        checkProjectAvailability(userId, name, false);
    }

    public void checkProjectAvailability(final long userId, final String name, final boolean skipNameChecking) {
        String errorKey = null;
        if (!skipNameChecking) {
            errorKey = this.validationService.isValidProjectName(name);
        }
        if (errorKey == null) {
            InvalidProjectReason reason = this.projectsDAO.checkProjectValidity(userId, name, StringUtils.slugify(name));
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
        if (projectTable.getVisibility() == Visibility.NEW) {
            this.hardDelete(projectTable, comment);
        } else if (projectTable.getVisibility() != Visibility.SOFTDELETE) {
            // Append deletion suffix to allow creation of new projects under the old namespace
            int deletedId = -1;
            for (int i = 0; i < 10; i++) {
                if (this.projectsDAO.getBySlug(projectTable.getOwnerName(), projectTable.getSlug() + SOFT_DELETION_SUFFIX + i) == null) {
                    deletedId = i;
                    break;
                }
            }

            if (deletedId == -1) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "Project has been deleted too often");
            }

            final String newName = projectTable.getName() + SOFT_DELETION_SUFFIX + deletedId;
            this.renameProject(projectTable.getOwnerName(), projectTable.getSlug(), newName, true);
            projectTable.setName(newName);
            projectTable.setSlug(StringUtils.slugify(newName));
            this.projectVisibilityService.changeVisibility(projectTable, Visibility.SOFTDELETE, comment);
        }
    }

    @Transactional
    public void hardDelete(final ProjectTable projectTable, final String comment) {
        this.actionLogger.project(LogAction.PROJECT_VISIBILITY_CHANGED.create(ProjectContext.of(projectTable.getId()), "Deleted: " + comment, projectTable.getVisibility().getTitle()));
        this.jobService.save(new DeleteDiscourseTopicJob(projectTable.getId()));
        this.projectsDAO.delete(projectTable);
        this.projectService.refreshHomeProjects();
        FileUtils.deleteDirectory(this.projectFiles.getProjectDir(projectTable.getOwnerName(), projectTable.getSlug()));
    }

    @EnumByName
    public enum InvalidProjectReason {

        OWNER_NAME("project.new.error.nameExists"),
        OWNER_SLUG("project.new.error.slugExists"),
        INVALID_NAME("project.new.error.invalidName");

        private final String key;

        InvalidProjectReason(final String key) {
            this.key = key;
        }
    }
}
