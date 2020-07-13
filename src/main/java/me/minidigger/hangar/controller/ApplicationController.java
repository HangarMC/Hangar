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
    public Object showActivities(@PathVariable Object user) {
        return null; // TODO implement showActivities request controller
    }

    @RequestMapping("/admin/approval/projects")
    public Object showProjectVisibility() {
        return null; // TODO implement showProjectVisibility request controller
    }

    @RequestMapping("/admin/approval/versions")
    public Object showQueue() {
        return null; // TODO implement showQueue request controller
    }

    @RequestMapping("/admin/flags")
    public Object showFlags() {
        return null; // TODO implement showFlags request controller
    }

    @RequestMapping("/admin/flags/{id}/resolve/{resolved}")
    public Object setFlagResolved(@PathVariable Object id, @PathVariable Object resolved) {
        return null; // TODO implement setFlagResolved request controller
    }

    @RequestMapping("/admin/health")
    public Object showHealth() {
        return null; // TODO implement showHealth request controller
    }

    @RequestMapping("/admin/log")
    public Object showLog(@RequestParam Object page, @RequestParam Object userFilter, @RequestParam Object projectFilter, @RequestParam Object versionFilter, @RequestParam Object pageFilter, @RequestParam Object actionFilter, @RequestParam Object subjectFilter) {
        return null; // TODO implement showLog request controller
    }

    @RequestMapping("/admin/stats")
    public Object showStats(@RequestParam Object from, @RequestParam Object to) {
        return null; // TODO implement showStats request controller
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
    public Object swagger() {
        return null; // TODO implement swagger request controller
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

    @RequestMapping("/{path}/")
    public Object removeTrail(@PathVariable Object path) {
        return null; // TODO implement removeTrail request controller
    }
}

