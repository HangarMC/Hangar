package me.minidigger.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;

@Controller
public class Apiv1Controller extends HangarController {

    private final HangarDao<UserDao> userDao;

    @Autowired
    public Apiv1Controller(HangarDao<UserDao> userDao) {
        this.userDao = userDao;
    }

    @RequestMapping("/api/sync_sso")
    public Object syncSso() {
        return null; // TODO implement syncSso request controller
    }

    @RequestMapping("/api/v1/projects")
    public Object listProjects(@RequestParam Object categories, @RequestParam Object sort, @RequestParam Object q, @RequestParam Object limit, @RequestParam Object offset) {
        return null; // TODO implement listProjects request controller
    }

    @RequestMapping("/api/v1/projects/{pluginId}")
    public Object showProject(@PathVariable Object pluginId) {
        return null; // TODO implement showProject request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/api/v1/projects/{pluginId}/keys/new")
    public Object createKey(@PathVariable Object pluginId) {
        return null; // TODO implement createKey request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/api/v1/projects/{pluginId}/keys/revoke")
    public Object revokeKey(@PathVariable Object pluginId) {
        return null; // TODO implement revokeKey request controller
    }

    @RequestMapping("/api/v1/projects/{pluginId}/pages")
    public Object listPages(@PathVariable Object pluginId, @RequestParam Object parentId) {
        return null; // TODO implement listPages request controller
    }

    @RequestMapping("/api/v1/projects/{pluginId}/versions")
    public Object listVersions(@PathVariable Object pluginId, @RequestParam Object channels, @RequestParam Object limit, @RequestParam Object offset) {
        return null; // TODO implement listVersions request controller
    }

    @RequestMapping("/api/v1/projects/{pluginId}/versions/{name}")
    public Object showVersion(@PathVariable Object pluginId, @PathVariable Object name) {
        return null; // TODO implement showVersion request controller
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/v1/projects/{pluginId}/versions/{name}")
    public Object deployVersion(@PathVariable Object pluginId, @PathVariable Object name) {
        return null; // TODO implement deployVersion request controller
    }

    @RequestMapping("/api/v1/projects/{plugin}/tags/{versionName}")
    public Object listTags(@PathVariable Object plugin, @PathVariable Object versionName) {
        return null; // TODO implement listTags request controller
    }

    @RequestMapping("/api/v1/tags/{tagId}")
    public Object tagColor(@PathVariable Object tagId) {
        return null; // TODO implement tagColor request controller
    }

    @RequestMapping("/api/v1/users")
    public Object listUsers(@RequestParam Object limit, @RequestParam Object offset) {
        return null; // TODO implement listUsers request controller
    }

    @RequestMapping("/api/v1/users/{user}")
    @ResponseBody
    public UsersTable showUser(@PathVariable String user) {
        return userDao.get().getByName(user);
    }

    @RequestMapping("/statusz")
    public Object showStatusZ() {
        return null; // TODO implement showStatusZ request controller
    }

}

