package me.minidigger.hangar.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.List.of;

@Component
public class RouteHelper {

    private final Map<String, Route> routes = new HashMap<>();

    public RouteHelper() {
        register("showProjectVisibility", "/admin/approval/projects", of(), of());
        register("actorCount", "/pantopticon/actor-count", of(), of("timeoutMs"));
        register("actorTree", "/pantopticon/actor-tree", of(), of("timeoutMs"));
        register("updateUser", "/admin/user/{user}/update", of("user"), of());
        register("showQueue", "/admin/approval/versions", of(), of());
        register("showLog", "/admin/log", of(), of("page", "userFilter", "projectFilter", "versionFilter", "pageFilter", "actionFilter", "subjectFilter"));
        register("removeTrail", "/{path}/", of("path"), of());
        register("faviconRedirect", "/favicon.ico", of(), of());
        register("showFlags", "/admin/flags", of(), of());
        register("sitemapIndex", "/sitemap.xml", of(), of());
        register("globalSitemap", "/global-sitemap.xml", of(), of());
        register("showStats", "/admin/stats", of(), of("from", "to"));
        register("linkOut", "/linkout", of(), of("remoteUrl"));
        register("showHealth", "/admin/health", of(), of());
        register("showHome", "/", of(), of());
        register("robots", "/robots.txt", of(), of());
        register("setFlagResolved", "/admin/flags/{id}/resolve/{resolved}", of("id", "resolved"), of());
        register("swagger", "/api", of(), of());
        register("showActivities", "/admin/activities/{user}", of("user"), of());
        register("userAdmin", "/admin/user/{user}", of("user"), of());
        register("javaScriptRoutes", "/javascriptRoutes", of(), of());

        register("projects.rename", "/{author}/{slug}/manage/rename", of("author", "slug"), of());
        register("projects.setWatching", "/{author}/{slug}/watchers/{watching}", of("author", "slug", "watching"), of());
        register("projects.showSettings", "/{author}/{slug}/manage", of("author", "slug"), of());
        register("projects.setInviteStatus", "/invite/{id}/{status}", of("id", "status"), of());
        register("projects.toggleStarred", "/{author}/{slug}/stars/toggle", of("author", "slug"), of());
        register("projects.showCreator", "/new", of(), of());
        register("projects.showStargazers", "/{author}/{slug}/stars", of("author", "slug"), of("page"));
        register("projects.showWatchers", "/{author}/{slug}/watchers", of("author", "slug"), of("page"));
        register("projects.uploadIcon", "/{author}/{slug}/icon", of("author", "slug"), of());
        register("projects.showIcon", "/{author}/{slug}/icon", of("author", "slug"), of());
        register("projects.sendForApproval", "/{author}/{slug}/manage/sendforapproval", of("author", "slug"), of());
        register("projects.setInviteStatusOnBehalf", "/invite/{id}/{status}/{behalf}", of("id", "status", "behalf"), of());
        register("projects.delete", "/{author}/{slug}/manage/hardDelete", of("author", "slug"), of());
        register("projects.addMessage", "/{author}/{slug}/notes/addmessage", of("author", "slug"), of());
        register("projects.showPendingIcon", "/{author}/{slug}/icon/pending", of("author", "slug"), of());
        register("projects.postDiscussionReply", "/{author}/{slug}/discuss/reply", of("author", "slug"), of());
        register("projects.show", "/{author}/{slug}", of("author", "slug"), of());
        register("projects.showDiscussion", "/{author}/{slug}/discuss", of("author", "slug"), of());
        register("projects.softDelete", "/{author}/{slug}/manage/delete", of("author", "slug"), of());
        register("projects.showFlags", "/{author}/{slug}/flags", of("author", "slug"), of());
        register("projects.flag", "/{author}/{slug}/flag", of("author", "slug"), of());
        register("projects.createProject", "/new", of(), of());
        register("projects.resetIcon", "/{author}/{slug}/icon/reset", of("author", "slug"), of());
        register("projects.save", "/{author}/{slug}/manage/save", of("author", "slug"), of());
        register("projects.showNotes", "/{author}/{slug}/notes", of("author", "slug"), of());
        register("projects.setVisible", "/{author}/{slug}/visible/{visibility}", of("author", "slug", "visibility"), of());
        register("projects.removeMember", "/{author}/{slug}/manage/members/remove", of("author", "slug"), of());
        register("projects.showNotes", "/{author}/{slug}/notes", of("author", "slug"), of());

        register("versions.restore", "/{author}/{slug}/versions/{version}/restore", of("author", "slug", "version"), of());
        register("versions.downloadRecommendedJar", "/{author}/{slug}/versions/recommended/jar", of("author", "slug"), of("token"));
        register("versions.publish", "/{author}/{slug}/versions/{version}", of("author", "slug", "version"), of());
        register("versions.setRecommended", "/{author}/{slug}/versions/{version}/recommended", of("author", "slug", "version"), of());
        register("versions.download", "/{author}/{slug}/versions/{version}/download", of("author", "slug", "version"), of("token", "confirm"));
        register("versions.showLog", "/{author}/{slug}/versionLog", of("author", "slug"), of("versionString"));
        register("versions.show", "/{author}/{slug}/versions/{version}", of("author", "slug", "version"), of());
        register("versions.downloadJar", "/{author}/{slug}/versions/{version}/jar", of("author", "slug", "version"), of("token"));
        register("versions.approve", "/{author}/{slug}/versions/{version}/approve", of("author", "slug", "version"), of());
        register("versions.approvePartial", "/{author}/{slug}/versions/{version}/approvePartial", of("author", "slug", "version"), of());
        register("versions.saveDescription", "/{author}/{slug}/versions/{version}/save", of("author", "slug", "version"), of());
        register("versions.downloadRecommended", "/{author}/{slug}/versions/recommended/download", of("author", "slug"), of("token"));
        register("versions.showList", "/{author}/{slug}/versions", of("author", "slug"), of());
        register("versions.downloadJarById", "/api/project/{pluginId}/versions/{name}/download", of("pluginId", "name"), of("token"));
        register("versions.downloadRecommendedJarById", "/api/project/{pluginId}/versions/recommended/download", of("pluginId"), of("token"));
        register("versions.upload", "/{author}/{slug}/versions/new/upload", of("author", "slug"), of());
        register("versions.softDelete", "/{author}/{slug}/versions/{version}/delete", of("author", "slug", "version"), of());
        register("versions.showDownloadConfirm", "/{author}/{slug}/versions/{version}/confirm", of("author", "slug", "version"), of("downloadType", "api", "dummy"));
        register("versions.showCreator", "/{author}/{slug}/versions/new", of("author", "slug"), of());
        register("versions.delete", "/{author}/{slug}/versions/{version}/hardDelete", of("author", "slug", "version"), of());
        register("versions.showCreatorWithMeta", "/{author}/{slug}/versions/new/{version}", of("author", "slug", "version"), of());
        register("versions.confirmDownload", "/{author}/{slug}/versions/{version}/confirm", of("author", "slug", "version"), of("downloadType", "api", "dummy"));

        register("pages.showPreview", "/pages/preview", of(), of());
        register("pages.save", "/{author}/{slug}/pages/{page}/edit", of("author", "slug", "page"), of());
        register("pages.showEditor", "/{author}/{slug}/pages/{page}/edit", of("author", "slug", "page"), of());
        register("pages.show", "/{author}/{slug}/pages/{page}", of("author", "slug", "page"), of());
        register("pages.delete", "/{author}/{slug}/pages/{page}/delete", of("author", "slug", "page"), of());

        register("users.showAuthors", "/authors", of(), of("sort", "page"));
        register("users.saveTagline", "/{user}/settings/tagline", of("user"), of());
        register("users.signUp", "/signup", of(), of());
        register("users.showNotifications", "/notifications", of(), of("notificationFilter", "inviteFilter"));
        register("users.showProjects", "/{user}", of("user"), of());
        register("users.verify", "/verify", of(), of("returnPath"));
        register("users.markNotificationRead", "/notifications/read/{id}", of("id"), of());
        register("users.login", "/login", of(), of("sso", "sig", "returnUrl"));
        register("users.showStaff", "/staff", of(), of("sort", "page"));
        register("users.setLocked", "/{user}/settings/lock/{locked}", of("user", "locked"), of("sso", "sig"));
        register("users.markPromptRead", "/prompts/read/{id}", of("id"), of());
        register("users.logout", "/logout", of(), of());
        register("users.userSitemap", "/{user}/sitemap.xml", of("user"), of());
        register("users.editApiKeys", "/{user}/settings/apiKeys", of("user"), of());

        register("org.updateMembers", "/organisations/{organisations}/settings/members", of("organisations"), of());
        register("org.updateAvatar", "/organisations/{organisations}/settings/avatar", of("organisations"), of());
        register("org.setInviteStatus", "/organisations/invite/{id}/{status}", of("id", "status"), of());
        register("org.showCreator", "/organisations/new", of(), of());
        register("org.create", "/organisations/new", of(), of());
        register("org.removeMember", "/organisations/{organisations}/settings/members/remove", of("organisations"), of());

        register("reviews.addMessage", "/{author}/{slug}/versions/{version}/reviews/addmessage", of("author", "slug", "version"), of());
        register("reviews.backlogToggle", "/{author}/{slug}/versions/{version}/reviews/reviewtoggle", of("author", "slug", "version"), of());
        register("reviews.showReviews", "/{author}/{slug}/versions/{version}/reviews", of("author", "slug", "version"), of());
        register("reviews.approveReview", "/{author}/{slug}/versions/{version}/reviews/approve", of("author", "slug", "version"), of());
        register("reviews.editReview", "/{author}/{slug}/versions/{version}/reviews/edit/{review}", of("author", "slug", "version", "review"), of());
        register("reviews.stopReview", "/{author}/{slug}/versions/{version}/reviews/stop", of("author", "slug", "version"), of());
        register("reviews.createReview", "/{author}/{slug}/versions/{version}/reviews/init", of("author", "slug", "version"), of());
        register("reviews.takeoverReview", "/{author}/{slug}/versions/{version}/reviews/takeover", of("author", "slug", "version"), of());
        register("reviews.reopenReview", "/{author}/{slug}/versions/{version}/reviews/reopen", of("author", "slug", "version"), of());

        register("channels.delete", "/{author}/{slug}/channels/{channel}/delete", of("author", "slug", "channel"), of());
        register("channels.save", "/{author}/{slug}/channels/{channel}", of("author", "slug", "channel"), of());
        register("channels.showList", "/{author}/{slug}/channels", of("author", "slug"), of());
        register("channels.create", "/{author}/{slug}/channels", of("author", "slug"), of());

        register("apiv1.showVersion", "/api/v1/projects/{pluginId}/versions/{name}", of("pluginId", "name"), of());
        register("apiv1.listProjects", "/api/v1/projects", of(), of("categories", "sort", "q", "limit", "offset"));
        register("apiv1.listUsers", "/api/v1/users", of(), of("limit", "offset"));
        register("apiv1.listVersions", "/api/v1/projects/{pluginId}/versions", of("pluginId"), of("channels", "limit", "offset"));
        register("apiv1.revokeKey", "/api/v1/projects/{pluginId}/keys/revoke", of("pluginId"), of());
        register("apiv1.createKey", "/api/v1/projects/{pluginId}/keys/new", of("pluginId"), of());
        register("apiv1.showProject", "/api/v1/projects/{pluginId}", of("pluginId"), of());
        register("apiv1.syncSso", "/api/sync_sso", of(), of());
        register("apiv1.tagColor", "/api/v1/tags/{tagId}", of("tagId"), of());
        register("apiv1.showStatusZ", "/statusz", of(), of());
        register("apiv1.showUser", "/api/v1/users/{user}", of("user"), of());
        register("apiv1.listPages", "/api/v1/projects/{pluginId}/pages", of("pluginId"), of("parentId"));
        register("apiv1.deployVersion", "/api/v1/projects/{pluginId}/versions/{name}", of("pluginId", "name"), of());
        register("apiv1.listTags", "/api/v1/projects/{plugin}/tags/{versionName}", of("plugin", "versionName"), of());

    }

    // TODO support route params
    public String getRouteUrl(String name, String... args) {
        Route route = routes.get(name);
        if (route == null) {
            throw new RuntimeException("No Route " + name);
        } else if ((route.pathParms.size() + route.queryParams.size()) != args.length) {
            throw new RuntimeException("Args dont match for route " + name + " " + (route.pathParms.size() + route.queryParams.size()) + "!=" + args.length);
        }

        // path params first
        String url = route.url;
        for (int i = 0; i < route.pathParms.size(); i++) {
            String value = args[i];
            String key = route.pathParms.get(i);
            url = url.replace("{" + key + "}", value);
        }

        // query params later
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        boolean first = true;
        for (int i = 0; i < route.queryParams.size(); i++) {
            String value = args[i + route.pathParms.size()];
            if (value == null || value.length() == 0) {
                continue;
            }

            if (first) {
                sb.append("?");
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(route.queryParams.get(i));
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }

    private void register(String name, String url, List<String> pathParms, List<String> queryParams) {
        Route route = new Route(name, url, pathParms, queryParams);
        routes.put(route.name, route);
    }

    class Route {
        String name;
        String url;
        List<String> pathParms;
        List<String> queryParams;

        public Route(String name, String url, List<String> pathParms, List<String> queryParams) {
            this.name = name;
            this.url = url;
            this.pathParms = pathParms;
            this.queryParams = queryParams;
        }
    }
}
