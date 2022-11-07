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
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.MarkdownService;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.util.BBCodeConverter;
import java.util.Collection;
import javax.validation.Valid;
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
    private final MarkdownService markdownService;
    private final ValidationService validationService;

    @Autowired
    public ProjectPageController(ProjectPageService projectPageService, MarkdownService markdownService, ValidationService validationService) {
        this.projectPageService = projectPageService;
        this.markdownService = markdownService;
        this.validationService = validationService;
    }

    @Anyone
    @RateLimit(overdraft = 10, refillTokens = 3, refillSeconds = 5, greedy = true)
    @PostMapping(path = "/render", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renderMarkdown(@RequestBody @Valid StringContent content) {
        if (content.getContent().length() > config.projects.getContentMaxLen()) {
            throw new HangarApiException("page.new.error.name.maxLength");
        }
        return ResponseEntity.ok(markdownService.render(content.getContent()));
    }

    @Unlocked
    @RateLimit(overdraft = 10, refillTokens = 3, refillSeconds = 5)
    @ResponseBody
    @PostMapping(path = "/convert-bbcode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String convertBBCode(@RequestBody @Valid StringContent bbCodeContent) {
        if (bbCodeContent.getContent().length() > config.projects.getMaxBBCodeLen()) {
            throw new HangarApiException("page.new.error.name.maxLength");
        }
        BBCodeConverter bbCodeConverter = new BBCodeConverter();
        return bbCodeConverter.convertToMarkdown(bbCodeContent.getContent());
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/checkName")
    public void checkName(@RequestParam long projectId, @RequestParam String name, @RequestParam(required = false) Long parentId) {
        validationService.testPageName(name);
        projectPageService.checkDuplicateName(projectId, name, parentId);
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping(path = "/page/{author}/{slug}/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExtendedProjectPage> getProjectPage(@PathVariable String author, @PathVariable String slug) {
        return ResponseEntity.ok(projectPageService.getProjectPage(author, slug, request.getRequestURI()));
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @GetMapping(value = "/list/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Collection<HangarProjectPage>> listProjectPages(@PathVariable final long projectId) {
        return ResponseEntity.ok(projectPageService.getProjectPages(projectId).values());
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping(value = "/create/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createProjectPage(@PathVariable long projectId, @RequestBody @Valid NewProjectPage newProjectPage) {
        return ResponseEntity.ok(projectPageService.createProjectPage(projectId, newProjectPage));
    }

    @Unlocked
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping(value = "/save/{projectId}/{pageId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void saveProjectPage(@PathVariable long projectId, @PathVariable long pageId, @RequestBody @Valid StringContent content) {
        projectPageService.saveProjectPage(projectId, pageId, content.getContent());
    }

    @Unlocked
    @PermissionRequired(perms = NamedPermission.EDIT_PAGE, type = PermissionType.PROJECT, args = "{#projectId}")
    @PostMapping("/delete/{projectId}/{pageId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProjectPage(@PathVariable long projectId, @PathVariable long pageId) {
        projectPageService.deleteProjectPage(projectId, pageId);
    }
}
