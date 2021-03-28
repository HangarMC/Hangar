package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectVersionDownloadWarningDao;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.db.modelold.ProjectVersionDownloadWarningsTable;
import io.papermc.hangar.db.modelold.ProjectVersionUnsafeDownloadsTable;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.DownloadType;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.VersionData;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.securityold.annotations.ProjectPermission;
import io.papermc.hangar.securityold.annotations.UserLock;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.serviceold.DownloadsService;
import io.papermc.hangar.serviceold.StatsService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.VersionService;
import io.papermc.hangar.serviceold.VersionService.RecommendedVersionService;
import io.papermc.hangar.serviceold.project.ChannelService;
import io.papermc.hangar.serviceold.project.ProjectFactory;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.FileUtils;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Controller("oldVersionsController")
public class VersionsController extends HangarController {

    private final VersionService versionService;
    private final RecommendedVersionService recommendedVersionService;
    private final ProjectFactory projectFactory;
    private final StatsService statsService;
    private final ChannelService channelService;
    private final DownloadsService downloadsService;
    private final UserActionLogService userActionLogService;
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
    public VersionsController(VersionService versionService, RecommendedVersionService recommendedVersionService, ProjectFactory projectFactory, StatsService statsService, ChannelService channelService, DownloadsService downloadsService, UserActionLogService userActionLogService, HangarConfig hangarConfig, HangarDao<ProjectDao> projectDao, ProjectFiles projectFiles, HangarDao<ProjectVersionDownloadWarningDao> downloadWarningDao, MessageSource messageSource, ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response, Supplier<ProjectVersionsTable> projectVersionsTable, Supplier<VersionData> versionData, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.versionService = versionService;
        this.recommendedVersionService = recommendedVersionService;
        this.projectFactory = projectFactory;
        this.statsService = statsService;
        this.channelService = channelService;
        this.downloadsService = downloadsService;
        this.userActionLogService = userActionLogService;
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

    @GetMapping(value = "/{author}/{slug}/versions/recommended/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public Object downloadRecommended(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String token) {
        ProjectsTable project = projectsTable.get();
        ProjectVersionsTable recommendedVersion = recommendedVersionService.getRecommendedVersion(project);
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
        ProjectVersionsTable recommendedVersion = recommendedVersionService.getRecommendedVersion(project);
        if (recommendedVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return sendJar(project, recommendedVersion, token, false);
        }
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
                mav.addObject("isTargetChannelNonReviewed", channelsTable.isNonReviewed());
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
}

