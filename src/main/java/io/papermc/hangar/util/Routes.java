package io.papermc.hangar.util;

import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.List.of;

public enum Routes {

    SHOW_PROJECT_VISIBILITY("showProjectVisibility", "/admin/approval/projects", of(), of()),
    ACTOR_COUNT("actorCount", "/pantopticon/actor-count", of(), of("timeoutMs")),
    ACTOR_TREE("actorTree", "/pantopticon/actor-tree", of(), of("timeoutMs")),
    UPDATE_USER("updateUser", "/admin/user/{user}/update", of("user"), of()),
    SHOW_QUEUE("showQueue", "/admin/approval/versions", of(), of()),
    SHOW_LOG("showLog", "/admin/log", of(), of("page", "userFilter", "projectFilter", "versionFilter", "pageFilter", "actionFilter", "subjectFilter")),
    SHOW_PLATFORM_VERSIONS("showPlatformVersions", "/admin/versions", of(), of()),
    UPDATE_PLATFORM_VERSIONS("updatePlatformVersions", "/admin/versions/{platform}", of("platform"), of()),
    REMOVE_TRAIL("removeTrail", "/{path}/", of("path"), of()),
    FAVICON_REDIRECT("faviconRedirect", "/favicon.ico", of(), of()),
    SHOW_FLAGS("showFlags", "/admin/flags", of(), of()),
    SITEMAP_INDEX("sitemapIndex", "/sitemap.xml", of(), of()),
    GLOBAL_SITEMAP("globalSitemap", "/global-sitemap.xml", of(), of()),
    SHOW_STATS("showStats", "/admin/stats", of(), of("from", "to")),
    LINK_OUT("linkOut", "/linkout", of(), of("remoteUrl")),
    SHOW_HEALTH("showHealth", "/admin/health", of(), of()),
    SHOW_HOME("showHome", "/", of(), of()),
    ROBOTS("robots", "/robots.txt", of(), of()),
    SET_FLAG_RESOLVED("setFlagResolved", "/admin/flags/{id}/resolve/{resolved}", of("id", "resolved"), of()),
    SWAGGER("swagger", "/api", of(), of()),
    SHOW_ACTIVITIES("showActivities", "/admin/activities/{user}", of("user"), of()),
    USER_ADMIN("userAdmin", "/admin/user/{user}", of("user"), of()),
    JAVA_SCRIPT_ROUTES("javaScriptRoutes", "/javascriptRoutes", of(), of()),

    PROJECTS_RENAME("projects.rename", "/{author}/{slug}/manage/rename", of("author", "slug"), of()),
    PROJECTS_SET_WATCHING("projects.setWatching", "/{author}/{slug}/watchers/{watching}", of("author", "slug", "watching"), of()),
    PROJECTS_SHOW_SETTINGS("projects.showSettings", "/{author}/{slug}/manage", of("author", "slug"), of()),
    PROJECTS_SET_INVITE_STATUS("projects.setInviteStatus", "/invite/{id}/{status}", of("id", "status"), of()),
    PROJECTS_TOGGLE_STARRED("projects.toggleStarred", "/{author}/{slug}/stars/toggle", of("author", "slug"), of()),
    PROJECTS_SHOW_CREATOR("projects.showCreator", "/new", of(), of()),
    PROJECTS_SHOW_STARGAZERS("projects.showStargazers", "/{author}/{slug}/stars", of("author", "slug"), of("page")),
    PROJECTS_SHOW_WATCHERS("projects.showWatchers", "/{author}/{slug}/watchers", of("author", "slug"), of("page")),
    PROJECTS_UPLOAD_ICON("projects.uploadIcon", "/{author}/{slug}/icon", of("author", "slug"), of()),
    PROJECTS_SHOW_ICON("projects.showIcon", "/{author}/{slug}/icon", of("author", "slug"), of()),
    PROJECTS_SEND_FOR_APPROVAL("projects.sendForApproval", "/{author}/{slug}/manage/sendforapproval", of("author", "slug"), of()),
    PROJECTS_SET_INVITE_STATUS_ON_BEHALF("projects.setInviteStatusOnBehalf", "/invite/{id}/{status}/{behalf}", of("id", "status", "behalf"), of()),
    PROJECTS_DELETE("projects.delete", "/{author}/{slug}/manage/hardDelete", of("author", "slug"), of()),
    PROJECTS_ADD_MESSAGE("projects.addMessage", "/{author}/{slug}/notes/addmessage", of("author", "slug"), of()),
    PROJECTS_SHOW_PENDING_ICON("projects.showPendingIcon", "/{author}/{slug}/icon/pending", of("author", "slug"), of()),
    PROJECTS_POST_DISCUSSION_REPLY("projects.postDiscussionReply", "/{author}/{slug}/discuss/reply", of("author", "slug"), of()),
    PROJECTS_SHOW("projects.show", "/{author}/{slug}", of("author", "slug"), of()),
    PROJECTS_SHOW_DISCUSSION("projects.showDiscussion", "/{author}/{slug}/discuss", of("author", "slug"), of()),
    PROJECTS_SOFT_DELETE("projects.softDelete", "/{author}/{slug}/manage/delete", of("author", "slug"), of()),
    PROJECTS_SHOW_FLAGS("projects.showFlags", "/{author}/{slug}/flags", of("author", "slug"), of()),
    PROJECTS_FLAG("projects.flag", "/{author}/{slug}/flag", of("author", "slug"), of()),
    PROJECTS_CREATE_PROJECT("projects.createProject", "/new", of(), of()),
    PROJECTS_RESET_ICON("projects.resetIcon", "/{author}/{slug}/icon/reset", of("author", "slug"), of()),
    PROJECTS_SAVE("projects.save", "/{author}/{slug}/manage/save", of("author", "slug"), of()),
    PROJECTS_SHOW_NOTES("projects.showNotes", "/{author}/{slug}/notes", of("author", "slug"), of()),
    PROJECTS_SET_VISIBLE("projects.setVisible", "/{author}/{slug}/visible/{visibility}", of("author", "slug", "visibility"), of()),
    PROJECTS_REMOVE_MEMBER("projects.removeMember", "/{author}/{slug}/manage/members/remove", of("author", "slug"), of()),

    VERSIONS_RESTORE("versions.restore", "/{author}/{slug}/versions/{version}/restore", of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_RECOMMENDED_JAR("versions.downloadRecommendedJar", "/{author}/{slug}/versions/recommended/jar", of("author", "slug"), of("token")),
    VERSIONS_PUBLISH("versions.publish", "/{author}/{slug}/versions/{version}", of("author", "slug", "version"), of()),
    VERSIONS_PUBLISH_URL("versions.publishUrl", "/{author}/{slug}/versions/publish", of("author", "slug"), of()),
    VERSIONS_SET_RECOMMENDED("versions.setRecommended", "/{author}/{slug}/versions/{version}/recommended", of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD("versions.download", "/{author}/{slug}/versions/{version}/download", of("author", "slug", "version"), of("token", "confirm")),
    VERSIONS_SHOW_LOG("versions.showLog", "/{author}/{slug}/versionLog", of("author", "slug"), of("versionString")),
    VERSIONS_SHOW("versions.show", "/{author}/{slug}/versions/{version}", of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_JAR("versions.downloadJar", "/{author}/{slug}/versions/{version}/jar", of("author", "slug", "version"), of("token")),
    VERSIONS_APPROVE("versions.approve", "/{author}/{slug}/versions/{version}/approve", of("author", "slug", "version"), of()),
    VERSIONS_APPROVE_PARTIAL("versions.approvePartial", "/{author}/{slug}/versions/{version}/approvePartial", of("author", "slug", "version"), of()),
    VERSIONS_SAVE_DESCRIPTION("versions.saveDescription", "/{author}/{slug}/versions/{version}/save", of("author", "slug", "version"), of()),
    VERSIONS_DOWNLOAD_RECOMMENDED("versions.downloadRecommended", "/{author}/{slug}/versions/recommended/download", of("author", "slug"), of("token")),
    VERSIONS_SHOW_LIST("versions.showList", "/{author}/{slug}/versions", of("author", "slug"), of()),
    VERSIONS_DOWNLOAD_JAR_BY_ID("versions.downloadJarById", "/api/project/{pluginId}/versions/{name}/download", of("pluginId", "name"), of("token")),
    VERSIONS_DOWNLOAD_RECOMMENDED_JAR_BY_ID("versions.downloadRecommendedJarById", "/api/project/{pluginId}/versions/recommended/download", of("pluginId"), of("token")),
    VERSIONS_UPLOAD("versions.upload", "/{author}/{slug}/versions/new/upload", of("author", "slug"), of()),
    VERSIONS_CREATE_EXTERNAL_URL("versions.createExternalUrl", "/{author}/{slug}/versions/new/create", of("author", "slug"), of()),
    VERSIONS_SOFT_DELETE("versions.softDelete", "/{author}/{slug}/versions/{version}/delete", of("author", "slug", "version"), of()),
    VERSIONS_SHOW_DOWNLOAD_CONFIRM("versions.showDownloadConfirm", "/{author}/{slug}/versions/{version}/confirm", of("author", "slug", "version"), of("downloadType", "api", "dummy")),
    VERSIONS_SHOW_CREATOR("versions.showCreator", "/{author}/{slug}/versions/new", of("author", "slug"), of()),
    VERSIONS_DELETE("versions.delete", "/{author}/{slug}/versions/{version}/hardDelete", of("author", "slug", "version"), of()),
    VERSIONS_SHOW_CREATOR_WITH_META("versions.showCreatorWithMeta", "/{author}/{slug}/versions/new/{version}", of("author", "slug", "version"), of()),
    VERSIONS_CONFIRM_DOWNLOAD("versions.confirmDownload", "/{author}/{slug}/versions/{version}/confirm", of("author", "slug", "version"), of("downloadType", "token", "dummy")),

    PAGES_SHOW_PREVIEW("pages.showPreview", "/pages/preview", of(), of()),
    PAGES_SAVE("pages.save", "/{author}/{slug}/pages/{page}/edit", of("author", "slug", "page"), of()),
    PAGES_SHOW_EDITOR("pages.showEditor", "/{author}/{slug}/pages/{page}/edit", of("author", "slug", "page"), of()),
    PAGES_SHOW("pages.show", "/{author}/{slug}/pages/{page}", of("author", "slug", "page"), of()),
    PAGES_DELETE("pages.delete", "/{author}/{slug}/pages/{page}/delete", of("author", "slug", "page"), of()),

    USERS_SHOW_AUTHORS("users.showAuthors", "/authors", of(), of("sort", "page")),
    USERS_SAVE_TAGLINE("users.saveTagline", "/{user}/settings/tagline", of("user"), of()),
    USERS_SIGN_UP("users.signUp", "/signup", of(), of()),
    USERS_SHOW_NOTIFICATIONS("users.showNotifications", "/notifications", of(), of("notificationFilter", "inviteFilter")),
    USERS_SHOW_PROJECTS("users.showProjects", "/{user}", of("user"), of()),
    USERS_VERIFY("users.verify", "/verify", of(), of("returnPath")),
    USERS_MARK_NOTIFICATION_READ("users.markNotificationRead", "/notifications/read/{id}", of("id"), of()),
    USERS_LOGIN("users.login", "/login", of(), of("sso", "sig", "returnUrl")),
    USERS_SHOW_STAFF("users.showStaff", "/staff", of(), of("sort", "page")),
    USERS_SET_LOCKED("users.setLocked", "/{user}/settings/lock/{locked}", of("user", "locked"), of("sso", "sig")),
    USERS_MARK_PROMPT_READ("users.markPromptRead", "/prompts/read/{id}", of("id"), of()),
    USERS_LOGOUT("users.logout", "/logout", of(), of()),
    USERS_USER_SITEMAP("users.userSitemap", "/{user}/sitemap.xml", of("user"), of()),
    USERS_EDIT_API_KEYS("users.editApiKeys", "/{user}/settings/apiKeys", of("user"), of()),

    ORG_UPDATE_MEMBERS("org.updateMembers", "/organizations/{organization}/settings/members", of("organization"), of()),
    ORG_UPDATE_AVATAR("org.updateAvatar", "/organizations/{organization}/settings/avatar", of("organization"), of()),
    ORG_SET_INVITE_STATUS("org.setInviteStatus", "/organizations/invite/{id}/{status}", of("id", "status"), of()),
    ORG_SHOW_CREATOR("org.showCreator", "/organizations/new", of(), of()),
    ORG_CREATE("org.create", "/organizations/new", of(), of()),
    ORG_REMOVE_MEMBER("org.removeMember", "/organizations/{organization}/settings/members/remove", of("organization"), of()),

    REVIEWS_ADD_MESSAGE("reviews.addMessage", "/{author}/{slug}/versions/{version}/reviews/addmessage", of("author", "slug", "version"), of()),
    REVIEWS_BACKLOG_TOGGLE("reviews.backlogToggle", "/{author}/{slug}/versions/{version}/reviews/reviewtoggle", of("author", "slug", "version"), of()),
    REVIEWS_SHOW_REVIEWS("reviews.showReviews", "/{author}/{slug}/versions/{version}/reviews", of("author", "slug", "version"), of()),
    REVIEWS_APPROVE_REVIEW("reviews.approveReview", "/{author}/{slug}/versions/{version}/reviews/approve", of("author", "slug", "version"), of()),
    REVIEWS_EDIT_REVIEW("reviews.editReview", "/{author}/{slug}/versions/{version}/reviews/edit/{review}", of("author", "slug", "version", "review"), of()),
    REVIEWS_STOP_REVIEW("reviews.stopReview", "/{author}/{slug}/versions/{version}/reviews/stop", of("author", "slug", "version"), of()),
    REVIEWS_CREATE_REVIEW("reviews.createReview", "/{author}/{slug}/versions/{version}/reviews/init", of("author", "slug", "version"), of()),
    REVIEWS_TAKEOVER_REVIEW("reviews.takeoverReview", "/{author}/{slug}/versions/{version}/reviews/takeover", of("author", "slug", "version"), of()),
    REVIEWS_REOPEN_REVIEW("reviews.reopenReview", "/{author}/{slug}/versions/{version}/reviews/reopen", of("author", "slug", "version"), of()),

    CHANNELS_DELETE("channels.delete", "/{author}/{slug}/channels/{channel}/delete", of("author", "slug", "channel"), of()),
    CHANNELS_SAVE("channels.save", "/{author}/{slug}/channels/{channel}", of("author", "slug", "channel"), of()),
    CHANNELS_SHOW_LIST("channels.showList", "/{author}/{slug}/channels", of("author", "slug"), of()),
    CHANNELS_CREATE("channels.create", "/{author}/{slug}/channels", of("author", "slug"), of()),

    APIV1_SHOW_VERSION("apiv1.showVersion", "/api/v1/projects/{pluginId}/versions/{name}", of("pluginId", "name"), of()),
    APIV1_LIST_PROJECTS("apiv1.listProjects", "/api/v1/projects", of(), of("categories", "sort", "q", "limit", "offset")),
    APIV1_LIST_USERS("apiv1.listUsers", "/api/v1/users", of(), of("limit", "offset")),
    APIV1_LIST_VERSIONS("apiv1.listVersions", "/api/v1/projects/{pluginId}/versions", of("pluginId"), of("channels", "limit", "offset")),
    APIV1_REVOKE_KEY("apiv1.revokeKey", "/api/v1/projects/{pluginId}/keys/revoke", of("pluginId"), of()),
    APIV1_CREATE_KEY("apiv1.createKey", "/api/v1/projects/{pluginId}/keys/new", of("pluginId"), of()),
    APIV1_SHOW_PROJECT("apiv1.showProject", "/api/v1/projects/{pluginId}", of("pluginId"), of()),
    APIV1_SYNC_SSO("apiv1.syncSso", "/api/sync_sso", of(), of()),
    APIV1_TAG_COLOR("apiv1.tagColor", "/api/v1/tags/{tagId}", of("tagId"), of()),
    APIV1_SHOW_STATUS_Z("apiv1.showStatusZ", "/statusz", of(), of()),
    APIV1_SHOW_USER("apiv1.showUser", "/api/v1/users/{user}", of("user"), of()),
    APIV1_LIST_PAGES("apiv1.listPages", "/api/v1/projects/{pluginId}/pages", of("pluginId"), of("parentId")),
    APIV1_DEPLOY_VERSION("apiv1.deployVersion", "/api/v1/projects/{pluginId}/versions/{name}", of("pluginId", "name"), of()),
    APIV1_LIST_TAGS("apiv1.listTags", "/api/v1/projects/{plugin}/tags/{versionName}", of("plugin", "versionName"), of()),
    APIV1_LIST_PLATFORMS("apiv1.listPlatforms", "/api/v1/platforms", of(), of());

    private static final Map<String, Routes> ROUTES = new HashMap<>();

    static {
        for (Routes route : values()) {
            ROUTES.put(route.name, route);
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
}