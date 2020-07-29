package me.minidigger.hangar.controller;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectPageDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.service.project.PagesFactory;
import me.minidigger.hangar.service.project.PagesSerivce;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PagesController extends HangarController {

    private final ProjectService projectService;
    private final PagesSerivce pagesSerivce;
    private final PagesFactory pagesFactory;
    private final HangarDao<ProjectPageDao> projectPageDao;
    private final RouteHelper routeHelper;

    @Autowired
    public PagesController(ProjectService projectService, PagesSerivce pagesSerivce, PagesFactory pagesFactory, HangarDao<ProjectPageDao> projectPageDao, RouteHelper routeHelper) {
        this.projectService = projectService;
        this.pagesSerivce = pagesSerivce;
        this.pagesFactory = pagesFactory;
        this.projectPageDao = projectPageDao;
        this.routeHelper = routeHelper;
    }

    @PostMapping("/pages/preview")
    public Object showPreview() {
        return "Test"; // TODO implement showPreview request controller
    }

    @GetMapping("/{author}/{slug}/pages/{page:.*}") // I do not know why {*page} isn't working here...
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String page) {
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectPage projectPage = ProjectPage.of(pagesSerivce.getPage(projectData.getProject().getId(), page));
        mav.addObject("p", projectData);
        ScopedProjectData sp = new ScopedProjectData();
        sp.setPermissions(Permission.IsProjectOwner.add(Permission.EditPage));
        mav.addObject("sp", sp);
        mav.addObject("page", projectPage);
        mav.addObject("parentPage");
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        return fillModel(mav);
        // TODO implement show request controller
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/pages/{*page}/delete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object page) {
        return null; // TODO implement delete request controller
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/pages/{page:.*}/edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object save(@PathVariable String author, @PathVariable String slug, @PathVariable String page, @RequestParam(value = "parent-id", required = false) String parentId, @RequestParam("content") String pageContent, @RequestParam("name") String pageName) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        if (projectData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ProjectPagesTable projectPage = pagesSerivce.getPage(projectData.getProject().getId(), page);
        if (projectPage == null) { // new page
            System.out.println("new page");
            Long parentIdLong;
            try {
                parentIdLong = Long.parseLong(parentId);
            } catch (NumberFormatException e) {
                parentIdLong = null;
            }
            pagesFactory.createPage(pageContent, pageName, page, parentIdLong, projectData.getProject().getId());
            return new ResponseEntity<>(HttpStatus.OK); // redirect handled by pageEdit.js
        } else {
            System.out.println("edit page");
            projectPage.setContents(pageContent);
            projectPageDao.get().update(projectPage);
            return new RedirectView(routeHelper.getRouteUrl("pages.show", author, slug, page));
        }
        // TODO User action log
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/pages/{page:.*}/edit")
    public Object showEditor(@PathVariable String author, @PathVariable String slug, @PathVariable String page) {
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectPage projectPage = ProjectPage.of(pagesSerivce.getPage(projectData.getProject().getId(), page));
        if (projectPage == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        mav.addObject("p", projectData);
        ScopedProjectData sp = new ScopedProjectData();
        sp.setPermissions(Permission.IsProjectOwner.add(Permission.EditPage));
        mav.addObject("sp", sp);
        mav.addObject("page", projectPage);
        mav.addObject("parentPage");
        mav.addObject("editorOpen", true);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        return fillModel(mav);
//        return null; // TODO implement showEditor request controller
    }

}

