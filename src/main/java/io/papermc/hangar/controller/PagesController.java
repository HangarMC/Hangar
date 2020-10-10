package io.papermc.hangar.controller;

import io.papermc.hangar.controller.forms.RawPage;
import io.papermc.hangar.controller.util.BBCodeConverter;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectPageDao;
import io.papermc.hangar.db.model.ProjectPagesTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.MarkdownService;
import io.papermc.hangar.service.StatsService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.project.PagesFactory;
import io.papermc.hangar.service.project.PagesSerivce;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

@Controller
public class PagesController extends HangarController {

    private final UserActionLogService userActionLogService;
    private final ProjectService projectService;
    private final MarkdownService markdownService;
    private final PagesSerivce pagesSerivce;
    private final StatsService statsService;
    private final PagesFactory pagesFactory;
    private final HangarDao<ProjectPageDao> projectPageDao;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    public PagesController(UserActionLogService userActionLogService, ProjectService projectService, MarkdownService markdownService, PagesSerivce pagesSerivce, StatsService statsService, PagesFactory pagesFactory, HangarDao<ProjectPageDao> projectPageDao, HttpServletRequest request, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.userActionLogService = userActionLogService;
        this.projectService = projectService;
        this.markdownService = markdownService;
        this.pagesSerivce = pagesSerivce;
        this.statsService = statsService;
        this.pagesFactory = pagesFactory;
        this.projectPageDao = projectPageDao;
        this.request = request;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @PostMapping(value = "/pages/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> showPreview(@RequestBody RawPage rawObj) {
        return ResponseEntity.ok(markdownService.render(rawObj.getRaw()));
    }

    @PostMapping(value = "/pages/bb-convert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> bbConverter(@RequestBody RawPage rawObj) {
        BBCodeConverter bbCodeConverter = new BBCodeConverter();
        return ResponseEntity.ok(bbCodeConverter.convertToMarkdown(rawObj.getRaw()));
    }

    @GetMapping({"/{author}/{slug}/pages/{page}", "/{author}/{slug}/pages/{page}/{subPage}"})
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        ProjectData projData = projectData.get();
        String pageName = getPageName(page, subPage);
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectPage projectPage = pagesSerivce.getPage(projData.getProject().getId(), pageName);
        mav.addObject("p", projData);
        ScopedProjectData sp = projectService.getScopedProjectData(projData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("projectPage", projectPage);
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projData.getProject().getId());
        statsService.addProjectView(projData.getProject());
        return fillModel(mav);
    }

    @ProjectPermission(NamedPermission.EDIT_PAGE)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping({"/{author}/{slug}/pages/{page}/delete", "/{author}/{slug}/pages/{page}/{subPage}/delete"})
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        String pageName = page + (subPage != null && !subPage.isEmpty() ? "/" + subPage : "");
        ProjectPagesTable projectPage = pagesSerivce.getPage(projectsTable.get().getId(), pageName);
        if (projectPage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!projectPage.getIsDeletable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this page");
        }
        userActionLogService.projectPage(request, LoggedActionType.PROJECT_PAGE_DELETED.with(ProjectPageContext.of(projectPage.getProjectId(), projectPage.getId())), "", projectPage.getContents());
        projectPageDao.get().delete(projectPage);
        return Routes.PROJECTS_SHOW.getRedirect(author, slug);
    }

    @ProjectPermission(NamedPermission.EDIT_PAGE)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = {"/{author}/{slug}/pages/{page}/edit", "/{author}/{slug}/pages/{page}/{subPage}/edit"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object save(@PathVariable String author,
                       @PathVariable String slug,
                       @PathVariable String page,
                       @PathVariable(required = false) String subPage,
                       @RequestParam(value = "parent-id", required = false) String parentId,
                       @RequestParam("content") String pageContent,
                       @RequestParam("name") String newPageName) {
        ProjectsTable project = projectsTable.get();
        String pageName = getPageName(page, subPage);
        String oldContents = null;
        ProjectPagesTable projectPage = pagesSerivce.getPage(project.getId(), pageName);
        Object toReturn;
        if (projectPage == null) { // new page
            Long parentIdLong;
            try {
                parentIdLong = Long.parseLong(parentId);
            } catch (NumberFormatException e) {
                parentIdLong = null;
            }
            projectPage = pagesFactory.createPage(pageContent, newPageName, pageName, parentIdLong, project.getId());
            userActionLogService.projectPage(request, LoggedActionType.PROJECT_PAGE_CREATED.with(ProjectPageContext.of(project.getId(), projectPage.getId())), pageContent, "");
            toReturn = new ResponseEntity<>(HttpStatus.OK); // redirect handled by pageEdit.js
        } else {
            oldContents = projectPage.getContents();
            projectPage.setContents(pageContent);
            projectPageDao.get().update(projectPage);
            userActionLogService.projectPage(request, LoggedActionType.PROJECT_PAGE_EDITED.with(ProjectPageContext.of(project.getId(), projectPage.getId())), pageContent, oldContents);
            toReturn = Routes.PAGES_SHOW.getRedirect(author, slug, StringUtils.slugify(pageName));
        }
        return toReturn;
    }

    @ProjectPermission(NamedPermission.EDIT_PAGE)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping({"/{author}/{slug}/pages/{page}/edit", "/{author}/{slug}/pages/{page}/{subPage}/edit"})
    public ModelAndView showEditor(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        ProjectData projData = projectData.get();
        String pageName = getPageName(page, subPage);
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectPage projectPage = pagesSerivce.getPage(projData.getProject().getId(), pageName);
        if (projectPage == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        mav.addObject("p", projData);
        ScopedProjectData sp = projectService.getScopedProjectData(projData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("projectPage", projectPage);
        mav.addObject("editorOpen", true);
        pagesSerivce.fillPages(mav, projData.getProject().getId());
        return fillModel(mav);
    }

    private String getPageName(String page, @Nullable String subPage) {
        return page + (subPage == null || subPage.isEmpty() ? "" : "/" + subPage);
    }

}

