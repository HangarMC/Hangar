package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectPage;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.util.BBCodeConverter;
import jakarta.validation.Valid;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// @el(author: String, slug: String, projectId: long)
@Anyone
@Controller
@RateLimit(path = "projectpage")
@RequestMapping("/api/internal/pages")
public class ProjectPageController extends HangarComponent {

    private final ProjectPageService projectPageService;
    private final ValidationService validationService;

    @Autowired
    public ProjectPageController(final ProjectPageService projectPageService, final ValidationService validationService) {
        this.projectPageService = projectPageService;
        this.validationService = validationService;
    }

    @RateLimit(overdraft = 10, refillTokens = 3, refillSeconds = 5)
    @ResponseBody
    @PostMapping(path = "/convert-bbcode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String convertBBCode(@RequestBody @Valid final StringContent bbCodeContent) {
        if (bbCodeContent.getContent().length() > this.config.projects.maxBBCodeLen()) {
            throw new HangarApiException("page.new.error.maxLength");
        }
        final BBCodeConverter bbCodeConverter = new BBCodeConverter();
        return bbCodeConverter.convertToMarkdown(bbCodeContent.getContent());
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/checkName")
    public void checkName(@RequestParam final long projectId, @RequestParam final String name, @RequestParam(required = false) final Long parentId) {
        this.validationService.testPageName(name);
        this.projectPageService.checkDuplicateName(projectId, name, parentId);
    }

    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#slug}")
    @GetMapping(path = "/page/{slug}/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExtendedProjectPage> getProjectPage(@PathVariable final String slug) {
        return ResponseEntity.ok(this.projectPageService.getProjectPageFromURI(slug, this.request.getRequestURI()));
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @GetMapping("/list/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Collection<HangarProjectPage>> listProjectPages(@PathVariable final long projectId) {
        return ResponseEntity.ok(this.projectPageService.getProjectPages(projectId).values());
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping(value = "/create/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createProjectPage(@PathVariable final long projectId, @RequestBody @Valid final NewProjectPage newProjectPage) {
        return ResponseEntity.ok(this.projectPageService.createProjectPage(projectId, newProjectPage));
    }

    @Unlocked
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping(value = "/save/{projectId}/{pageId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void saveProjectPage(@PathVariable final long projectId, @PathVariable final long pageId, @RequestBody @Valid final StringContent content) {
        this.projectPageService.saveProjectPage(projectId, pageId, content.getContent());
    }

    @Unlocked
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping("/delete/{projectId}/{pageId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProjectPage(@PathVariable final long projectId, @PathVariable final long pageId) {
        this.projectPageService.deleteProjectPage(projectId, pageId);
    }
}
