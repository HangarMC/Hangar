package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.forms.NewVersion;
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
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.DownloadType;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.DownloadsService;
import io.papermc.hangar.service.StatsService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.service.pluginupload.PluginUploadService;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.service.project.ProjectFactory;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.FileUtils;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.Routes;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Controller
public class VersionsController extends HangarController {

    private final ProjectService projectService;
    private final VersionService versionService;
    private final ProjectFactory projectFactory;
    private final StatsService statsService;
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
    private final Supplier<ProjectVersionsTable> projectVersionsTable;
    private final Supplier<VersionData> versionData;
    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;


    @Autowired
    public VersionsController(ProjectService projectService, VersionService versionService, ProjectFactory projectFactory, StatsService statsService, PluginUploadService pluginUploadService, ChannelService channelService, DownloadsService downloadsService, UserActionLogService userActionLogService, CacheManager cacheManager, HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, ProjectFiles projectFiles, HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao, MessageSource messageSource, ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response, Supplier<ProjectVersionsTable> projectVersionsTable, Supplier<VersionData> versionData, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.projectService = projectService;
        this.versionService = versionService;
        this.projectFactory = projectFactory;
        this.statsService = statsService;
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
        this.versionData = versionData;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @GlobalPermission(NamedPermission.VIEW_LOGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versionLog")
    public ModelAndView showLog(@PathVariable String author, @PathVariable String slug, @RequestParam String versionString) {
        ModelAndView mv = new ModelAndView("projects/versions/log");
        mv.addObject("project", projectData.get());
        mv.addObject("version", projectVersionsTable.get());
        mv.addObject("visibilityChanges", versionService.getVersionVisibilityChanges(projectVersionsTable.get().getId()));
        return fillModel(mv);
    }

    @GetMapping("/{author}/{slug}/versions")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/versions/list");
        ProjectData projData = projectData.get();
        ScopedProjectData sp = projectService.getScopedProjectData(projData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("p", projectData.get());
        mav.addObject("channels", channelService.getProjectChannels(projData.getProject().getId()));
        statsService.addProjectView(projData.getProject());
        return fillModel(mav);
    }

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/new")
    public ModelAndView showCreator(@PathVariable String author, @PathVariable String slug, ModelMap modelMap) {
        ModelAndView mav = _showCreator(author, slug, null);
        AlertUtil.transferAlerts(mav, modelMap);
        return mav;
    }

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/new/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelAndView upload(@PathVariable String author, @PathVariable String slug, @RequestParam("pluginFile") MultipartFile file) {
        ProjectData projData = projectData.get();
        String uploadError = projectFactory.getUploadError(getCurrentUser());
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

        PendingVersion pendingVersion;
        try {
            pendingVersion = pluginUploadService.processSubsequentPluginUpload(file, getCurrentUser(), projData.getProject());
        } catch (HangarException e) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return fillModel(mav);
        }
        return _showCreator(author, slug, pendingVersion);
    }

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/new/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView create(@PathVariable String author, @PathVariable String slug, @RequestParam String externalUrl) {
        ProjectData projData = projectData.get();
        if (!URL_PATTERN.matcher(externalUrl).matches()) {
            ModelAndView mav = _showCreator(author, slug, null);
            return fillModel(AlertUtil.showAlert(mav, AlertType.ERROR, "error.invalidUrl"));
        }

        ProjectChannelsTable channel = channelService.getFirstChannel(projData.getProject());
        PendingVersion pendingVersion = new PendingVersion(
                null,
                null,
                null,
                "",
                projData.getProject().getId(),
                null,
                null,
                null,
                projData.getProjectOwner().getId(),
                channel.getName(),
                channel.getColor(),
                null,
                externalUrl,
                false,
                versionService.getMostRelevantVersion(projData.getProject()));
        return _showCreator(author, slug, pendingVersion);
    }

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/new/{versionName}")
    public ModelAndView showCreatorWithMeta(@PathVariable String author, @PathVariable String slug, @PathVariable String versionName) {
        PendingVersion pendingVersion = cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).get(projectsTable.get().getId() + "/" + versionName, PendingVersion.class);

        if (pendingVersion == null) {
            ModelAndView mav = _showCreator(author, slug, null);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.plugin.timeout");
            return fillModel(mav);
        }

        return _showCreator(author, slug, pendingVersion);
    }

    private ModelAndView _showCreator(String author, String slug, PendingVersion pendingVersion) {
        ProjectData projData = projectData.get();
        ModelAndView mav = new ModelAndView("projects/versions/create");
        mav.addObject("projectName", projData.getProject().getName());
        mav.addObject("projectSlug", slug);
        mav.addObject("ownerName", author);
        mav.addObject("projectDescription", projData.getProject().getDescription());
        mav.addObject("forumSync", projData.getProject().getForumSync());
        mav.addObject("pending", pendingVersion);
        mav.addObject("channels", channelService.getProjectChannels(projData.getProject().getId()));
        return fillModel(mav);
    }

    @GetMapping(value = "/{author}/{slug}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommended(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String token) {
        ProjectsTable project = projectsTable.get();
        ProjectVersionsTable recommendedVersion = versionService.getRecommendedVersion(project);
        if (recommendedVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return sendVersion(project, recommendedVersion, token, false);
        }
    }

    @GetMapping(value = "/{author}/{slug}/versions/recommended/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommendedJar(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String token) {
        ProjectsTable project = projectsTable.get();
        ProjectVersionsTable recommendedVersion = versionService.getRecommendedVersion(project);
        if (recommendedVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return sendJar(project, recommendedVersion, token, false);
        }
    }

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/new/{version:.+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void saveNewVersion(@PathVariable String author, @PathVariable String slug, @PathVariable("version") String versionName, @RequestBody NewVersion newVersion) {
        ProjectsTable project = projectsTable.get();
        cacheManager.getCache(CacheConfig.NEW_VERSION_CACHE).put(project.getId() + "/" + versionName, newVersion);
    }

    @ProjectPermission(NamedPermission.CREATE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version:.+}")
    public ModelAndView publish(@PathVariable String author, @PathVariable String slug, @PathVariable("version") String versionName, RedirectAttributes attributes) {
        ProjectsTable project = projectsTable.get();
        PendingVersion pendingVersion = cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).get(project.getId() + "/" + versionName, PendingVersion.class);
        NewVersion newVersion = cacheManager.getCache(CacheConfig.NEW_VERSION_CACHE).get(project.getId() + "/" + versionName, NewVersion.class);
        if (newVersion == null || (pendingVersion == null && newVersion.getExternalUrl() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (pendingVersion == null && newVersion.getExternalUrl() != null) {
            pendingVersion = new PendingVersion(
                    newVersion.getVersionString(),
                    null,
                    null,
                    "",
                    project.getId(),
                    null,
                    null,
                    null,
                    getCurrentUser().getId(),
                    newVersion.getChannel().getName(),
                    newVersion.getChannel().getColor(),
                    null,
                    newVersion.getExternalUrl(),
                    newVersion.isForumSync(),
                    null
            );
        }
        return __publish(attributes, author, slug, versionName, newVersion, pendingVersion);
    }

    private ModelAndView __publish(RedirectAttributes attributes, String author, String slug, String versionName, @NotNull NewVersion newVersion, @NotNull PendingVersion pendingVersion) {
        ProjectData projData = projectData.get();

        if (newVersion.getPlatformDependencies().stream().anyMatch(s -> !s.getPlatform().getPossibleVersions().containsAll(s.getVersions()))) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.plugin.invalidVersion");
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        List<ProjectChannelsTable> projectChannels = channelService.getProjectChannels(projData.getProject().getId());
        String alertMsg = null;
        String[] alertArgs = new String[0];
        Optional<ProjectChannelsTable> channelOptional = projectChannels.stream().filter(ch -> ch.getName().equals(newVersion.getChannel().getName().trim())).findAny();
        ProjectChannelsTable channel;
        if (channelOptional.isEmpty()) {
            if (projectChannels.size() >= hangarConfig.projects.getMaxChannels()) {
                alertMsg = "error.channel.maxChannels";
                alertArgs = new String[]{String.valueOf(hangarConfig.projects.getMaxChannels())};
            } else if (projectChannels.stream().anyMatch(ch -> ch.getColor() == newVersion.getChannel().getColor())) {
                alertMsg = "error.channel.duplicateColor";
            }
            if (alertMsg != null) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, alertMsg, alertArgs);
                return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
            }
            channel = channelService.addProjectChannel(projData.getProject().getId(), newVersion.getChannel().getName().trim(), newVersion.getChannel().getColor(), newVersion.getChannel().isNonReviewed());
        } else {
            channel = channelOptional.get();
        }
        newVersion.getChannel().setColor(channel.getColor());
        newVersion.getChannel().setName(channel.getName());
        newVersion.getChannel().setNonReviewed(channel.getIsNonReviewed());

        PendingVersion updatedVersion = pendingVersion.update(newVersion);

        if (versionService.exists(updatedVersion)) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.plugin.versionExists");
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        ProjectVersionsTable version;
        try {
            version = updatedVersion.complete(request, projData, projectFactory);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessage(), e.getArgs());
            return Routes.VERSIONS_SHOW_CREATOR.getRedirect(author, slug);
        }

        if (newVersion.isRecommended()) {
            projData.getProject().setRecommendedVersionId(version.getId());
            projectDao.get().update(projData.getProject());
        }

        if (newVersion.isUnstable()) {
            versionService.addUnstableTag(version.getId());
        }

        userActionLogService.version(request, LoggedActionType.VERSION_UPLOADED.with(VersionContext.of(projData.getProject().getId(), version.getId())), "published", "");

        cacheManager.getCache(CacheConfig.NEW_VERSION_CACHE).evict(projData.getProject().getId() + "/" + versionName);
        cacheManager.getCache(CacheConfig.PENDING_VERSION_CACHE).evict(projData.getProject().getId() + "/" + versionName);
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version.getVersionStringUrl());
    }

    @GetMapping("/{author}/{slug}/versions/{version:.*}")
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathVariable String version, ModelMap modelMap) {
        ModelAndView mav = new ModelAndView("projects/versions/view");
        VersionData vData = versionData.get();
        AlertUtil.transferAlerts(mav, modelMap);
        ScopedProjectData sp = projectService.getScopedProjectData(vData.getP().getProject().getId());
        mav.addObject("v", vData);
        mav.addObject("sp", sp);
        statsService.addProjectView(vData.getP().getProject());
        return fillModel(mav);
    }

    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/approve")
    public ModelAndView approve(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        return _approve(projectVersionsTable.get(), author, slug, version, false);
    }

    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/approvePartial")
    public ModelAndView approvePartial(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        return _approve(projectVersionsTable.get(), author, slug, version, true);
    }

    private ModelAndView _approve(ProjectVersionsTable projectVersion, String author, String slug, String version, boolean partial) {
        if (projectVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ReviewState newState = partial ? ReviewState.PARTIALLY_REVIEWED : ReviewState.REVIEWED;
        ReviewState oldState = projectVersion.getReviewState();
        projectVersion.setReviewState(newState);
        projectVersion.setReviewerId(getCurrentUser().getId());
        projectVersion.setApprovedAt(OffsetDateTime.now());
        projectVersion.setVisibility(Visibility.PUBLIC);
        versionService.update(projectVersion);
        userActionLogService.version(request, LoggedActionType.VERSION_REVIEW_STATE_CHANGED.with(VersionContext.of(projectVersion.getProjectId(), projectVersion.getId())), newState.name(), oldState.name());
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

    @GetMapping("/{author}/{slug}/versions/{version}/confirm")
    public Object showDownloadConfirm(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(defaultValue = "0") DownloadType downloadType, @RequestParam(defaultValue = "false") boolean api, @RequestParam(required = false) String dummy) {
        ProjectVersionsTable versionsTable = projectVersionsTable.get();
        ProjectsTable project = projectsTable.get();
        if (versionsTable.getReviewState() == ReviewState.REVIEWED) {
            return AlertUtil.showAlert(Routes.PROJECTS_SHOW.getRedirect(author, slug), AlertType.ERROR, "error.plugin.stateChanged");
        }
        OffsetDateTime expiration = OffsetDateTime.now().plus(hangarConfig.projects.getUnsafeDownloadMaxAge().toMillis(), ChronoUnit.MILLIS);
        String token = UUID.randomUUID().toString();
        String address = RequestUtil.getRemoteAddress(request);

        String apiMsgKey = "version.download.confirm.body.api";
        if (versionsTable.getReviewState() == ReviewState.PARTIALLY_REVIEWED) {
            apiMsgKey = "version.download.confirmPartial.api";
        }
        String apiMsg = messageSource.getMessage(apiMsgKey, null, LocaleContextHolder.getLocale());
        String curlInstruction;
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken == null) {
            curlInstruction = messageSource.getMessage("version.download.confirm.curl.nocsrf", new String[]{Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null)}, LocaleContextHolder.getLocale());
        } else {
            curlInstruction = messageSource.getMessage("version.download.confirm.curl", new String[]{Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null), csrfToken.getToken()}, LocaleContextHolder.getLocale());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"README.txt\"");
        if (api) {
            removeAddWarnings(address, expiration, token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String downloadUrl = versionsTable.getExternalUrl() != null ? versionsTable.getExternalUrl() : Routes.VERSIONS_DOWNLOAD_JAR_BY_ID.getRouteUrl(project.getOwnerName(), project.getSlug(), versionsTable.getVersionStringUrl(), token);
            ObjectNode objectNode = mapper.createObjectNode()
                    .put("message", apiMsg)
                    .put("post", Routes.VERSIONS_CONFIRM_DOWNLOAD.getRouteUrl(author, slug, version, downloadType.ordinal() + "", token, null))
                    .put("url", downloadUrl)
                    .put("curl", curlInstruction)
                    .put("token", token);
            return new ResponseEntity<>(objectNode.toPrettyString(), headers, HttpStatus.MULTIPLE_CHOICES);
        } else {
            Optional<String> userAgent = Optional.ofNullable(request.getHeader(HttpHeaders.USER_AGENT)).map(String::toLowerCase);
            if (userAgent.isPresent() && userAgent.get().startsWith("wget/")) {
                return new ResponseEntity<>(messageSource.getMessage("version.download.confirm.wget", null, LocaleContextHolder.getLocale()), headers, HttpStatus.MULTIPLE_CHOICES);
            } else if (userAgent.isPresent() && userAgent.get().startsWith("curl/")) {
                removeAddWarnings(address, expiration, token);
                return new ResponseEntity<>(apiMsg + "\n" + curlInstruction + "\n", headers, HttpStatus.MULTIPLE_CHOICES);
            } else {
                ProjectChannelsTable channelsTable = channelService.getVersionsChannel(versionsTable.getProjectId(), versionsTable.getId());
                response.addCookie(new Cookie(ProjectVersionDownloadWarningsTable.cookieKey(versionsTable.getId()), "set"));
                ModelAndView mav = new ModelAndView("projects/versions/unsafeDownload");
                mav.addObject("project", project);
                mav.addObject("target", versionsTable);
                mav.addObject("isTargetChannelNonReviewed", channelsTable.getIsNonReviewed());
                mav.addObject("downloadType", downloadType);
                return fillModel(mav);
            }
        }
    }

    private void removeAddWarnings(String address, OffsetDateTime expiration, String token) {
        ProjectVersionsTable versionsTable = projectVersionsTable.get();
        downloadWarningDao.get().deleteInvalid(address, versionsTable.getId());
        downloadWarningDao.get().insert(new ProjectVersionDownloadWarningsTable(
                expiration,
                token,
                versionsTable.getId(),
                RequestUtil.getRemoteInetAddress(request)
        ));
    }

    @PostMapping(value = "/{author}/{slug}/versions/{version}/confirm", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ModelAndView confirmDownload(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(defaultValue = "0") DownloadType downloadType, @RequestParam Optional<String> token, @RequestParam(required = false) String dummy) {
        ProjectVersionsTable pvt = projectVersionsTable.get();
        if (pvt.getReviewState() == ReviewState.REVIEWED) {
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
            case EXTERNAL_DOWNLOAD:
                return new ModelAndView("redirect:" + pvt.getExternalUrl());
            default:
                throw new IllegalArgumentException();
        }
    }

    private ProjectVersionUnsafeDownloadsTable confirmDownload0(DownloadType downloadType, Optional<String> token) {
        if (token.isPresent()) {
            ProjectVersionDownloadWarningsTable warning = downloadsService.findUnconfirmedWarning(RequestUtil.getRemoteInetAddress(request), token.get(), projectVersionsTable.get().getId());
            if (warning == null) {
                throw new HangarException("error.plugin.noConfirmDownload");
            } else if (warning.hasExpired()) {
                downloadsService.deleteWarning(warning);
                throw new HangarException("error.plugin.noConfirmDownload");
            } else {
                ProjectVersionUnsafeDownloadsTable download = downloadsService.addUnsafeDownload(new ProjectVersionUnsafeDownloadsTable(currentUser.get().map(UsersTable::getId).orElse(null), RequestUtil.getRemoteInetAddress(request), downloadType));
                downloadsService.confirmWarning(warning, download.getId());
                return download;
            }
        } else {
            String cookieKey = ProjectVersionDownloadWarningsTable.cookieKey(projectVersionsTable.get().getId());
            Cookie cookie = WebUtils.getCookie(request, cookieKey);
            if (cookie != null && cookie.getValue().contains("set")) {
                cookie.setValue("confirmed");
                response.addCookie(cookie);
                return downloadsService.addUnsafeDownload(new ProjectVersionUnsafeDownloadsTable(currentUser.get().map(UsersTable::getId).orElse(null), RequestUtil.getRemoteInetAddress(request), downloadType));
            } else {
                throw new HangarException("error.plugin.noConfirmDownload");
            }
        }
    }

    @ProjectPermission(NamedPermission.DELETE_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView softDelete(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment, RedirectAttributes ra) {
        VersionData vData = versionData.get();
        try {
            projectFactory.prepareDeleteVersion(vData);
        } catch (HangarException e) {
            AlertUtil.showAlert(ra, AlertUtil.AlertType.ERROR, e.getMessage());
            return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
        }

        Visibility oldVisibility = vData.getV().getVisibility();
        versionService.changeVisibility(vData, Visibility.SOFTDELETE, comment, getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(vData.getP().getProject().getId(), vData.getV().getId())), "SoftDelete: " + comment, oldVisibility.getName());
        return Routes.VERSIONS_SHOW_LIST.getRedirect(author, slug);
    }

    @GetMapping(value = "/{author}/{slug}/versions/{version}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object download(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(required = false) String token, @RequestParam(defaultValue = "false") boolean confirm) {
        return sendVersion(projectsTable.get(), projectVersionsTable.get(), token, confirm);
    }

    private Object sendVersion(ProjectsTable project, ProjectVersionsTable version, String token, boolean confirm) {
        boolean isSafeExternalHost = version.isExternal() && hangarConfig.security.checkSafe(version.getExternalUrl());
        boolean passed = checkConfirmation(version, token);
        if (passed || confirm || isSafeExternalHost) {
            return _sendVersion(project, version);
        } else {
            return Routes.VERSIONS_SHOW_DOWNLOAD_CONFIRM.getRedirect(
                    project.getOwnerName(),
                    project.getSlug(),
                    version.getVersionStringUrl(),
                    (version.getExternalUrl() != null ? DownloadType.EXTERNAL_DOWNLOAD.ordinal() : DownloadType.UPLOADED_FILE.ordinal()) + "",
                    false + "",
                    "dummy"
            );
        }
    }

    private boolean checkConfirmation(ProjectVersionsTable version, String token) {
        if (version.getReviewState() == ReviewState.REVIEWED) {
            return true;
        } else {
            Optional<Cookie> cookie = Optional.ofNullable(WebUtils.getCookie(request, ProjectVersionDownloadWarningsTable.cookieKey(version.getId())));
            boolean hasSessionConfirm = "confirmed".equals(cookie.map(Cookie::getValue).orElse(""));
            if (hasSessionConfirm) {
                Cookie newCookie = cookie.get();
                newCookie.setMaxAge(0);
                response.addCookie(newCookie);
                return true;
            } else {
                ProjectVersionDownloadWarningsTable warning = downloadWarningDao.get().findConfirmedWarning(token, version.getId(), RequestUtil.getRemoteInetAddress(request));

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

    private Object _sendVersion(ProjectsTable project, ProjectVersionsTable version) {
        statsService.addVersionDownloaded(version);
        if (version.getExternalUrl() != null) {
            return new ModelAndView("redirect:" + version.getExternalUrl());
        }
        Path path = projectFiles.getVersionDir(project.getOwnerName(), project.getName(), version.getVersionString()).resolve(version.getFileName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + version.getFileName() + "\"");

        return new FileSystemResource(path);
    }

    @GlobalPermission(NamedPermission.HARD_DELETE_PROJECT)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/hardDelete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestBody String comment, RedirectAttributes ra) {
        VersionData vData = versionData.get();
        try {
            projectFactory.prepareDeleteVersion(vData);
        } catch (HangarException e) {
            AlertUtil.showAlert(ra, AlertUtil.AlertType.ERROR, e.getMessage());
            return Routes.VERSIONS_SHOW_LIST.getRedirect(author, slug);
        }
        Path versionDir = projectFiles.getVersionDir(vData.getP().getOwnerName(), vData.getP().getProject().getSlug(), vData.getV().getVersionString());
        FileUtils.deleteDirectory(versionDir);
        versionService.deleteVersion(vData.getV().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(vData.getV().getProjectId(), vData.getV().getId())), "Deleted: " + comment, vData.getV().getVisibility().getName());
        // Ore deletes the channel if no more versions are left, I don't think that is a good idea, easy enough to delete the channel manually.
        return Routes.VERSIONS_SHOW_LIST.getRedirect(author, slug);
    }

    @GetMapping(value = "/{author}/{slug}/versions/{version}/jar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadJar(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam(required = false) String token) {
        return sendJar(projectsTable.get(), projectVersionsTable.get(), token, false);
    }

    private Object sendJar(ProjectsTable project, ProjectVersionsTable version, String token, boolean api) {
        if (project.getVisibility() == Visibility.SOFTDELETE || version.isExternal()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            boolean passed = checkConfirmation(version, token);

            if (!passed) {
                return Routes.VERSIONS_SHOW_DOWNLOAD_CONFIRM.getRedirect(
                        project.getOwnerName(),
                        project.getSlug(),
                        version.getVersionStringUrl(),
                        DownloadType.JAR_FILE.ordinal() + "",
                        api + "",
                        null
                );
            } else {
                String fileName = version.getFileName();
                Path path = projectFiles.getVersionDir(project.getOwnerName(), project.getName(), version.getVersionString()).resolve(fileName);

                statsService.addVersionDownloaded(version);
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

    @ProjectPermission(NamedPermission.EDIT_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/recommended")
    public ModelAndView setRecommended(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectsTable project = projectsTable.get();
        ProjectVersionsTable versionsTable = projectVersionsTable.get();
        project.setRecommendedVersionId(versionsTable.getId());
        projectDao.get().update(project);
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/restore", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView restore(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String comment) {
        VersionData vData = versionData.get();
        versionService.changeVisibility(vData, Visibility.PUBLIC, comment, getCurrentUser().getId());
        userActionLogService.version(request, LoggedActionType.VERSION_DELETED.with(VersionContext.of(vData.getP().getProject().getId(), vData.getV().getId())), "Restore: " + comment, "");
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

    @ProjectPermission(NamedPermission.EDIT_VERSION)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/save", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView saveDescription(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String content) {
        VersionData vData = versionData.get();
        String oldDesc = vData.getV().getDescription();
        String newDesc = content.trim();
        vData.getV().setDescription(newDesc);
        versionService.update(vData.getV());
        userActionLogService.version(request, LoggedActionType.VERSION_DESCRIPTION_CHANGED.with(VersionContext.of(vData.getP().getProject().getId(), vData.getV().getId())), newDesc, oldDesc);
        return Routes.VERSIONS_SHOW.getRedirect(author, slug, version);
    }

}

