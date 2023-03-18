package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPagesController;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

// @el(author: String, slug: String, projectId: long)
@Anyone
@Controller
@RateLimit(path = "projectpage")
public class PagesController extends HangarComponent implements IPagesController{

    private final ProjectPageService projectPageService;

    public PagesController(final ProjectPageService projectPageService) {
        this.projectPageService = projectPageService;
    }

    @Override
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.VIEW_PUBLIC_INFO, args = "{#author, #slug}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getPage(final String author, final String slug) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(author, slug, this.request.getRequestURI());
        if (projectPage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return projectPage.getContents();
    }

    @Unlocked
    @Override
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#author, #slug}")
    @ResponseStatus(HttpStatus.OK)
    public void changePage(final String author, final String slug, final String content) {
        final ExtendedProjectPage projectPage = this.projectPageService.getProjectPage(author, slug, this.request.getRequestURI());
        this.projectPageService.saveProjectPage(projectPage.getProjectId(), projectPage.getProjectId(), content);
    }
}
