package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.enums.EnumByName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProjectFactory extends HangarComponent {

    private final ProjectsDAO projectsDAO;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final ProjectPageService projectPageService;
    private final ProjectMemberService projectMemberService;
    private final UsersApiService usersApiService;

    @Autowired
    public ProjectFactory(HangarDao<ProjectsDAO> projectDAO, ProjectService projectService, ChannelService channelService, ProjectPageService projectPageService, ProjectMemberService projectMemberService, UsersApiService usersApiService) {
        this.projectsDAO = projectDAO.get();
        this.projectService = projectService;
        this.channelService = channelService;
        this.projectPageService = projectPageService;
        this.projectMemberService = projectMemberService;
        this.usersApiService = usersApiService;
    }

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
        } catch (Throwable exception) {
            if (projectTable != null) {
                projectsDAO.delete(projectTable);
            }
            throw exception;
        }

        usersApiService.clearAuthorsCache();
        projectService.refreshHomeProjects();
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
        userActionLogService.project(LogAction.PROJECT_RENAMED.create(ProjectContext.of(projectTable.getId()), author + "/" + compactNewName, author + "/" + oldName));
        projectService.refreshHomeProjects();
        return StringUtils.slugify(compactNewName);
    }

    public void checkProjectAvailability(long userId, String name) {
        InvalidProjectReason invalidProjectReason;
        if (StringUtils.compact(name).length() < 1 || StringUtils.compact(name).length() > config.projects.getMaxNameLen() || !config.projects.getNameMatcher().test(name)) {
            invalidProjectReason = InvalidProjectReason.INVALID_NAME;
        } else {
            invalidProjectReason = projectsDAO.checkProjectValidity(userId, name, StringUtils.slugify(name));
        }
        if (invalidProjectReason != null) {
            throw new HangarApiException(HttpStatus.CONFLICT, invalidProjectReason.key);
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
}
