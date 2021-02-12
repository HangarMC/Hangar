package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Version extends Model implements Named, Visible {

    private final String name;
    private final Map<Platform, Set<PluginDependency>> pluginDependencies;
    private final Map<Platform, Set<String>> platformDependencies;
    private final Visibility visibility;
    private final String description;
    private final VersionStats stats;
    private final FileInfo fileInfo;
    private final String author;
    private final ReviewState reviewState;
    private final Set<Tag> tags;

    public Version(OffsetDateTime createdAt, @ColumnName("version_string") String name, Visibility visibility, String description, @Nested("vs") VersionStats stats, @Nested("fi") FileInfo fileInfo, String author, @EnumByOrdinal ReviewState reviewState) {
        super(createdAt);
        this.name = name;
        this.tags = new HashSet<>();
        this.pluginDependencies = new EnumMap<>(Platform.class);
        this.platformDependencies = new EnumMap<>(Platform.class);
        this.visibility = visibility;
        this.description = description;
        this.stats = stats;
        this.fileInfo = fileInfo;
        this.author = author;
        this.reviewState = reviewState;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<Platform, Set<PluginDependency>> getPluginDependencies() {
        return pluginDependencies;
    }

    public Map<Platform, Set<String>> getPlatformDependencies() {
        return platformDependencies;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    public String getDescription() {
        return description;
    }

    public VersionStats getStats() {
        return stats;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public String getAuthor() {
        return author;
    }

    public ReviewState getReviewState() {
        return reviewState;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Version{" +
                "versionString='" + name + '\'' +
                ", pluginDependencies=" + pluginDependencies +
                ", platformDependencies=" + platformDependencies +
                ", visibility=" + visibility +
                ", description='" + description + '\'' +
                ", stats=" + stats +
                ", fileInfo=" + fileInfo +
                ", author='" + author + '\'' +
                ", reviewState=" + reviewState +
                ", tags=" + tags +
                "} " + super.toString();
    }
}
