package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPagesController;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.PageEditForm;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

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
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.VIEW_PUBLIC_INFO, args = "{#slug}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getMainPage(final String slug) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(slug, "");
        return projectPage.getContents();
    }

    @Override
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.VIEW_PUBLIC_INFO, args = "{#slug}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getPage(final String slug, final String path) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(slug, path);
        return projectPage.getContents();
    }

    @Unlocked
    @Override
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#slug}")
    @ResponseStatus(HttpStatus.OK)
    public void editMainPage(final String slug, final StringContent pageEditForm) {
        this.editPage(slug, new PageEditForm("", pageEditForm.getContent()));
    }

    @Unlocked
    @Override
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#slug}")
    @ResponseStatus(HttpStatus.OK)
    public void editPage(final String slug, final PageEditForm pageEditForm) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(slug, pageEditForm.path());
        this.projectPageService.saveProjectPage(projectPage.getProjectId(), projectPage.getId(), pageEditForm.content());
    }
}
