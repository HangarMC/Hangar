package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.controller.HangarController;

@Controller
public class ApplicationController extends HangarController {

    @RequestMapping("/")
    public ModelAndView showHome() {
        return fillModel( new ModelAndView("home"));
    }

    @RequestMapping("/admin/activities/{user}")
    public ModelAndView showActivities(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/admin/activity");
        mav.addObject("username", user);
        return fillModel(mav);
    }

    @RequestMapping("/admin/approval/projects")
    public Object showProjectVisibility() {
        return fillModel(new ModelAndView("users/admin/visibility")); // TODO implement showProjectVisibility request controller
    }

    @RequestMapping("/admin/approval/versions")
    public ModelAndView showQueue() {
        return fillModel(new ModelAndView("users/admin/queue")); // TODO implement showQueue request controller
    }

    @RequestMapping("/admin/flags")
    public Object showFlags() {
        return fillModel(new ModelAndView("users/admin/flags")); // TODO implement showFlags request controller
    }

    @RequestMapping("/admin/flags/{id}/resolve/{resolved}")
    public Object setFlagResolved(@PathVariable Object id, @PathVariable Object resolved) {
        return null; // TODO implement setFlagResolved request controller
    }

    @RequestMapping("/admin/health")
    public ModelAndView showHealth() {
        return fillModel(new ModelAndView("users/admin/health")); // TODO implement showHealth request controller
    }

    @RequestMapping("/admin/log")
    public ModelAndView showLog(@RequestParam(required = false) Object page,
                                @RequestParam(required = false) Object userFilter,
                                @RequestParam(required = false) Object projectFilter,
                                @RequestParam(required = false) Object versionFilter,
                                @RequestParam(required = false) Object pageFilter,
                                @RequestParam(required = false) Object actionFilter,
                                @RequestParam(required = false) Object subjectFilter) {
        return fillModel(new ModelAndView("users/admin/log"));  // TODO implement showLog request controller
    }

    @RequestMapping("/admin/stats")
    public ModelAndView showStats(@RequestParam(required = false) Object from, @RequestParam(required = false) Object to) {
        return fillModel(new ModelAndView("users/admin/stats")); // TODO implement showStats request controller
    }

    @RequestMapping("/admin/user/{user}")
    public Object userAdmin(@PathVariable Object user) {
        return null; // TODO implement userAdmin request controller
    }

    @RequestMapping("/admin/user/{user}/update")
    public Object updateUser(@PathVariable Object user) {
        return null; // TODO implement updateUser request controller
    }

    @RequestMapping("/api")
    public ModelAndView swagger() {
        return fillModel(new ModelAndView("swagger"));
    }

    @RequestMapping("/favicon.ico")
    @ResponseBody
    public Object faviconRedirect() {
        return "no u"; // TODO implement faviconRedirect request controller
    }

    @RequestMapping("/global-sitemap.xml")
    public Object globalSitemap() {
        return null; // TODO implement globalSitemap request controller
    }

    @RequestMapping("/javascriptRoutes")
    @ResponseBody
    public Object javaScriptRoutes() {
        return "no u"; // TODO implement javaScriptRoutes request controller
    }

    @RequestMapping("/linkout")
    public ModelAndView linkOut(@RequestParam(defaultValue = "") String remoteUrl) {
        ModelAndView view = new ModelAndView("linkout");
        view.addObject("remoteUrl", remoteUrl);
        return fillModel(view);
    }

    @RequestMapping("/pantopticon/actor-count")
    public Object actorCount(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorCount request controller
    }

    @RequestMapping("/pantopticon/actor-tree")
    public Object actorTree(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorTree request controller
    }

    @RequestMapping("/robots.txt")
    public Object robots() {
        return null; // TODO implement robots request controller
    }

    @RequestMapping("/sitemap.xml")
    public Object sitemapIndex() {
        return null; // TODO implement sitemapIndex request controller
    }

//    @RequestMapping("/{path}/")
//    public Object removeTrail(@PathVariable Object path) {
//        return null; // implement removeTrail request controller - pretty sure this one is dum
//    }
}
