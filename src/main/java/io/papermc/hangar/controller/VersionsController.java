package io.papermc.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.ProjectVersionDownloadWarningDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionDownloadWarningsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.DownloadType;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.plugindata.PluginDataService;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.service.pluginupload.PluginUploadService;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.service.project.ProjectFactory;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.RouteHelper;

@Controller
public class VersionsController extends HangarController {

    private final ProjectService projectService;
    private final VersionService versionService;
    private final ProjectFactory projectFactory;
    private final UserService userService;
    private final PluginUploadService pluginUploadService;
    private final ChannelService channelService;
    private final UserActionLogService userActionLogService;
    private final CacheManager cacheManager;
    private final RouteHelper routeHelper;
    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectDao> projectDao;
    private final ProjectFiles projectFiles;
    private final HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao;
    private final PluginDataService pluginDataService;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Autowired
    public VersionsController(ProjectService projectService, VersionService versionService, ProjectFactory projectFactory, UserService userService, PluginUploadService pluginUploadService, ChannelService channelService, UserActionLogService userActionLogService, CacheManager cacheManager, RouteHelper routeHelper, HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, ProjectFiles projectFiles, HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao, PluginDataService pluginDataService, HttpServletRequest request, HttpServletResponse response) {
        this.projectService = projectService;
        this.versionService = versionService;
        this.projectFactory = projectFactory;
        this.userService = userService;
        this.pluginUploadService = pluginUploadService;
        this.channelService = channelService;
        this.userActionLogService = userActionLogService;
        this.cacheManager = cacheManager;
        this.routeHelper = routeHelper;
        this.hangarConfig = hangarConfig;
        this.projectDao = projectDao;
        this.projectFiles = projectFiles;
        this.downloadWarningDao = downloadWarningDao;
        this.pluginDataService = pluginDataService;
        this.request = request;
        this.response = response;
    }

    @RequestMapping(value = "/api/project/{pluginId}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommendedJarById(@PathVariable String pluginId, @RequestParam(required = false) String token) {
        ProjectsTable project = projectDao.get().getByPluginId(pluginId);

        Long recommendedVersionId = project.getRecommendedVersionId();
        if (recommendedVersionId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), recommendedVersionId);// TODO we need to check visibility here, the query currently doesnt do that
            return sendVersion(project, versionsTable, token, true);
        }
    }

    @RequestMapping(value = "/api/project/{pluginId}/versions/{name}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadJarById(@PathVariable String pluginId, @PathVariable String name, @RequestParam(required = false) String token) {
        ProjectsTable project = projectDao.get().getByPluginId(pluginId);
        ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), name);// TODO we need to check visibility here, the query currently doesnt do that
        if (token != null) {
            // TODO  confirmDownload0(version.id, Some(DownloadType.JarFile.value), Some(token)).orElseFail(notFound) *>
            return sendJar(project, versionsTable, token, true);
        } else {
            return sendJar(project, versionsTable, null, true);
        }
    }

    @GlobalPermission(NamedPermission.VIEW_LOGS)
    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versionLog")
    public ModelAndView showLog(@PathVariable String author, @PathVariable String slug, @RequestParam String versionString) {
        ModelAndView mv = new ModelAndView("projects/versions/log");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectVersionsTable version = versionService.getVersion(projectData.getProject().getId(), versionString);
        mv.addObject("project", projectData);
        mv.addObject("version", version);
        mv.addObject("visibilityChanges", versionService.getVersionVisibilityChanges(version.getId()));
        return fillModel(mv);
    }

    @GetMapping("/{author}/{slug}/versions")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/versions/list");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData sp = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("p", projectData);
        mav.addObject("channels", channelService.getProjectChannels(projectData.getProject().getId()));
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/new")
    public ModelAndView showCreator(@PathVariable String author, @PathVariable String slug, ModelMap modelMap) {
        ModelAndView mav = _showCreator(author, slug, null);
        AlertUtil.transferAlerts(mav, modelMap);
        return mav;
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/new/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        PendingVersion pendingVersion;
        try {
            pendingVersion = pluginUploadService.processSubsequentPluginUpload(file, projectData.getProjectOwner(), projectData.getProject());
        } catch (HangarException e) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return fillModel(mav);
        }
        return _showCreator(author, slug, pendingVersion);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/new/{versionName}")
    public ModelAndView showCreatorWithMeta(@PathVariable String author, @PathVariable String slug, @PathVariable String versionName) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        PendingVersion pendingVersion = cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).get(projectData.getProject().getId() + "/" + versionName, PendingVersion.class);

        if (pendingVersion == null) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.plugin.timeout");
            return fillModel(mav);
        }

        return _showCreator(author, slug, pendingVersion);
    }

    private ModelAndView _showCreator(String author, String slug, PendingVersion pendingVersion) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ModelAndView mav = new ModelAndView("projects/versions/create");
        mav.addObject("projectName", projectData.getProject().getName());
        mav.addObject("pluginId", projectData.getProject().getPluginId());
        mav.addObject("projectSlug", slug);
        mav.addObject("ownerName", author);
        mav.addObject("projectDescription", projectData.getProject().getDescription());
        mav.addObject("forumSync", projectData.getProject().getForumSync());
        mav.addObject("pending", pendingVersion);
        mav.addObject("channels", channelService.getProjectChannels(projectData.getProject().getId()));
        return fillModel(mav);
    }

    @RequestMapping(value = "/{author}/{slug}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommended(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String token) {
        ProjectsTable project = projectDao.get().getBySlug(author, slug);

        Long recommendedVersionId = project.getRecommendedVersionId();
        if (recommendedVersionId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), recommendedVersionId);// TODO we need to check visibility here, the query currently doesnt do that
            return sendVersion(project, versionsTable, token, false);
        }
    }

    @RequestMapping(value = "/{author}/{slug}/versions/recommended/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommendedJar(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String token) {
        ProjectsTable project = projectDao.get().getBySlug(author, slug);

        Long recommendedVersionId = project.getRecommendedVersionId();
        if (recommendedVersionId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), recommendedVersionId);// TODO we need to check visibility here, the query currently doesnt do that
            return sendJar(project, versionsTable, token, false);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version:.+}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView publish(@PathVariable String author,
                                @PathVariable String slug,
                                @PathVariable("version") String versionName,
                                @RequestParam(defaultValue = "false") boolean unstable,
                                @RequestParam(defaultValue = "false") boolean recommended,
                                @RequestParam("channel-input") String channelInput,
                                @RequestParam(value = "channel-color-input", required = false) Color channelColorInput,
                                @RequestParam(value = "non-reviewed", defaultValue = "false") boolean nonReviewed,
                                @RequestParam(value = "forum-post", defaultValue = "false") boolean forumPost,
                                @RequestParam(required = false) String content,
                                RedirectAttributes attributes) {
        Color channelColor = channelColorInput == null ? hangarConfig.channels.getColorDefault() : channelColorInput;
        ProjectData projectData = projectService.getProjectData(author, slug);
        PendingVersion pendingVersion = cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).get(projectData.getProject().getId() + "/" + versionName, PendingVersion.class);
        if (pendingVersion == null) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.plugin.timeout");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.showCreator", author, slug));
        }

        PendingVersion newPendingVersion = pendingVersion.copy(
                channelInput.trim(),
                channelColor,
                forumPost,
                content
        );

        if (versionService.exists(newPendingVersion)) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.plugin.versionExists");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.showCreator", author, slug));
        }

        ProjectChannelsTable channel = channelService.getProjectChannel(projectData.getProject().getId(), channelInput);
        ProjectVersionsTable version;
        try {
            if (channel == null) throw new HangarException("error.channel.invalidName", channelInput);
            version = newPendingVersion.complete(request, projectData, projectFactory);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessage(), e.getArgs());
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.showCreator", author, slug));
        }

        if (recommended) {
            projectData.getProject().setRecommendedVersionId(version.getId());
            projectDao.get().update(projectData.getProject());
        }

        if (unstable) {
            versionService.addUnstableTag(version.getId());
        }

        userActionLogService.version(request, LoggedActionType.VERSION_UPLOADED.with(VersionContext.of(projectData.getProject().getId(), version.getId())), "published", "null");

        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, versionName));
    }

    @GetMapping("/{author}/{slug}/versions/{version:.*}")
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String version, ModelMap modelMap) {
        ModelAndView mav = new ModelAndView("projects/versions/view");
        AlertUtil.transferAlerts(mav, modelMap);
        VersionData v = versionService.getVersionData(author, slug, version);
        ScopedProjectData sp = projectService.getScopedProjectData(v.getP().getProject().getId());
        mav.addObject("v", v);
        mav.addObject("sp", sp);
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/approve")
    public ModelAndView approve(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        return _approve(author, slug, version, false);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/approvePartial")
    public ModelAndView approvePartial(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        return _approve(author, slug, version, true);
    }

    private ModelAndView _approve(String author, String slug, String version, boolean partial) {
        ReviewState newState = partial ? ReviewState.PARTIALLY_REVIEWED : ReviewState.REVIEWED;
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        ReviewState oldState = versionsTable.getReviewState();
        versionsTable.setReviewState(newState);
        versionsTable.setReviewerId(userService.getCurrentUser().getId());
        versionsTable.setApprovedAt(OffsetDateTime.now());
        versionsTable.setVisibility(Visibility.PUBLIC);
        versionService.update(versionsTable);
        userActionLogService.version(request, LoggedActionType.VERSION_REVIEW_STATE_CHANGED.with(VersionContext.of(versionsTable.getProjectId(), versionsTable.getId())), newState.name(), oldState.name());
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, version));
    }

    @RequestMapping("/{author}/{slug}/versions/{version}/confirm")
    public Object showDownloadConfirm(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object downloadType, @RequestParam Object api, @RequestParam Object dummy) {
        return null; // TODO implement showDownloadConfirm request controller
    }

    @PostMapping(value = "/{author}/{slug}/versions/{version}/confirm", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object confirmDownload(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version, @RequestParam Object downloadType, @RequestParam Object api, @RequestParam Object dummy) {
        return null; // TODO implement confirmDownload request controller
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView softDelete(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment, RedirectAttributes ra) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        try {
            projectFactory.prepareDeleteVersion(versionData);
        } catch (HangarException e) {
            AlertUtil.showAlert(ra, AlertUtil.AlertType.ERROR, e.getMessage());
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, version));
        }

        Visibility oldVisibility = versionData.getV().getVisibility();
        versionService.changeVisibility(versionData, Visibility.SOFTDELETE, comment, userService.getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(versionData.getP().getProject().getId(), versionData.getV().getId())), "SoftDelete: " + comment, oldVisibility.getName());
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.showList", author, slug));
    }

    @RequestMapping(value = "/{author}/{slug}/versions/{version}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object download(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(required = false) String token, @RequestParam(defaultValue = "false") boolean confirm) {
        ProjectsTable project = projectDao.get().getBySlug(author, slug);
        ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), version);// TODO we need to check visibility here, the query currently doesnt do that
        return sendVersion(project, versionsTable, token, confirm);
    }

    private Object sendVersion(ProjectsTable project, ProjectVersionsTable version, String token, boolean confirm) {
        boolean passed = checkConfirmation(version, token);
        if (passed || confirm) {
            return _sendVersion(project, version);
        } else {
            return new RedirectView(routeHelper.getRouteUrl("versions.showDownloadConfirm",
                    project.getOwnerName(),
                    project.getSlug(),
                    version.getVersionString(),
                    DownloadType.UPLOADED_FILE.ordinal() + "",
                    false + "",
                    "dummy"));
        }
    }

    private boolean checkConfirmation(ProjectVersionsTable version, String token) {
        if (version.getReviewState() == ReviewState.REVIEWED) {
            return true;
        } else {
            boolean hasSessionConfirm = "confirmed".equals(request.getSession().getAttribute(ProjectVersionDownloadWarningsTable.cookieKey(version.getId())));

            if (hasSessionConfirm) {
                return true;
            } else {
                ProjectVersionDownloadWarningsTable warning = downloadWarningDao.get().find(token, version.getId(), RequestUtil.getRemoteAddress(request));

                if (warning.hasExpired()) {
                    downloadWarningDao.get().delete(warning);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    private FileSystemResource _sendVersion(ProjectsTable project, ProjectVersionsTable version) {
        // TODO version download stat

        Path path = projectFiles.getVersionDir(project.getOwnerName(), project.getName(), version.getVersionString()).resolve(version.getFileName());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + version.getFileName() + "\"");

        return new FileSystemResource(path);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/versions/{version}/hardDelete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement delete request controller
    }

    @RequestMapping(value = "/{author}/{slug}/versions/{version}/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadJar(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(required = false) String token) {
        ProjectsTable project = projectDao.get().getBySlug(author, slug);
        ProjectVersionsTable versionsTable = versionService.getVersion(project.getId(), version);// TODO we need to check visibility here, the query currently doesnt do that
        return sendJar(project, versionsTable, token, false);
    }

    private Object sendJar(ProjectsTable project, ProjectVersionsTable version, String token, boolean api) {
        if (project.getVisibility() == Visibility.SOFTDELETE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            boolean passed = checkConfirmation(version, token);

            if (!passed) {
                return new RedirectView(routeHelper.getRouteUrl("versions.showDownloadConfirm",
                        project.getOwnerName(),
                        project.getSlug(),
                        version.getVersionString(),
                        DownloadType.JAR_FILE.ordinal() + "",
                        api + "",
                        null));
            } else {
                String fileName = version.getFileName();
                Path path = projectFiles.getVersionDir(project.getOwnerName(), project.getName(), version.getVersionString()).resolve(fileName);

                // TODO download stats

                if (fileName.endsWith(".jar")) {
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + version.getFileName() + "\"");

                    return new FileSystemResource(path);
                } else {
                    // pluginDataService.openJar(path);

                    throw new RuntimeException("MiniDigger was too lazy to implement this dum feature of turning files into jars"); // TODO auto converting to jar
                }
            }
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/recommended")
    public ModelAndView setRecommended(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        versionData.getP().getProject().setRecommendedVersionId(versionData.getV().getId());
        projectDao.get().update(versionData.getP().getProject());
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, version));
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/restore", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView restore(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        versionService.changeVisibility(versionData, Visibility.PUBLIC, comment, userService.getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(versionData.getP().getProject().getId(), versionData.getV().getId())), "Restore: " + comment, "");
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, version));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/save", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView saveDescription(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String content) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        String oldDesc = versionData.getV().getDescription();
        String newDesc = content.trim();
        versionData.getV().setDescription(newDesc);
        versionService.update(versionData.getV());
        userActionLogService.version(request, LoggedActionType.VERSION_DESCRIPTION_CHANGED.with(VersionContext.of(versionData.getP().getProject().getId(), versionData.getV().getId())), newDesc, oldDesc);
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("versions.show", author, slug, version));
    }

}

