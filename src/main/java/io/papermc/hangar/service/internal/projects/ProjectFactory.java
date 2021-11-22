package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
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

@Service
public class ProjectFactory extends HangarComponent {

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
    private final HomeProjectService homeProjectService;

    @Autowired
    public ProjectFactory(ProjectsDAO projectDAO, ProjectService projectService, ChannelService channelService, ProjectPageService projectPageService, ProjectMemberService projectMemberService, ProjectVisibilityService projectVisibilityService, UsersApiService usersApiService, JobService jobService, ProjectFiles projectFiles, ValidationService validationService, HomeProjectService homeProjectService) {
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
        this.homeProjectService = homeProjectService;
    }

    @Transactional
    public ProjectTable createProject(NewProjectForm newProject) {
        ProjectOwner projectOwner = projectService.getProjectOwner(newProject.getOwnerId());
        if (projectOwner == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.project.ownerNotFound");
        }
        checkProjectAvailability(projectOwner.getUserId(), newProject.getName());
        ProjectTable projectTable = null;
        try {
            projectTable = projectsDAO.insert(new ProjectTable(projectOwner, newProject));
            channelService.createProjectChannel(config.channels.getNameDefault(), config.channels.getColorDefault(), projectTable.getId(), false);
            projectMemberService.addNewAcceptedByDefaultMember(ProjectRole.PROJECT_OWNER.create(projectTable.getId(), projectOwner.getUserId(), true));
            String newPageContent = newProject.getPageContent();
            if (newPageContent == null) {
                newPageContent = "# " + projectTable.getName() + "\n\n" + config.pages.home.getMessage();
            }
            projectPageService.createPage(projectTable.getId(), config.pages.home.getName(), StringUtils.slugify(config.pages.home.getName()), newPageContent, false, null, true);
            jobService.save(new UpdateDiscourseProjectTopicJob(projectTable.getId()));
        } catch (Exception exception) {
            if (projectTable != null) {
                projectsDAO.delete(projectTable);
            }
            throw exception;
        }

        usersApiService.clearAuthorsCache();
        return projectTable;
    }


    public String renameProject(String author, String slug, String newName) {
        String compactNewName = StringUtils.compact(newName);
        ProjectTable projectTable = projectService.getProjectTable(author, slug);
        String oldName = projectTable.getName();
        checkProjectAvailability(projectTable.getOwnerId(), compactNewName);
        projectTable.setName(compactNewName);
        projectTable.setSlug(StringUtils.slugify(compactNewName));
        projectsDAO.update(projectTable);
        actionLogger.project(LogAction.PROJECT_RENAMED.create(ProjectContext.of(projectTable.getId()), author + "/" + compactNewName, author + "/" + oldName));
        jobService.save(new UpdateDiscourseProjectTopicJob(projectTable.getId()));
        homeProjectService.refreshHomeProjects();
        return StringUtils.slugify(compactNewName);
    }

    public void checkProjectAvailability(long userId, String name) {
        String errorKey = validationService.isValidProjectName(name);
        if (errorKey == null) {
            var reason = projectsDAO.checkProjectValidity(userId, name, StringUtils.slugify(name));
            if (reason != null) {
                errorKey = reason.key;
            }
        }
        if (errorKey != null) {
            throw new HangarApiException(HttpStatus.CONFLICT, errorKey);
        }
    }

    @EnumByName
    public enum InvalidProjectReason {

        OWNER_NAME("project.new.error.nameExists"),
        OWNER_SLUG("project.new.error.slugExists"),
        INVALID_NAME("project.new.error.invalidName");

        private final String key;

        InvalidProjectReason(String key) {
            this.key = key;
        }
    }

    public void softDelete(ProjectTable projectTable, String comment) {
        if (projectTable.getVisibility() == Visibility.NEW) {
            hardDelete(projectTable, comment);
        } else {
            jobService.save(new UpdateDiscourseProjectTopicJob(projectTable.getId()));
            projectVisibilityService.changeVisibility(projectTable, Visibility.SOFTDELETE, comment);
            homeProjectService.refreshHomeProjects();
        }
    }

    public void hardDelete(ProjectTable projectTable, String comment) {
        actionLogger.project(LogAction.PROJECT_VISIBILITY_CHANGED.create(ProjectContext.of(projectTable.getId()), "Deleted: " + comment, projectTable.getVisibility().getTitle()));
        FileUtils.deleteDirectory(projectFiles.getProjectDir(projectTable.getOwnerName(), projectTable.getName()));
        jobService.save(new DeleteDiscourseTopicJob(projectTable.getId()));
        projectsDAO.delete(projectTable);
        homeProjectService.refreshHomeProjects();
    }
}
