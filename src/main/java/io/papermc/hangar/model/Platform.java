package io.papermc.hangar.model;

import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.service.VersionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public enum Platform {

    PAPER("Paper", PlatformCategory.SERVER_CATEGORY, 0, "paperapi", TagColor.PAPER, "https://papermc.io/downloads", List.of("1.8", "1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16")),
    WATERFALL("Waterfall", PlatformCategory.PROXY_CATEGORY, 1, "waterfall", TagColor.WATERFALL, "https://papermc.io/downloads#Waterfall", List.of("1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17")),
    VELOCITY("Velocity", PlatformCategory.PROXY_CATEGORY, 1, "velocity", TagColor.VELOCITY, "https://www.velocitypowered.com/downloads", List.of());

    private static final Map<String, Platform> PLATFORMS_BY_DEPENDENDY = new HashMap<>();

    static {
        for (Platform platform : values()) {
            PLATFORMS_BY_DEPENDENDY.put(platform.dependencyId, platform);
        }
    }

    private final String name;
    private final PlatformCategory platformCategory;
    private final int priority;
    private final String dependencyId;
    private final TagColor tagColor;
    private final String url;
    private final List<String> possibleVersions;

    Platform(String name, PlatformCategory platformCategory, int priority, String dependencyId, TagColor tagColor, String url, List<String> possibleVersions) {
        this.name = name;
        this.platformCategory = platformCategory;
        this.priority = priority;
        this.dependencyId = dependencyId;
        this.tagColor = tagColor;
        this.url = url;
        this.possibleVersions = possibleVersions;
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

    public List<String> getPossibleVersions() {
        return possibleVersions;
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

    public static List<Pair<Platform, ProjectVersionTagsTable>> getGhostTags(long versionId, List<Dependency> dependencies) {
        return getPlatforms(
                dependencies
                .stream()
                .map(Dependency::getPluginId)
                .collect(Collectors.toList())
        ).stream().map(p -> new ImmutablePair<>(
                p,
                p.createGhostTag(
                        versionId,
                        dependencies
                        .stream()
                        .filter(d -> d.getPluginId().equalsIgnoreCase(p.dependencyId))
                        .findFirst()
                        .get()
                        .getVersion()
                )
        )).collect(Collectors.toList());
    }

    @Nullable
    public static Platform getByDependencyId(String dependencyId) {
        return PLATFORMS_BY_DEPENDENDY.get(dependencyId.toLowerCase());
    }

    public static List<ProjectVersionTagsTable> createPlatformTags(VersionService versionService, long versionId, List<Dependency> dependencies) {
        return versionService.insertTags(getGhostTags(versionId, dependencies).stream().map(Pair::getRight).collect(Collectors.toList()));
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
