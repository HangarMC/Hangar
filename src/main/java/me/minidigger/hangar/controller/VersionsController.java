package me.minidigger.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.model.viewhelpers.VersionData;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.VersionService;
import me.minidigger.hangar.service.pluginupload.PendingVersion;
import me.minidigger.hangar.service.pluginupload.PluginUploadService;
import me.minidigger.hangar.service.project.ProjectFactory;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.AlertUtil;

@Controller
public class VersionsController extends HangarController {

    private final ProjectService projectService;
    private final VersionService versionService;
    private final ProjectFactory projectFactory;
    private final UserService userService;
    private final PluginUploadService pluginUploadService;

    @Autowired
    public VersionsController(ProjectService projectService, VersionService versionService, ProjectFactory projectFactory, UserService userService, PluginUploadService pluginUploadService) {
        this.projectService = projectService;
        this.versionService = versionService;
        this.projectFactory = projectFactory;
        this.userService = userService;
        this.pluginUploadService = pluginUploadService;
    }

    @RequestMapping("/api/project/{pluginId}/versions/recommended/download")
    public Object downloadRecommendedJarById(@PathVariable Object pluginId, @RequestParam Object token) {
        return null; // TODO implement downloadRecommendedJarById request controller
    }

    @RequestMapping("/api/project/{pluginId}/versions/{name}/download")
    public Object downloadJarById(@PathVariable Object pluginId, @PathVariable Object name, @RequestParam Object token) {
        return null; // TODO implement downloadJarById request controller
    }

    @RequestMapping("/{author}/{slug}/versionLog")
    public Object showLog(@PathVariable Object author, @PathVariable Object slug, @RequestParam Object versionString) {
        return null; // TODO implement showLog request controller
    }

    @RequestMapping("/{author}/{slug}/versions")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/versions/list");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData sp = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("p", projectData);
        mav.addObject("channels", List.of()); // TODO channel list
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/new")
    public ModelAndView showCreator(@PathVariable String author, @PathVariable String slug) {
        return _showCreator(author, slug, null);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/new/upload")
    public ModelAndView upload(@PathVariable String author, @PathVariable String slug, @RequestParam("pluginFile") MultipartFile file) {
        String uploadError = projectFactory.getUploadError(userService.getCurrentUser());
        if (uploadError != null) {
            if (file == null) {
                uploadError = "error.noFile";
            }
        }
        if (uploadError != null) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, uploadError);
            return fillModel(mav);
        }

        ProjectData projectData = projectService.getProjectData(author, slug);
        PendingVersion pendingVersion = pluginUploadService.processSubsequentPluginUpload(file, projectData.getProjectOwner(), projectData.getProject());

        return _showCreator(author, slug, pendingVersion);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/new/{versionName}")
    public ModelAndView showCreatorWithMeta(@PathVariable String author, @PathVariable String slug, @PathVariable String versionName) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        PendingVersion pendingVersion = pluginUploadService.getPendingVersion(projectData.getProject(), versionName);

        if (pendingVersion == null) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.plugin.timeout");
            return fillModel(mav);
        }

        return _showCreator(author, slug, pendingVersion);
    }

    private ModelAndView _showCreator(String author, String slug, Object pendingVersion) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ModelAndView mav = new ModelAndView("projects/versions/create");
        mav.addObject("projectName", projectData.getProject().getName());
        mav.addObject("pluginId", projectData.getProject().getPluginId());
        mav.addObject("projectSlug", slug);
        mav.addObject("ownerName", author);
        mav.addObject("projectDescription", projectData.getProject().getDescription());
        mav.addObject("forumSync", projectData.getProject().getForumSync());
        mav.addObject("pending", pendingVersion);
        mav.addObject("channels", List.of());// TODO channel list
        return fillModel(mav);
    }

    @RequestMapping("/{author}/{slug}/versions/recommended/download")
    public Object downloadRecommended(@PathVariable Object author, @PathVariable Object slug, @RequestParam Object token) {
        return null; // TODO implement downloadRecommended request controller
    }

    @RequestMapping("/{author}/{slug}/versions/recommended/jar")
    public Object downloadRecommendedJar(@PathVariable Object author, @PathVariable Object slug, @RequestParam Object token) {
        return null; // TODO implement downloadRecommendedJar request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}")
    public Object publish(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement publish request controller
    }

    @GetMapping("/{author}/{slug}/versions/{version}")
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ModelAndView mav = new ModelAndView("projects/versions/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData sp = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("v", new VersionData(projectData, versionService.getVersion(projectData.getProject().getId()), null));
        mav.addObject("sp", sp);
        return fillModel(mav);  // TODO implement show request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/approve")
    public Object approve(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement approve request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/approvePartial")
    public Object approvePartial(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement approvePartial request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/confirm")
    public Object showDownloadConfirm(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object downloadType, @RequestParam Object api, @RequestParam Object dummy) {
        return null; // TODO implement showDownloadConfirm request controller
    }

    @PostMapping("/{author}/{slug}/versions/{version}/confirm")
    public Object confirmDownload(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object downloadType, @RequestParam Object api, @RequestParam Object dummy) {
        return null; // TODO implement confirmDownload request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/delete")
    public Object softDelete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement softDelete request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/download")
    public Object download(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object token, @RequestParam Object confirm) {
        return null; // TODO implement download request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/hardDelete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement delete request controller
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/jar")
    public Object downloadJar(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object token) {
        return null; // TODO implement downloadJar request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/recommended")
    public Object setRecommended(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement setRecommended request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/restore")
    public Object restore(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement restore request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/save")
    public Object saveDescription(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement saveDescription request controller
    }

}

