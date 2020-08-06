package me.minidigger.hangar.model;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.service.VersionService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public enum Platform {

    PAPER("Paper", PlatformCategory.SERVER_CATEGORY, 0, "paperapi", TagColor.PAPER, "https://papermc.io/downloads"),
    WATERFALL("Waterfall", PlatformCategory.PROXY_CATEGORY, 1, "waterfall", TagColor.WATERFALL, "https://papermc.io/downloads#Waterfall"),
    VELOCITY("Velocity", PlatformCategory.PROXY_CATEGORY, 1, "velocity", TagColor.VELOCITY, "https://www.velocitypowered.com/downloads");

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

    private static final Platform[] VALUES = Platform.values();

    public static Platform[] getValues() {
        return VALUES;
    }

    public static List<Platform> getPlatforms(List<String> dependencyIds) { // OMFG
        return Arrays.stream(Platform.getValues())
                .filter(p -> dependencyIds.contains(p.dependencyId))
                .collect(Collectors.groupingBy(Platform::getPlatformCategory))
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream()
                        .collect(Collectors.groupingBy(Platform::getPriority))
                        .entrySet()
                        .stream()
                        .max(Comparator.comparingInt(Entry::getKey))
                        .get()
                        .getValue()
                        .stream())
                .collect(Collectors.toList());
    }

    public static List<ProjectVersionTagsTable> getGhostTags(long versionId, List<Dependency> dependencies) {
        return getPlatforms(
                dependencies
                        .stream()
                        .map(Dependency::getPluginId)
                        .collect(Collectors.toList())
        ).stream()
                .map(p -> p.createGhostTag(
                        versionId,
                        dependencies
                                .stream()
                                .filter(d -> d.getPluginId().equals(p.dependencyId))
                                .findFirst()
                                .get()
                                .getVersion()
                ))
                .collect(Collectors.toList());
    }

    public static List<ProjectVersionTagsTable> createPlatformTags(VersionService versionService, long versionId, List<Dependency> dependencies) {
        return versionService.insertTags(getGhostTags(versionId, dependencies));
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
            return Arrays.stream(Platform.getValues()).filter(p -> p.platformCategory == this).collect(Collectors.toList());
        }
    }
}
