package io.papermc.hangar.modelold;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonValue;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.PlatformVersionsDao;
import io.papermc.hangar.db.modelold.ProjectVersionTagsTable;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.modelold.generated.PlatformDependency;
import io.papermc.hangar.serviceold.VersionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@JsonFormat(shape = Shape.OBJECT)
public enum Platform {

    PAPER("Paper", PlatformCategory.SERVER_CATEGORY, 0, TagColor.PAPER, "https://papermc.io/downloads"),
    WATERFALL("Waterfall", PlatformCategory.PROXY_CATEGORY, 1, TagColor.WATERFALL, "https://papermc.io/downloads#Waterfall"),
    VELOCITY("Velocity", PlatformCategory.PROXY_CATEGORY, 1, TagColor.VELOCITY, "https://www.velocitypowered.com/downloads");

    private final String name;
    private final PlatformCategory platformCategory;
    private final int priority;
    private final TagColor tagColor;
    private final String url;
    private HangarDao<PlatformVersionsDao> platformVersionsDao;

    Platform(String name, PlatformCategory platformCategory, int priority, TagColor tagColor, String url) {
        this.name = name;
        this.platformCategory = platformCategory;
        this.priority = priority;
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

    public TagColor getTagColor() {
        return tagColor;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getPossibleVersions() {
        return platformVersionsDao.get().getVersionsForPlatform(this.ordinal());
    }

    private void setPlatformVersionsDao(HangarDao<PlatformVersionsDao> platformVersionsDao) {
        this.platformVersionsDao = platformVersionsDao;
    }

    public ProjectVersionTagsTable createGhostTag(long versionId, List<String> version) {
        return new ProjectVersionTagsTable(-1, versionId, name, version, tagColor);
    }

    private static final Platform[] VALUES = Platform.values();

    public static Platform[] getValues() {
        return VALUES;
    }

    public static List<Pair<Platform, ProjectVersionTagsTable>> getGhostTags(long versionId, List<PlatformDependency> platformDependencies) {
        return platformDependencies.stream().map(dep -> new ImmutablePair<>(
                dep.getPlatform(),
                dep.getPlatform().createGhostTag(
                        versionId,
                        dep.getVersions()
                )
        )).collect(Collectors.toList());
    }

    @Nullable
    public static Platform getByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        for (Platform pl : VALUES) {
            if (pl.name.equalsIgnoreCase(name)) {
                return pl;
            }
        }
        return null;
    }

    public static List<ProjectVersionTagsTable> createPlatformTags(VersionService versionService, long versionId, List<PlatformDependency> platformDependencies) {
        return versionService.insertTags(getGhostTags(versionId, platformDependencies).stream().map(Pair::getRight).collect(Collectors.toList()));
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

        @JsonValue
        public String getTagName() {
            return tagName;
        }

        public List<Platform> getPlatforms() {
            return Arrays.stream(Platform.getValues()).filter(p -> p.platformCategory == this).collect(Collectors.toList());
        }
    }

    @Component
    public static class PlatformInjector {

        private final HangarDao<PlatformVersionsDao> platformVersionsDao;

        @Autowired
        public PlatformInjector(HangarDao<PlatformVersionsDao> platformVersionsDao) {
            this.platformVersionsDao = platformVersionsDao;
        }

        @PostConstruct
        public void postConstruct() {
            for (Platform platform : EnumSet.allOf(Platform.class)) {
                platform.setPlatformVersionsDao(platformVersionsDao);
            }
        }

    }
}
