package io.papermc.hangar.controller.extras;

import java.util.ArrayList;
import java.util.List;

public class RobotsBuilder {

    private final List<Group> groups;
    private final List<String> sitemaps;

    public RobotsBuilder() {
        this.groups = new ArrayList<>();
        this.sitemaps = new ArrayList<>();
    }

    public Group group(final String userAgent) {
        return new Group(userAgent, this);
    }

    public RobotsBuilder sitemap(final String sitemap) {
        this.sitemaps.add(sitemap);
        return this;
    }

    public String build() {
        final StringBuilder sb = new StringBuilder();
        for (final Group group : this.groups) {
            sb.append("user-agent: ").append(group.userAgent).append('\n');
            for (final String directive : group.directives) {
                sb.append(directive).append('\n');
            }
        }
        sb.append("\n\n");
        for (final String sitemap : this.sitemaps) {
            sb.append("Sitemap: ").append(sitemap).append("\n");
        }
        return sb.toString();
    }

    public static class Group {

        private final String userAgent;
        private final RobotsBuilder builder;
        private final List<String> directives;

        private Group(final String userAgent, final RobotsBuilder builder) {
            this.userAgent = userAgent;
            this.builder = builder;
            this.directives = new ArrayList<>();
        }

        public Group allow(final String path) {
            this.directives.add("Allow: " + path);
            return this;
        }

        public Group disallow(final String path) {
            this.directives.add("Disallow: " + path);
            return this;
        }

        public RobotsBuilder endGroup() {
            this.builder.groups.add(this);
            return this.builder;
        }
    }
}
