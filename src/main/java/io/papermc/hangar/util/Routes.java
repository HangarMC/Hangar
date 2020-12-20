package io.papermc.hangar.util;

import org.springframework.web.servlet.ModelAndView;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.List.of;

public enum Routes {

    SHOW_PROJECT_VISIBILITY("showProjectVisibility", Paths.SHOW_PROJECT_VISIBILITY, of(), of()),
    ACTOR_COUNT("actorCount", Paths.ACTOR_COUNT, of(), of("timeoutMs")),
    ACTOR_TREE("actorTree", Paths.ACTOR_TREE, of(), of("timeoutMs")),
    UPDATE_USER("updateUser", Paths.UPDATE_USER, of("user"), of()),
    SHOW_QUEUE("showQueue", Paths.SHOW_QUEUE, of(), of()),
    SHOW_LOG("showLog", Paths.SHOW_LOG, of(), of("page", "userFilter", "projectFilter", "versionFilter", "pageFilter", "actionFilter", "subjectFilter")),
    SHOW_PLATFORM_VERSIONS("showPlatformVersions", Paths.SHOW_PLATFORM_VERSIONS, of(), of()),
    UPDATE_PLATFORM_VERSIONS("updatePlatformVersions", Paths.UPDATE_PLATFORM_VERSIONS, of("platform"), of()),
    REMOVE_TRAIL("removeTrail", Paths.REMOVE_TRAIL, of("path"), of()),
    FAVICON_REDIRECT("faviconRedirect", Paths.FAVICON_REDIRECT, of(), of()),
    SHOW_FLAGS("showFlags", Paths.SHOW_FLAGS, of(), of()),
    SITEMAP_INDEX("sitemapIndex", Paths.SITEMAP_INDEX, of(), of()),
    GLOBAL_SITEMAP("globalSitemap", Paths.GLOBAL_SITEMAP, of(), of()),
    SHOW_STATS("showStats", Paths.SHOW_STATS, of(), of("from", "to")),
    LINK_OUT("linkOut", Paths.LINK_OUT, of(), of("remoteUrl")),
    SHOW_HEALTH("showHealth", Paths.SHOW_HEALTH, of(), of()),
    SHOW_HOME("showHome", Paths.SHOW_HOME, of(), of()),
    ROBOTS("robots", Paths.ROBOTS, of(), of()),
    SET_FLAG_RESOLVED("setFlagResolved", Paths.SET_FLAG_RESOLVED, of("id", "resolved"), of()),
    SWAGGER("swagger", Paths.SWAGGER, of(), of()),
    SHOW_ACTIVITIES("showActivities", Paths.SHOW_ACTIVITIES, of("user"), of()),
    USER_ADMIN("userAdmin", Paths.USER_ADMIN, of("user"), of()),

    PROJECTS_RENAME("projects.rename", Paths.PROJECTS_RENAME, of("author", "slug"), of()),
    PROJECTS_SET_WATCHING("projects.setWatching", Paths.PROJECTS_SET_WATCHING, of("author", "slug", "watching"), of()),
    PROJECTS_SHOW_SETTINGS("projects.showSettings", Paths.PROJECTS_SHOW_SETTINGS, of("author", "slug"), of()),
    PROJECTS_SET_INVITE_STATUS("projects.setInviteStatus", Paths.PROJECTS_SET_INVITE_STATUS, of("id", "status"), of()),
    PROJECTS_TOGGLE_STARRED("projects.toggleStarred", Paths.PROJECTS_TOGGLE_STARRED, of("author", "slug"), of()),
    PROJECTS_VALIDATE_NAME("projects.validateName", Paths.PROJECTS_VALIDATE_NAME, of(), of()),
    PROJECTS_SHOW_CREATOR("projects.showCreator", Paths.PROJECTS_SHOW_CREATOR, of(), of()),
    PROJECTS_SHOW_STARGAZERS("projects.showStargazers", Paths.PROJECTS_SHOW_STARGAZERS, of("author", "slug"), of("page")),
    PROJECTS_SHOW_WATCHERS("projects.showWatchers", Paths.PROJECTS_SHOW_WATCHERS, of("author", "slug"), of("page")),
    PROJECTS_UPLOAD_ICON("projects.uploadIcon", Paths.PROJECTS_UPLOAD_ICON, of("author", "slug"), of()),
    PROJECTS_SHOW_ICON("projects.showIcon", Paths.PROJECTS_SHOW_ICON, of("author", "slug"), of()),
    PROJECTS_SEND_FOR_APPROVAL("projects.sendForApproval", Paths.PROJECTS_SEND_FOR_APPROVAL, of("author", "slug"), of()),
    PROJECTS_SET_INVITE_STATUS_ON_BEHALF("projects.setInviteStatusOnBehalf", Paths.PROJECTS_SET_INVITE_STATUS_ON_BEHALF, of("id", "status", "behalf"), of()),
    PROJECTS_DELETE("projects.delete", Paths.PROJECTS_DELETE, of("author", "slug"), of()),
    PROJECTS_ADD_MESSAGE("projects.addMessage", Paths.PROJECTS_ADD_MESSAGE, of("author", "slug"), of()),
    PROJECTS_SHOW_PENDING_ICON("projects.showPendingIcon", Paths.PROJECTS_SHOW_PENDING_ICON, of("author", "slug"), of()),
    PROJECTS_POST_DISCUSSION_REPLY("projects.postDiscussionReply", Paths.PROJECTS_POST_DISCUSSION_REPLY, of("author", "slug"), of()),
    PROJECTS_SHOW("projects.show", Paths.PROJECTS_SHOW, of("author", "slug"), of()),
    PROJECTS_SHOW_DISCUSSION("projects.showDiscussion", Paths.PROJECTS_SHOW_DISCUSSION, of("author", "slug"), of()),
    PROJECTS_SOFT_DELETE("projects.softDelete", Paths.PROJECTS_SOFT_DELETE, of("author", "slug"), of()),
    PROJECTS_SHOW_FLAGS("projects.showFlags", Paths.PROJECTS_SHOW_FLAGS, of("author", "slug"), of()),
    PROJECTS_FLAG("projects.flag", Paths.PROJECTS_FLAG, of("author", "slug"), of()),
    PROJECTS_CREATE_PROJECT("projects.createProject", Paths.PROJECTS_CREATE_PROJECT, of(), of()),
    PROJECTS_RESET_ICON("projects.resetIcon", Paths.PROJECTS_RESET_ICON, of("author", "slug"), of()),
    PROJECTS_SAVE("projects.save", Paths.PROJECTS_SAVE, of("author", "slug"), of()),
    PROJECTS_SHOW_NOTES("projects.showNotes", Paths.PROJECTS_SHOW_NOTES, of("author", "slug"), of()),
    PROJECTS_SET_VISIBLE("projects.setVisible", Paths.PROJECTS_SET_VISIBLE, of("author", "slug", "visibility"), of()),
    PROJECTS_REMOVE_MEMBER("projects.removeMember", Paths.PROJECTS_REMOVE_MEMBER, of("author", "slug"), of()),

    VERSIONS_RESTORE("versions.restore", Paths.VERSIONS_RESTORE, of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_RECOMMENDED_JAR("versions.downloadRecommendedJar", Paths.VERSIONS_DOWNLOAD_RECOMMENDED_JAR, of("author", "slug"), of("token")),
    VERSIONS_SAVE_NEW_VERSION("versions.saveNewVersion", Paths.VERSIONS_SAVE_NEW_VERSION, of("author", "slug", "version"), of()),
    VERSIONS_PUBLISH("versions.publish", Paths.VERSIONS_PUBLISH, of("author", "slug", "version"), of()),
    VERSIONS_PUBLISH_URL("versions.publishUrl", Paths.VERSIONS_PUBLISH_URL, of("author", "slug"), of()),
    VERSIONS_SET_RECOMMENDED("versions.setRecommended", Paths.VERSIONS_SET_RECOMMENDED, of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD("versions.download", Paths.VERSIONS_DOWNLOAD, of("author", "slug", "version"), of("token", "confirm")),
    VERSIONS_SHOW_LOG("versions.showLog", Paths.VERSIONS_SHOW_LOG, of("author", "slug"), of("versionString")),
    VERSIONS_SHOW("versions.show", Paths.VERSIONS_SHOW, of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_JAR("versions.downloadJar", Paths.VERSIONS_DOWNLOAD_JAR, of("author", "slug", "version"), of("token")),
    VERSIONS_APPROVE("versions.approve", Paths.VERSIONS_APPROVE, of("author", "slug", "version"), of()),
    VERSIONS_APPROVE_PARTIAL("versions.approvePartial", Paths.VERSIONS_APPROVE_PARTIAL, of("author", "slug", "version"), of()),
    VERSIONS_SAVE_DESCRIPTION("versions.saveDescription", Paths.VERSIONS_SAVE_DESCRIPTION, of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_RECOMMENDED("versions.downloadRecommended", Paths.VERSIONS_DOWNLOAD_RECOMMENDED, of("author", "slug"), of("token")),
    VERSIONS_SHOW_LIST("versions.showList", Paths.VERSIONS_SHOW_LIST, of("author", "slug"), of()),
    VERSIONS_DOWNLOAD_JAR_BY_ID("versions.downloadJarById", Paths.VERSIONS_DOWNLOAD_JAR_BY_ID, of("author", "slug", "name"), of("token")),
    VERSIONS_DOWNLOAD_RECOMMENDED_JAR_BY_ID("versions.downloadRecommendedJarById", Paths.VERSIONS_DOWNLOAD_RECOMMENDED_JAR_BY_ID, of("author", "slug"), of("token")),
    VERSIONS_UPLOAD("versions.upload", Paths.VERSIONS_UPLOAD, of("author", "slug"), of()),
    VERSIONS_CREATE_EXTERNAL_URL("versions.createExternalUrl", Paths.VERSIONS_CREATE_EXTERNAL_URL, of("author", "slug"), of()),
    VERSIONS_SOFT_DELETE("versions.softDelete", Paths.VERSIONS_SOFT_DELETE, of("author", "slug", "version"), of()),
    VERSIONS_SHOW_DOWNLOAD_CONFIRM("versions.showDownloadConfirm", Paths.VERSIONS_SHOW_DOWNLOAD_CONFIRM, of("author", "slug", "version"), of("downloadType", "api", "dummy")),
    VERSIONS_SHOW_CREATOR("versions.showCreator", Paths.VERSIONS_SHOW_CREATOR, of("author", "slug"), of()),
    VERSIONS_DELETE("versions.delete", Paths.VERSIONS_DELETE, of("author", "slug", "version"), of()),
    VERSIONS_SHOW_CREATOR_WITH_META("versions.showCreatorWithMeta", Paths.VERSIONS_SHOW_CREATOR_WITH_META, of("author", "slug", "version"), of()),
    VERSIONS_CONFIRM_DOWNLOAD("versions.confirmDownload", Paths.VERSIONS_CONFIRM_DOWNLOAD, of("author", "slug", "version"), of("downloadType", "token", "dummy")),

    PAGES_SHOW_PREVIEW("pages.showPreview", Paths.PAGES_SHOW_PREVIEW, of(), of()),
    PAGES_BB_CONVERT("pages.bbConvert", Paths.PAGES_BB_CONVERT, of(), of()),
    PAGES_SAVE("pages.save", Paths.PAGES_SAVE, of("author", "slug", "page"), of()),
    PAGES_SHOW_EDITOR("pages.showEditor", Paths.PAGES_SHOW_EDITOR, of("author", "slug", "page"), of()),
    PAGES_SHOW("pages.show", Paths.PAGES_SHOW, of("author", "slug", "page"), of()),
    PAGES_DELETE("pages.delete", Paths.PAGES_DELETE, of("author", "slug", "page"), of()),

    USERS_SHOW_AUTHORS("users.showAuthors", Paths.USERS_SHOW_AUTHORS, of(), of("sort", "page")),
    USERS_SAVE_TAGLINE("users.saveTagline", Paths.USERS_SAVE_TAGLINE, of("user"), of()),
    USERS_SIGN_UP("users.signUp", Paths.USERS_SIGN_UP, of(), of()),
    USERS_SHOW_NOTIFICATIONS("users.showNotifications", Paths.USERS_SHOW_NOTIFICATIONS, of(), of("notificationFilter", "inviteFilter")),
    USERS_SHOW_PROJECTS("users.showProjects", Paths.USERS_SHOW_PROJECTS, of("user"), of()),
    USERS_VERIFY("users.verify", Paths.USERS_VERIFY, of(), of("returnPath")),
    USERS_MARK_NOTIFICATION_READ("users.markNotificationRead", Paths.USERS_MARK_NOTIFICATION_READ, of("id"), of()),
    USERS_LOGIN("users.login", Paths.USERS_LOGIN, of(), of("sso", "sig", "returnUrl")),
    USERS_SHOW_STAFF("users.showStaff", Paths.USERS_SHOW_STAFF, of(), of("sort", "page")),
    USERS_SET_LOCKED("users.setLocked", Paths.USERS_SET_LOCKED, of("user", "locked"), of("sso", "sig")),
    USERS_MARK_PROMPT_READ("users.markPromptRead", Paths.USERS_MARK_PROMPT_READ, of("id"), of()),
    USERS_LOGOUT("users.logout", Paths.USERS_LOGOUT, of(), of()),
    USERS_USER_SITEMAP("users.userSitemap", Paths.USERS_USER_SITEMAP, of("user"), of()),
    USERS_EDIT_API_KEYS("users.editApiKeys", Paths.USERS_EDIT_API_KEYS, of("user"), of()),

    ORG_UPDATE_MEMBERS("org.updateMembers", Paths.ORG_UPDATE_MEMBERS, of("organization"), of()),
    ORG_UPDATE_AVATAR("org.updateAvatar", Paths.ORG_UPDATE_AVATAR, of("organization"), of()),
    ORG_SET_INVITE_STATUS("org.setInviteStatus", Paths.ORG_SET_INVITE_STATUS, of("id", "status"), of()),
    ORG_SHOW_CREATOR("org.showCreator", Paths.ORG_SHOW_CREATOR, of(), of()),
    ORG_CREATE("org.create", Paths.ORG_CREATE, of(), of()),
    ORG_REMOVE_MEMBER("org.removeMember", Paths.ORG_REMOVE_MEMBER, of("organization"), of()),

    REVIEWS_ADD_MESSAGE("reviews.addMessage", Paths.REVIEWS_ADD_MESSAGE, of("author", "slug", "version"), of()),
    REVIEWS_BACKLOG_TOGGLE("reviews.backlogToggle", Paths.REVIEWS_BACKLOG_TOGGLE, of("author", "slug", "version"), of()),
    REVIEWS_SHOW_REVIEWS("reviews.showReviews", Paths.REVIEWS_SHOW_REVIEWS, of("author", "slug", "version"), of()),
    REVIEWS_APPROVE_REVIEW("reviews.approveReview", Paths.REVIEWS_APPROVE_REVIEW, of("author", "slug", "version"), of()),
    REVIEWS_EDIT_REVIEW("reviews.editReview", Paths.REVIEWS_EDIT_REVIEW, of("author", "slug", "version", "review"), of()),
    REVIEWS_STOP_REVIEW("reviews.stopReview", Paths.REVIEWS_STOP_REVIEW, of("author", "slug", "version"), of()),
    REVIEWS_CREATE_REVIEW("reviews.createReview", Paths.REVIEWS_CREATE_REVIEW, of("author", "slug", "version"), of()),
    REVIEWS_TAKEOVER_REVIEW("reviews.takeoverReview", Paths.REVIEWS_TAKEOVER_REVIEW, of("author", "slug", "version"), of()),
    REVIEWS_REOPEN_REVIEW("reviews.reopenReview", Paths.REVIEWS_REOPEN_REVIEW, of("author", "slug", "version"), of()),

    CHANNELS_DELETE("channels.delete", Paths.CHANNELS_DELETE, of("author", "slug", "channel"), of()),
    CHANNELS_SAVE("channels.save", Paths.CHANNELS_SAVE, of("author", "slug", "channel"), of()),
    CHANNELS_SHOW_LIST("channels.showList", Paths.CHANNELS_SHOW_LIST, of("author", "slug"), of()),
    CHANNELS_CREATE("channels.create", Paths.CHANNELS_CREATE, of("author", "slug"), of()),


    // TO BE REMOVED
    APIV1_REVOKE_KEY("apiv1.revokeKey", Paths.APIV1_REVOKE_KEY, of("author", "slug"), of()),
    APIV1_CREATE_KEY("apiv1.createKey", Paths.APIV1_CREATE_KEY, of("author", "slug"), of());

    private static final Map<String, Routes> ROUTES = new HashMap<>();
    private static final Map<Routes, String> JS_ROUTES = new EnumMap<>(Routes.class);

    public static Map<Routes, String> getJsRoutes() {
        return JS_ROUTES;
    }

    static {
        for (Routes route : values()) {
            ROUTES.put(route.name, route);
            JS_ROUTES.put(route, route.url);
        }
    }

    public static String getRouteUrlOf(String name, String... args) {
        Routes route = ROUTES.get(name);
        if (route == null) {
            throw new RuntimeException("No Route " + name);
        }

        return route.getRouteUrl(args);
    }

    private final String name;
    private final String url;
    private final String[] pathParams;
    private final String[] queryParams;

    Routes(String name, String url, List<String> pathParams, List<String> queryParams) {
        this.name = name;
        this.url = url;
        this.pathParams = pathParams.toArray(new String[0]);
        this.queryParams = queryParams.toArray(new String[0]);
    }

    public String getUrl() {
        return url;
    }

    public String getRouteUrl(String... args) {
        if ((pathParams.length + queryParams.length) != args.length) {
            throw new RuntimeException("Args dont match for route " + name + " " + (pathParams.length + queryParams.length) + "!=" + args.length);
        }

        // path params first
        String url = this.url;
        for (int i = 0; i < pathParams.length; i++) {
            String value = args[i];
            String key = pathParams[i];
            url = url.replace("{" + key + "}", value);
        }

        // query params later
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        boolean first = true;
        for (int i = 0; i < queryParams.length; i++) {
            String value = args[i + pathParams.length];
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (first) {
                sb.append('?');
                first = false;
            } else {
                sb.append('&');
            }

            sb.append(queryParams[i]);
            sb.append('=');
            sb.append(value);
        }
        return sb.toString();
    }

    public ModelAndView getRedirect(String... args) {
        return new ModelAndView("redirect:" + getRouteUrl(args));
    }

    public static ModelAndView getRedirectToUrl(String url) {
        return new ModelAndView("redirect:" + url);
    }

    public static class Paths {
        public static final String SHOW_PROJECT_VISIBILITY = "/admin/approval/projects";
        public static final String ACTOR_COUNT = "/pantopticon/actor-count";
        public static final String ACTOR_TREE = "/pantopticon/actor-tree";
        public static final String UPDATE_USER = "/admin/user/{user}/update";
        public static final String SHOW_QUEUE = "/admin/approval/versions";
        public static final String SHOW_LOG = "/admin/log";
        public static final String SHOW_PLATFORM_VERSIONS = "/admin/versions";
        public static final String UPDATE_PLATFORM_VERSIONS = "/admin/versions/{platform}";
        public static final String REMOVE_TRAIL = "/{path}/";
        public static final String FAVICON_REDIRECT = "/favicon.ico";
        public static final String SHOW_FLAGS = "/admin/flags";
        public static final String SITEMAP_INDEX = "/sitemap.xml";
        public static final String GLOBAL_SITEMAP = "/global-sitemap.xml";
        public static final String SHOW_STATS = "/admin/stats";
        public static final String LINK_OUT = "/linkout";
        public static final String SHOW_HEALTH = "/admin/health";
        public static final String SHOW_HOME = "/";
        public static final String ROBOTS = "/robots.txt";
        public static final String SET_FLAG_RESOLVED = "/admin/flags/{id}/resolve/{resolved}";
        public static final String SWAGGER = "/api";
        public static final String SHOW_ACTIVITIES = "/admin/activities/{user}";
        public static final String USER_ADMIN = "/admin/user/{user}";

        public static final String PROJECTS_RENAME = "/{author}/{slug}/manage/rename";
        public static final String PROJECTS_SET_WATCHING = "/{author}/{slug}/watchers/{watching}";
        public static final String PROJECTS_SHOW_SETTINGS = "/{author}/{slug}/manage";
        public static final String PROJECTS_SET_INVITE_STATUS = "/invite/{id}/{status}";
        public static final String PROJECTS_TOGGLE_STARRED = "/{author}/{slug}/stars/toggle";
        public static final String PROJECTS_VALIDATE_NAME = "/validateProjectName";
        public static final String PROJECTS_SHOW_CREATOR = "/new";
        public static final String PROJECTS_SHOW_STARGAZERS = "/{author}/{slug}/stars";
        public static final String PROJECTS_SHOW_WATCHERS = "/{author}/{slug}/watchers";
        public static final String PROJECTS_UPLOAD_ICON = "/{author}/{slug}/icon";
        public static final String PROJECTS_SHOW_ICON = "/{author}/{slug}/icon";
        public static final String PROJECTS_SEND_FOR_APPROVAL = "/{author}/{slug}/manage/sendforapproval";
        public static final String PROJECTS_SET_INVITE_STATUS_ON_BEHALF = "/invite/{id}/{status}/{behalf}";
        public static final String PROJECTS_DELETE = "/{author}/{slug}/manage/hardDelete";
        public static final String PROJECTS_ADD_MESSAGE = "/{author}/{slug}/notes/addmessage";
        public static final String PROJECTS_SHOW_PENDING_ICON = "/{author}/{slug}/icon/pending";
        public static final String PROJECTS_POST_DISCUSSION_REPLY = "/{author}/{slug}/discuss/reply";
        public static final String PROJECTS_SHOW = "/{author}/{slug}";
        public static final String PROJECTS_SHOW_DISCUSSION = "/{author}/{slug}/discuss";
        public static final String PROJECTS_SOFT_DELETE = "/{author}/{slug}/manage/delete";
        public static final String PROJECTS_SHOW_FLAGS = "/{author}/{slug}/flags";
        public static final String PROJECTS_FLAG = "/{author}/{slug}/flag";
        public static final String PROJECTS_CREATE_PROJECT = "/new";
        public static final String PROJECTS_RESET_ICON = "/{author}/{slug}/icon/reset";
        public static final String PROJECTS_SAVE = "/{author}/{slug}/manage/save";
        public static final String PROJECTS_SHOW_NOTES = "/{author}/{slug}/notes";
        public static final String PROJECTS_SET_VISIBLE = "/{author}/{slug}/visible/{visibility}";
        public static final String PROJECTS_REMOVE_MEMBER = "/{author}/{slug}/manage/members/remove";

        public static final String VERSIONS_RESTORE = "/{author}/{slug}/versions/{version}/restore";
        public static final String VERSIONS_DOWNLOAD_RECOMMENDED_JAR = "/{author}/{slug}/versions/recommended/jar";
        public static final String VERSIONS_SAVE_NEW_VERSION = "/{author}/{slug}/versions/new/{version}";
        public static final String VERSIONS_PUBLISH = "/{author}/{slug}/versions/{version}";
        public static final String VERSIONS_PUBLISH_URL = "/{author}/{slug}/versions/publish";
        public static final String VERSIONS_SET_RECOMMENDED = "/{author}/{slug}/versions/{version}/recommended";
        public static final String VERSIONS_DOWNLOAD = "/{author}/{slug}/versions/{version}/download";
        public static final String VERSIONS_SHOW_LOG = "/{author}/{slug}/versionLog";
        public static final String VERSIONS_SHOW = "/{author}/{slug}/versions/{version}";
        public static final String VERSIONS_DOWNLOAD_JAR = "/{author}/{slug}/versions/{version}/jar";
        public static final String VERSIONS_APPROVE = "/{author}/{slug}/versions/{version}/approve";
        public static final String VERSIONS_APPROVE_PARTIAL = "/{author}/{slug}/versions/{version}/approvePartial";
        public static final String VERSIONS_SAVE_DESCRIPTION = "/{author}/{slug}/versions/{version}/save";
        public static final String VERSIONS_DOWNLOAD_RECOMMENDED = "/{author}/{slug}/versions/recommended/download";
        public static final String VERSIONS_SHOW_LIST = "/{author}/{slug}/versions";
        public static final String VERSIONS_DOWNLOAD_JAR_BY_ID = "/api/project/{author}/{slug}/versions/{name}/download";
        public static final String VERSIONS_DOWNLOAD_RECOMMENDED_JAR_BY_ID = "/api/project/{author}/{slug}/versions/recommended/download";
        public static final String VERSIONS_UPLOAD = "/{author}/{slug}/versions/new/upload";
        public static final String VERSIONS_CREATE_EXTERNAL_URL = "/{author}/{slug}/versions/new/create";
        public static final String VERSIONS_SOFT_DELETE = "/{author}/{slug}/versions/{version}/delete";
        public static final String VERSIONS_SHOW_DOWNLOAD_CONFIRM = "/{author}/{slug}/versions/{version}/confirm";
        public static final String VERSIONS_SHOW_CREATOR = "/{author}/{slug}/versions/new";
        public static final String VERSIONS_DELETE = "/{author}/{slug}/versions/{version}/hardDelete";
        public static final String VERSIONS_SHOW_CREATOR_WITH_META = "/{author}/{slug}/versions/new/{version}";
        public static final String VERSIONS_CONFIRM_DOWNLOAD = "/{author}/{slug}/versions/{version}/confirm";

        public static final String PAGES_SHOW_PREVIEW = "/pages/preview";
        public static final String PAGES_BB_CONVERT = "/pages/bb-convert";
        public static final String PAGES_SAVE = "/{author}/{slug}/pages/{page}/edit";
        public static final String PAGES_SHOW_EDITOR = "/{author}/{slug}/pages/{page}/edit";
        public static final String PAGES_SHOW = "/{author}/{slug}/pages/{page}";
        public static final String PAGES_DELETE = "/{author}/{slug}/pages/{page}/delete";

        public static final String USERS_SHOW_AUTHORS = "/authors";
        public static final String USERS_SAVE_TAGLINE = "/{user}/settings/tagline";
        public static final String USERS_SIGN_UP = "/signup";
        public static final String USERS_SHOW_NOTIFICATIONS = "/notifications";
        public static final String USERS_SHOW_PROJECTS = "/{user}";
        public static final String USERS_VERIFY = "/verify";
        public static final String USERS_MARK_NOTIFICATION_READ = "/notifications/read/{id}";
        public static final String USERS_LOGIN = "/login";
        public static final String USERS_SHOW_STAFF = "/staff";
        public static final String USERS_SET_LOCKED = "/{user}/settings/lock/{locked}";
        public static final String USERS_MARK_PROMPT_READ = "/prompts/read/{id}";
        public static final String USERS_LOGOUT = "/logout";
        public static final String USERS_USER_SITEMAP = "/{user}/sitemap.xml";
        public static final String USERS_EDIT_API_KEYS = "/{user}/settings/apiKeys";

        public static final String ORG_UPDATE_MEMBERS = "/organizations/{organization}/settings/members";
        public static final String ORG_UPDATE_AVATAR = "/organizations/{organization}/settings/avatar";
        public static final String ORG_SET_INVITE_STATUS = "/organizations/invite/{id}/{status}";
        public static final String ORG_SHOW_CREATOR = "/organizations/new";
        public static final String ORG_CREATE = "/organizations/new";
        public static final String ORG_REMOVE_MEMBER = "/organizations/{organization}/settings/members/remove";

        public static final String REVIEWS_ADD_MESSAGE = "/{author}/{slug}/versions/{version}/reviews/addmessage";
        public static final String REVIEWS_BACKLOG_TOGGLE = "/{author}/{slug}/versions/{version}/reviews/reviewtoggle";
        public static final String REVIEWS_SHOW_REVIEWS = "/{author}/{slug}/versions/{version}/reviews";
        public static final String REVIEWS_APPROVE_REVIEW = "/{author}/{slug}/versions/{version}/reviews/approve";
        public static final String REVIEWS_EDIT_REVIEW = "/{author}/{slug}/versions/{version}/reviews/edit/{review}";
        public static final String REVIEWS_STOP_REVIEW = "/{author}/{slug}/versions/{version}/reviews/stop";
        public static final String REVIEWS_CREATE_REVIEW = "/{author}/{slug}/versions/{version}/reviews/init";
        public static final String REVIEWS_TAKEOVER_REVIEW = "/{author}/{slug}/versions/{version}/reviews/takeover";
        public static final String REVIEWS_REOPEN_REVIEW = "/{author}/{slug}/versions/{version}/reviews/reopen";

        public static final String CHANNELS_DELETE = "/{author}/{slug}/channels/{channel}/delete";
        public static final String CHANNELS_SAVE = "/{author}/{slug}/channels/{channel}";
        public static final String CHANNELS_SHOW_LIST = "/{author}/{slug}/channels";
        public static final String CHANNELS_CREATE = "/{author}/{slug}/channels";

        // TO BE REMOVED
        public static final String APIV1_REVOKE_KEY = "/api/v1/projects/{author}/{slug}/keys/revoke";
        public static final String APIV1_CREATE_KEY = "/api/v1/projects/{author}/{slug}/keys/new";

        private Paths() { }
    }
}