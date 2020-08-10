package me.minidigger.hangar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.service.project.PagesSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class Apiv1Controller extends HangarController {

    private final ObjectMapper mapper;
    private final PagesSerivce pagesSerivce;
    private final HangarDao<UserDao> userDao;

    @Autowired
    public Apiv1Controller(ObjectMapper mapper, PagesSerivce pagesSerivce, HangarDao<UserDao> userDao) {
        this.mapper = mapper;
        this.pagesSerivce = pagesSerivce;
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

    @GetMapping(value = "/api/v1/projects/{pluginId}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayNode> listPages(@PathVariable String pluginId, @RequestParam(required = false) Long parentId) {
        List<ProjectPage> pages = pagesSerivce.getPages(pluginId);
        ArrayNode pagesArray = mapper.createArrayNode();
        pages.stream().filter(p -> {
            if (parentId != null) {
                return parentId.equals(p.getParentId());
            } else return true;
        }).forEach(page -> {
            ObjectNode pageObj = mapper.createObjectNode();
            pageObj.set("createdAt", mapper.valueToTree(page.getCreatedAt()));
            pageObj.set("id", mapper.valueToTree(page.getId()));
            pageObj.set("name", mapper.valueToTree(page.getName()));
            pageObj.set("parentId", mapper.valueToTree(page.getParentId()));
            String[] slug = page.getSlug().split("/");
            pageObj.set("slug", mapper.valueToTree(slug[slug.length - 1]));
            pageObj.set("fullSlug", mapper.valueToTree(page.getSlug()));
            pagesArray.add(pageObj);
        });
        if (pagesArray.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(pagesArray);
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

