package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.members.ProjectMemberTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProject;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.MemberService;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.enums.EnumByName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProjectFactory extends HangarService {

    private final ProjectDAO projectDAO;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final PageService pageService;
    private final MemberService.ProjectMemberService projectMemberService;
    private final UsersApiService usersApiService;

    @Autowired
    public ProjectFactory(HangarDao<ProjectDAO> projectDAO, ProjectService projectService, ChannelService channelService, PageService pageService, MemberService.ProjectMemberService projectMemberService, UsersApiService usersApiService) {
        this.projectDAO = projectDAO.get();
        this.projectService = projectService;
        this.channelService = channelService;
        this.pageService = pageService;
        this.projectMemberService = projectMemberService;
        this.usersApiService = usersApiService;
    }

    public ProjectTable createProject(NewProject newProject) {
        ProjectOwner projectOwner = projectService.getProjectOwner(newProject.getOwnerId());
        if (projectOwner == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.project.ownerNotFound");
        }
        checkProjectAvailability(projectOwner.getUserId(), newProject.getName());
        ProjectTable projectTable = null;
        try {
            projectTable = projectDAO.insert(new ProjectTable(projectOwner, newProject));
            channelService.createProjectChannel(hangarConfig.channels.getNameDefault(), hangarConfig.channels.getColorDefault(), projectTable.getId(), false);
            projectMemberService.addMember(projectTable.getId(), ProjectRole.PROJECT_OWNER.create(projectTable.getId(), projectOwner.getUserId(), true), ProjectMemberTable::new);
            String newPageContent = newProject.getPageContent();
            if (newPageContent == null) {
                newPageContent = "# " + projectTable.getName() + "\n\n" + hangarConfig.pages.home.getMessage();
            }
            pageService.createPage(projectTable.getId(), hangarConfig.pages.home.getName(), StringUtils.slugify(hangarConfig.pages.home.getName()), newPageContent, false, null, true);
        } catch (Throwable exception) {
            if (projectTable != null) {
                projectDAO.delete(projectTable);
            }
            throw exception;
        }

        usersApiService.clearAuthorsCache();
        projectService.refreshHomeProjects();
        return projectTable;
    }

    public void checkProjectAvailability(long userId, String name) {
        InvalidProjectReason invalidProjectReason;
        if (!hangarConfig.isValidProjectName(name)) {
            invalidProjectReason = InvalidProjectReason.INVALID_NAME;
        } else {
            invalidProjectReason = projectDAO.checkProjectValidity(userId, name, StringUtils.slugify(name));
        }
        if (invalidProjectReason != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, invalidProjectReason.key);
        }
    }

    @EnumByName
    public enum InvalidProjectReason {

        OWNER_NAME("error.project.nameExists"),
        OWNER_SLUG("error.project.slugExists"),
        INVALID_NAME("error.project.invalidName");

        private final String key;

        InvalidProjectReason(String key) {
            this.key = key;
        }
    }
}
