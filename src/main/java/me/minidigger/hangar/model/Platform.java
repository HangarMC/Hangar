package me.minidigger.hangar.model;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.model.generated.TagColor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public enum Platform { // TODO add platforms

    PAPER("Paper", PlatformCategory.SERVER_CATEGORY, 0, "paper-api", TagColor.PAPER, "https://papermc.io/downloads");

    private final String name;
    private final PlatformCategory platformCategory;
    private final int priority;
    private final String dependencyId;
    private final TagColor tagColor;
    private final String url;

    Platform(String name, PlatformCategory platformCategory, int priority, String dependencyId, TagColor tagColor, String url) {
        this.name = name;
        this.platformCategory = platformCategory;
        this.priority = priority;
        this.dependencyId = dependencyId;
        this.tagColor = tagColor;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public PlatformCategory getPlatformCategory() {
        return platformCategory;
    }

    public int getPriority() {
        return priority;
    }

    public String getDependencyId() {
        return dependencyId;
    }

    public TagColor getTagColor() {
        return tagColor;
    }

    public String getUrl() {
        return url;
    }

    public ProjectVersionTagsTable createGhostTag(long versionId, String version) {
        return new ProjectVersionTagsTable(-1, versionId, name, version, tagColor);
    }

    public static List<Platform> getPlatforms(List<String> dependencyIds) { // OMFG
        return Arrays.stream(Platform.values()).filter(p -> dependencyIds.contains(p.dependencyId)).collect(Collectors.groupingBy(Platform::getPlatformCategory)).entrySet().stream().flatMap(entry -> entry.getValue().stream().collect(Collectors.groupingBy(Platform::getPriority)).entrySet().stream().max(Comparator.comparingInt(Entry::getKey)).get().getValue().stream()).collect(Collectors.toList());
    }

    public static List<ProjectVersionTagsTable> getGhostTags(long versionId, List<Dependency> dependencies) {
        return getPlatforms(dependencies.stream().map(Dependency::getPluginId).collect(Collectors.toList())).stream().map(p -> p.createGhostTag(versionId, dependencies.stream().filter(d -> d.getPluginId().equals(p.dependencyId)).findFirst().get().getVersion())).collect(Collectors.toList());
    }

    public enum PlatformCategory {
        SERVER_CATEGORY("Server Plugins", "Server"),
        PROXY_CATEGORY("Proxy Plugins", "Proxy");

        private final String name;
        private final String tagName;

        PlatformCategory(String name, String tagName) {
            this.name = name;
            this.tagName = tagName;
        }

        public String getName() {
            return name;
        }

        public String getTagName() {
            return tagName;
        }

        public List<Platform> getPlatforms() {
            return Arrays.stream(Platform.values()).filter(p -> p.platformCategory == this).collect(Collectors.toList());
        }
    }
}
