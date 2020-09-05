package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.ProjectVersionDownloadWarningDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionDownloadWarningsTable;
import io.papermc.hangar.db.model.ProjectVersionUnsafeDownloadsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.DownloadType;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.service.DownloadsService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.service.pluginupload.PluginUploadService;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.service.project.ProjectFactory;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
public class VersionsController extends HangarController {

    private final ProjectService projectService;
    private final VersionService versionService;
    private final ProjectFactory projectFactory;
    private final UserService userService;
    private final PluginUploadService pluginUploadService;
    private final ChannelService channelService;
    private final DownloadsService downloadsService;
    private final UserActionLogService userActionLogService;
    private final CacheManager cacheManager;
    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectDao> projectDao;
    private final ProjectFiles projectFiles;
    private final HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao;
    private final MessageSource messageSource;
    private final ObjectMapper mapper;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final ProjectVersionsTable projectVersionsTable;
    private final ProjectsTable projectsTable;

    @Autowired(required = false)
    public VersionsController(ProjectService projectService, VersionService versionService, ProjectFactory projectFactory, UserService userService, PluginUploadService pluginUploadService, ChannelService channelService, DownloadsService downloadsService, UserActionLogService userActionLogService, CacheManager cacheManager, HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, ProjectFiles projectFiles, HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao, MessageSource messageSource, ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response, ProjectVersionsTable projectVersionsTable, ProjectsTable projectsTable) {
        this.projectService = projectService;
        this.versionService = versionService;
        this.projectFactory = projectFactory;
        this.userService = userService;
        this.pluginUploadService = pluginUploadService;
        this.channelService = channelService;
        this.downloadsService = downloadsService;
        this.userActionLogService = userActionLogService;
        this.cacheManager = cacheManager;
        this.hangarConfig = hangarConfig;
        this.projectDao = projectDao;
        this.projectFiles = projectFiles;
        this.downloadWarningDao = downloadWarningDao;
        this.messageSource = messageSource;
        this.mapper = mapper;
        this.request = request;
        this.response = response;
        this.projectVersionsTable = projectVersionsTable;
        this.projectsTable = projectsTable;
    }

    @Bean
    @RequestScope
    ProjectVersionsTable projectVersionsTable() {
        Map<String, String> pathParams = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!pathParams.keySet().containsAll(Set.of("author", "slug", "version"))) {
            return null;
        } else {
            ProjectVersionsTable projectVersionsTable = versionService.getVersion(pathParams.get("author"), pathParams.get("slug"), pathParams.get("version"));
            if (projectVersionsTable == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return projectVersionsTable;
        }
    }

    @GetMapping(value = "/api/project/{pluginId}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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

    @GetMapping(value = "/api/project/{pluginId}/versions/{name}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
    @GetMapping("/{author}/{slug}/versionLog")
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

    @GetMapping(value = "/{author}/{slug}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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

    @GetMapping(value = "/{author}/{slug}/versions/recommended/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        List<ProjectChannelsTable> projectChannels = channelService.getProjectChannels(projectData.getProject().getId());
        String alertMsg = null;
        String[] alertArgs = new String[0];
        Optional<ProjectChannelsTable> channelOptional = projectChannels.stream().filter(ch -> ch.getName().equals(channelInput.trim())).findAny();
        ProjectChannelsTable channel;
        if (channelOptional.isEmpty()) {
            if (projectChannels.size() >= hangarConfig.projects.getMaxChannels()) {
                alertMsg = "error.channel.maxChannels";
                alertArgs = new String[] {String.valueOf(hangarConfig.projects.getMaxChannels())};
            }
            else if (projectChannels.stream().anyMatch(ch -> ch.getColor() == channelColor)) {
                alertMsg = "error.channel.duplicateColor";
            }
            if (alertMsg != null) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, alertMsg, alertArgs);
                return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
            }
            channel = channelService.addProjectChannel(projectData.getProject().getId(), channelInput.trim(), channelColor);
        } else {
            channel = channelOptional.get();
        }

        PendingVersion newPendingVersion = pendingVersion.copy(
                channel.getName(),
                channel.getColor(),
                forumPost,
                content
        );

        if (versionService.exists(newPendingVersion)) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.plugin.versionExists");
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        ProjectVersionsTable version;
        try {
            version = newPendingVersion.complete(request, projectData, projectFactory);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessage(), e.getArgs());
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        if (recommended) {
            projectData.getProject().setRecommendedVersionId(version.getId());
            projectDao.get().update(projectData.getProject());
        }

        if (unstable) {
            versionService.addUnstableTag(version.getId());
        }

        userActionLogService.version(request, LoggedActionType.VERSION_UPLOADED.with(VersionContext.of(projectData.getProject().getId(), version.getId())), "published", "");

        return Routes.VERSIONS_SHOW.getRedirect(author, slug, versionName);
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
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

    @GetMapping("/{author}/{slug}/versions/{version}/confirm")
    public Object showDownloadConfirm(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(defaultValue = "0") DownloadType downloadType, @RequestParam Optional<Boolean> api, @RequestParam(required = false) String dummy) {
        if (projectVersionsTable.getReviewState() == ReviewState.REVIEWED) {
            return AlertUtil.showAlert(Routes.PROJECTS_SHOW.getRedirect(author, slug), AlertType.ERROR, "error.plugin.stateChanged");
        }
        OffsetDateTime expiration = OffsetDateTime.now().plus(hangarConfig.projects.getUnsafeDownloadMaxAge().toMillis(), ChronoUnit.MILLIS);
        String token = UUID.randomUUID().toString();
        String address = RequestUtil.getRemoteAddress(request);

        String apiMsgKey = "version.download.confirm.body.api";
        if (projectVersionsTable.getReviewState() == ReviewState.PARTIALLY_REVIEWED) {
            apiMsgKey = "version.download.confirmPartial.api";
        }
        String apiMsg = messageSource.getMessage(apiMsgKey, null, LocaleContextHolder.getLocale());
        String curlInstruction;
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken == null) {
            curlInstruction = messageSource.getMessage("version.download.confirm.curl.nocsrf", new String[] {Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null)}, LocaleContextHolder.getLocale());
        } else {
            curlInstruction = messageSource.getMessage("version.download.confirm.curl", new String[] {Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null), csrfToken.getToken()}, LocaleContextHolder.getLocale());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"README.txt\"");
        if (api.orElse(false)) {
            removeAddWarnings(address, expiration, token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("message", apiMsg);
            objectNode.put("post", Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null));
            objectNode.put("url", Routes.VERSIONS_DOWNLOAD_JAR_BY_ID.getRouteUrl(projectsTable.getPluginId(), projectVersionsTable.getVersionString(), token));
            objectNode.put("curl", curlInstruction);
            objectNode.put("token", token);
            return new ResponseEntity<>(objectNode.toPrettyString(), headers, HttpStatus.MULTIPLE_CHOICES);
        } else {
            Optional<String> userAgent = Optional.ofNullable(request.getHeader(HttpHeaders.USER_AGENT)).map(String::toLowerCase);
            if (userAgent.isPresent() && userAgent.get().startsWith("wget/")) {
                return new ResponseEntity<>(messageSource.getMessage("version.download.confirm.wget", null, LocaleContextHolder.getLocale()), headers, HttpStatus.MULTIPLE_CHOICES);
            } else if (userAgent.isPresent() && userAgent.get().startsWith("curl/")) {
                removeAddWarnings(address, expiration, token);
                return new ResponseEntity<>(apiMsg + "\n" + curlInstruction + "\n", headers, HttpStatus.MULTIPLE_CHOICES);
            } else {
                ProjectChannelsTable channelsTable = channelService.getVersionsChannel(projectVersionsTable.getProjectId(), projectVersionsTable.getId());
                response.addCookie(new Cookie(ProjectVersionDownloadWarningsTable.cookieKey(projectVersionsTable.getId()), "set"));
                ModelAndView mav = new ModelAndView("projects/versions/unsafeDownload");
                mav.addObject("project", projectsTable);
                mav.addObject("target", projectVersionsTable);
                mav.addObject("isTargetChannelNonReviewed", channelsTable.getIsNonReviewed());
                mav.addObject("downloadType", downloadType);
                return fillModel(mav);
            }
        }
    }

    private void removeAddWarnings(String address, OffsetDateTime expiration, String token) {
        downloadWarningDao.get().deleteInvalid(address, projectVersionsTable.getId());
        downloadWarningDao.get().insert(new ProjectVersionDownloadWarningsTable(
                expiration,
                token,
                projectVersionsTable.getId(),
                RequestUtil.getRemoteInetAddress(request)
        ));
    }

    @PostMapping(value = "/{author}/{slug}/versions/{version}/confirm", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object confirmDownload(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(defaultValue = "0") DownloadType downloadType, @RequestParam Optional<String> token, @RequestParam(required = false) String dummy) {
        if (projectVersionsTable.getReviewState() == ReviewState.REVIEWED) {
            return Routes.PROJECTS_SHOW.getRedirect(author, slug);
        }
        ProjectVersionUnsafeDownloadsTable download;
        try {
            download = confirmDownload0(downloadType, token);
        } catch (HangarException e) {
            return AlertUtil.showAlert(Routes.PROJECTS_SHOW.getRedirect(author, slug), AlertType.ERROR, e.getMessageKey(), e.getArgs());
        }
        switch (download.getDownloadType()) {
            case UPLOADED_FILE:
                return Routes.VERSIONS_DOWNLOAD.getRedirect(author, slug, version, token.orElse(null), "");
            case JAR_FILE:
                return Routes.VERSIONS_DOWNLOAD_JAR.getRedirect(author, slug, version, token.orElse(null), "");
            default:
                throw new IllegalArgumentException();
        }
    }

    private ProjectVersionUnsafeDownloadsTable confirmDownload0(DownloadType downloadType, Optional<String> token) {
        if (token.isPresent()) {
            ProjectVersionDownloadWarningsTable warning = downloadsService.findUnconfirmedWarning(RequestUtil.getRemoteInetAddress(request), token.get(), projectVersionsTable.getId());
            if (warning == null) {
                throw new HangarException("error.plugin.noConfirmDownload");
            } else if (warning.hasExpired()) {
                downloadsService.deleteWarning(warning);
                throw new HangarException("error.plugin.noConfirmDownload");
            } else {
                ProjectVersionUnsafeDownloadsTable download = downloadsService.addUnsafeDownload(new ProjectVersionUnsafeDownloadsTable(userService.currentUser().map(UsersTable::getId).orElse(null), RequestUtil.getRemoteInetAddress(request), downloadType));
                downloadsService.confirmWarning(warning, download.getId());
                return download;
            }
        } else {
            String cookieKey = ProjectVersionDownloadWarningsTable.cookieKey(projectVersionsTable.getId());
            Cookie cookie = WebUtils.getCookie(request, cookieKey);
            if (cookie != null && cookie.getValue().contains("set")) {
                cookie.setValue("confirmed");
                response.addCookie(cookie);
                return downloadsService.addUnsafeDownload(new ProjectVersionUnsafeDownloadsTable(userService.currentUser().map(UsersTable::getId).orElse(null), RequestUtil.getRemoteInetAddress(request), downloadType));
            } else {
                throw new HangarException("error.plugin.noConfirmDownload");
            }
        }
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView softDelete(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment, RedirectAttributes ra) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        try {
            projectFactory.prepareDeleteVersion(versionData);
        } catch (HangarException e) {
            AlertUtil.showAlert(ra, AlertUtil.AlertType.ERROR, e.getMessage());
            return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
        }

        Visibility oldVisibility = versionData.getV().getVisibility();
        versionService.changeVisibility(versionData, Visibility.SOFTDELETE, comment, userService.getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(versionData.getP().getProject().getId(), versionData.getV().getId())), "SoftDelete: " + comment, oldVisibility.getName());
        return Routes.VERSIONS_SHOW_LIST.getRedirect(author, slug);
    }

    @GetMapping(value = "/{author}/{slug}/versions/{version}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
            return new RedirectView(Routes.getRouteUrlOf("versions.showDownloadConfirm",
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
            String cookie = Optional.ofNullable(WebUtils.getCookie(request, ProjectVersionDownloadWarningsTable.cookieKey(version.getId()))).map(Cookie::getValue).orElse("");
            boolean hasSessionConfirm = "confirmed".equals(cookie);
            if (hasSessionConfirm) {
                return true;
            } else {
                ProjectVersionDownloadWarningsTable warning = downloadWarningDao.get().find(token, version.getId(), RequestUtil.getRemoteInetAddress(request));

                if (warning == null) {
                    return false;
                } else if (warning.hasExpired()) {
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
    @PostMapping("/{author}/{slug}/versions/{version}/hardDelete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object version) {
        return null; // TODO implement delete request controller
    }

    @GetMapping(value = "/{author}/{slug}/versions/{version}/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
                return new RedirectView(Routes.getRouteUrlOf("versions.showDownloadConfirm",
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
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/restore", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView restore(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment) {
        VersionData versionData = versionService.getVersionData(author, slug, version);
        versionService.changeVisibility(versionData, Visibility.PUBLIC, comment, userService.getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(versionData.getP().getProject().getId(), versionData.getV().getId())), "Restore: " + comment, "");
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
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
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

}

