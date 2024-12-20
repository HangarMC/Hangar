package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPagesController;
import io.papermc.hangar.model.api.project.PageEditForm;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// @el(author: String, slug: String, projectId: long)
@Anyone
@Controller
@RateLimit(path = "projectpage")
public class PagesController extends HangarComponent implements IPagesController {

    private final ProjectPageService projectPageService;

    public PagesController(final ProjectPageService projectPageService) {
        this.projectPageService = projectPageService;
    }

    @Override
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.VIEW_PUBLIC_INFO, args = "{#project}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getMainPage(final ProjectTable project) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(project, "");
        return projectPage.getContents();
    }

    @Override
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.VIEW_PUBLIC_INFO, args = "{#project}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getPage(final ProjectTable project, final String path) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(project, path);
        return projectPage.getContents();
    }

    @Unlocked
    @RequireAal(1)
    @Override
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#project}")
    @ResponseStatus(HttpStatus.OK)
    public void editMainPage(final ProjectTable project, final StringContent pageEditForm) {
        this.editPage(project, new PageEditForm("", pageEditForm.getContent()));
    }

    @Unlocked
    @RequireAal(1)
    @Override
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#project}")
    @ResponseStatus(HttpStatus.OK)
    public void editPage(final ProjectTable project, final PageEditForm pageEditForm) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(project, pageEditForm.path());
        this.projectPageService.saveProjectPage(projectPage.getProjectId(), projectPage.getId(), pageEditForm.content());
    }
}
