package me.minidigger.hangar.controller;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectPageDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.service.MarkdownService;
import me.minidigger.hangar.service.UserActionLogService;
import me.minidigger.hangar.service.project.PagesFactory;
import me.minidigger.hangar.service.project.PagesSerivce;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.RouteHelper;
import me.minidigger.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PagesController extends HangarController {

    private final UserActionLogService userActionLogService;
    private final ProjectService projectService;
    private final PagesSerivce pagesSerivce;
    private final PagesFactory pagesFactory;
    private final HangarDao<ProjectPageDao> projectPageDao;
    private final RouteHelper routeHelper;
    private final MarkdownService markdownService;

    @Autowired
    public PagesController(UserActionLogService userActionLogService, ProjectService projectService, PagesSerivce pagesSerivce, PagesFactory pagesFactory, HangarDao<ProjectPageDao> projectPageDao, RouteHelper routeHelper, MarkdownService markdownService) {
        this.userActionLogService = userActionLogService;
        this.projectService = projectService;
        this.pagesSerivce = pagesSerivce;
        this.pagesFactory = pagesFactory;
        this.projectPageDao = projectPageDao;
        this.routeHelper = routeHelper;
        this.markdownService = markdownService;
    }

    @PostMapping(value = "/pages/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> showPreview(@RequestBody String raw) throws JSONException {
        JSONObject rawJson;
        try {
            rawJson = new JSONObject(raw);
            rawJson.getString("raw");
        } catch (JSONException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(markdownService.render(rawJson.getString("raw")), HttpStatus.OK);
    }

    @GetMapping({"/{author}/{slug}/pages/{page}", "/{author}/{slug}/pages/{page}/{subPage}"})
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        String pageName = getPageName(page, subPage);
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectPage projectPage = ProjectPage.of(pagesSerivce.getPage(projectData.getProject().getId(), pageName));
        mav.addObject("p", projectData);
        ScopedProjectData sp = new ScopedProjectData();
        sp.setPermissions(Permission.IsProjectOwner.add(Permission.EditPage));
        mav.addObject("sp", sp);
        mav.addObject("page", projectPage);
        mav.addObject("parentPage");
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @PostMapping({"/{author}/{slug}/pages/{page}/delete", "/{author}/{slug}/pages/{page}/{subPage}/delete"})
    public RedirectView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        String pageName = page + (subPage != null && !subPage.isEmpty() ? "/" + subPage : "");
        ProjectData projectData = projectService.getProjectData(author, slug);
        if (projectData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ProjectPagesTable projectPage = pagesSerivce.getPage(projectData.getProject().getId(), pageName);
        if (projectPage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!projectPage.getIsDeletable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this page");
        }
        projectPageDao.get().delete(projectPage);
        return new RedirectView(routeHelper.getRouteUrl("projects.show", author, slug));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = {"/{author}/{slug}/pages/{page}/edit", "/{author}/{slug}/pages/{page}/{subPage}/edit"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object save(@PathVariable String author,
                       @PathVariable String slug,
                       @PathVariable String page,
                       @PathVariable(required = false) String subPage,
                       @RequestParam(value = "parent-id", required = false) String parentId,
                       @RequestParam("content") String pageContent,
                       @RequestParam("name") String newPageName,
                       HttpServletRequest request) {
        String pageName = getPageName(page, subPage);

        ProjectData projectData = projectService.getProjectData(author, slug);
        if (projectData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String oldContents = null;
        ProjectPagesTable projectPage = pagesSerivce.getPage(projectData.getProject().getId(), pageName);
        Object toReturn;
        if (projectPage == null) { // new page
            Long parentIdLong;
            try {
                parentIdLong = Long.parseLong(parentId);
            } catch (NumberFormatException e) {
                parentIdLong = null;
            }
            projectPage = pagesFactory.createPage(pageContent, newPageName, pageName, parentIdLong, projectData.getProject().getId());
            toReturn = new ResponseEntity<>(HttpStatus.OK); // redirect handled by pageEdit.js
        } else {
            oldContents = projectPage.getContents();
            projectPage.setContents(pageContent);
            projectPageDao.get().update(projectPage);
            toReturn = new RedirectView(routeHelper.getRouteUrl("pages.show", author, slug, StringUtils.slugify(pageName)));
        }
        userActionLogService.projectPage(request, LoggedActionType.PROJECT_PAGE_EDITED.with(ProjectPageContext.of(projectData.getProject().getId(), projectPage.getId())), pageContent, oldContents);
        return toReturn;
    }

    @Secured("ROLE_USER")
    @GetMapping({"/{author}/{slug}/pages/{page}/edit", "/{author}/{slug}/pages/{page}/{subPage}/edit"})
    public ModelAndView showEditor(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @PathVariable(required = false) String subPage) {
        String pageName = getPageName(page, subPage);
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectPage projectPage = ProjectPage.of(pagesSerivce.getPage(projectData.getProject().getId(), pageName));
        if (projectPage == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        mav.addObject("p", projectData);
        ScopedProjectData sp = new ScopedProjectData();
        sp.setPermissions(Permission.IsProjectOwner.add(Permission.EditPage));
        mav.addObject("sp", sp);
        mav.addObject("page", projectPage);
        mav.addObject("parentPage"); // TODO parentPage
        mav.addObject("editorOpen", true);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        return fillModel(mav);
    }

    private String getPageName(String page, @Nullable String subPage) {
        return page + (subPage == null || subPage.isEmpty() ? "" : "/" + subPage);
    }

}

