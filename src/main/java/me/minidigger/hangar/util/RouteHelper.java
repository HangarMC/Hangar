package me.minidigger.hangar.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RouteHelper {

    private Map<String, Route> routes = new HashMap<>();

    public RouteHelper() {
        register("showHome", "/");
        register("showFlags", "/flags");
        register("swagger", "/api");
        register("javaScriptRoutes", "/javascriptRoutes");
        register("linkout", "/linkout", "remoteUrl");
        // idk if these admin urls are right
        register("showQueue", "/queue");
        register("showProjectVisibility", "/idk");
        register("showHealth", "/admin/health");
        register("showStats", "/admin/stats");
        register("showLog", "/admin/log");

        register("projects.showCreator", "/new");

        register("org.showCreator", "/organisations/new");
        register("users.showAuthors", "/authors");
        register("users.showStaff", "/staff", "sort", "page");
        register("users.showProjects", "/");
        register("users.showNotifications", "/notifications");
        register("users.login", "/login", "returnUrl");
        register("users.signUp", "/signUp");
        register("users.logout", "/logout");
    }

    public String getRouteUrl(String name, String... args) {
        Route route = routes.get(name);
        if (route == null) {
            throw new RuntimeException("No Route " + name);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(route.url);
        boolean first = true;
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            if(value == null || value.equals("")) {
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
