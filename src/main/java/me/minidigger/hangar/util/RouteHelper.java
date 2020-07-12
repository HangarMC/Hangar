package me.minidigger.hangar.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RouteHelper {

    private Map<String, Route> routes = new HashMap<>();

    public RouteHelper() {
        register("showProjectVisibility", "/idk");
        register("actorCount", "/pantopticon/actor-count", "timeoutMs");
        register("actorTree", "/pantopticon/actor-tree", "timeoutMs");
        register("updateUser", "/admin/user/{user}/update");
        register("showQueue", "/admin/approval/versions");
        register("showLog", "/admin/log", "page", "userFilter", "projectFilter", "versionFilter", "pageFilter", "actionFilter", "subjectFilter");
        register("removeTrail", "{path}/");
        register("faviconRedirect", "favicon.ico");
        register("showFlags", "/admin/flags");
        register("sitemapIndex", "/sitemap.xml");
        register("globalSitemap", "/global-sitemap.xml");
        register("showStats", "/admin/stats", "from", "to");
        register("linkOut", "/linkout", "remoteUrl");
        register("showHealth", "/admin/health");
        register("showHome", "/");
        register("robots", "/robots.txt");
        register("setFlagResolved", "/admin/flags/{id}/resolve/{resolved}");
        register("swagger", "/api");
        register("showActivities", "/admin/activities/{user}");
        register("userAdmin", "/admin/user/{user}");
        register("javaScriptRoutes", "/javascriptRoutes");

        register("projects.showCreator", "/new");

        register("users.showAuthors", "/authors", "sort", "page");
        register("users.saveTagline", "{user}/settings/tagline");
        register("users.signUp", "/signup");
        register("users.showNotifications", "/notifications", "notificationFilter", "inviteFilter");
        register("users.showProjects", "/{user}");
        register("users.verify", "/verify", "returnPath");
        register("users.markNotificationRead", "/notifications/read/{id}");
        register("users.login", "/login", "sso", "sig", "returnUrl");
        register("users.showStaff", "/staff", "sort", "page");
        register("users.setLocked", "/{user}/settings/lock/{locked}", "sso", "sig");
        register("users.markPromptRead", "/prompts/read/{id}");
        register("users.logout", "/logout");
        register("users.userSitemap", "/{user}/sitemap.xml");
        register("users.editApiKeys", "/{user}/settings/apiKeys");

        register("org.updateMembers", "/organisations/{organisations}/settings/members");
        register("org.updateAvatar", "/organisations/{organisations}/settings/avatar");
        register("org.setInviteStatus", "/organisations/invite/{id}/{status}");
        register("org.showCreator", "/organisations/new");
        register("org.create", "/organisations/new");
        register("org.removeMember", "/organisations/{organisations}/settings/members/remove");

        register("reviews.addMessage", "{author}/{slug}/versions/{version}/reviews/addmessage");
        register("reviews.backlogToggle", "{author}/{slug}/versions/{version}/reviews/reviewtoggle");
        register("reviews.showReviews", "{author}/{slug}/versions/{version}/reviews");
        register("reviews.approveReview", "{author}/{slug}/versions/{version}/reviews/approve");
        register("reviews.editReview", "{author}/{slug}/versions/{version}/reviews/edit/{review}");
        register("reviews.stopReview", "{author}/{slug}/versions/{version}/reviews/stop");
        register("reviews.createReview", "{author}/{slug}/versions/{version}/reviews/init");
        register("reviews.takeoverReview", "{author}/{slug}/versions/{version}/reviews/takeover");
        register("reviews.reopenReview", "{author}/{slug}/versions/{version}/reviews/reopen");

        register("apiv1.showVersion", "api/v1/projects/{pluginId}/versions/{name}");
        register("apiv1.listProjects", "api/v1/projects", "categories", "sort", "q", "limit", "offset");
        register("apiv1.listUsers", "api/v1/users", "limit", "offset");
        register("apiv1.listVersions", "api/v1/projects/{pluginId}/versions", "channels", "limit", "offset");
        register("apiv1.revokeKey", "api/v1/projects/{pluginId}/keys/revoke");
        register("apiv1.createKey", "api/v1/projects/{pluginId}/keys/new");
        register("apiv1.showProject", "api/v1/projects/{pluginId}");
        register("apiv1.syncSso", "api/sync_sso");
        register("apiv1.tagColor", "api/v1/tags/{tagId}");
        register("apiv1.showStatusZ", "statusz");
        register("apiv1.showUser", "api/v1/users/{user}");
        register("apiv1.listPages", "api/v1/projects/{pluginId}/pages", "parentId");
        register("apiv1.deployVersion", "api/v1/projects/{pluginId}/versions/{name}");
        register("apiv1.listTags", "api/v1/projects/{plugin}/tags/{versionName}");
    }

    // TODO support route params
    public String getRouteUrl(String name, String... args) {
        Route route = routes.get(name);
        if (route == null) {
            throw new RuntimeException("No Route " + name);
        } else if(route.args.size() != args.length) {
            throw new RuntimeException("Args dont match for route " + name + " " + route.args.size() + "!=" + args.length);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(route.url);
        boolean first = true;
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            if (value == null || value.equals("")) {
                continue;
            }

            if (first) {
                sb.append("?");
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(route.args.get(i));
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }

    private void register(String name, String url, String... args) {
        Route route = new Route(name, url, List.of(args));
        routes.put(route.name, route);
    }

    class Route {
        String name;
        String url;
        List<String> args;

        public Route(String name, String url, List<String> args) {
            this.name = name;
            this.url = url;
            this.args = args;
        }
    }
}
