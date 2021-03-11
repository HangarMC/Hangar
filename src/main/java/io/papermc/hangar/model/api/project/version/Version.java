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

// TODO CompactVersion w/o dependencies for the version list page
public class Version extends Model implements Named, Visible {

    private final String name;
    private final Map<Platform, Set<PluginDependency>> pluginDependencies;
    private final Map<Platform, Set<String>> platformDependencies;
    private final Visibility visibility;
    private final String description;
    private final VersionStats stats;
    private final FileInfo fileInfo;
    private final String externalUrl;
    private final String author;
    private final ReviewState reviewState;
    private final Set<Tag> tags;
    private final boolean recommended;

    public Version(OffsetDateTime createdAt, @ColumnName("version_string") String name, Visibility visibility, String description, @Nested("vs") VersionStats stats, @Nested("fi") FileInfo fileInfo, String externalUrl, String author, @EnumByOrdinal ReviewState reviewState, boolean recommended) {
        super(createdAt);
        this.name = name;
        this.externalUrl = externalUrl;
        this.recommended = recommended;
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

    public String getExternalUrl() {
        return externalUrl;
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

    public boolean isRecommended() {
        return recommended;
    }

    @Override
    public String toString() {
        return "Version{" +
                "name='" + name + '\'' +
                ", pluginDependencies=" + pluginDependencies +
                ", platformDependencies=" + platformDependencies +
                ", visibility=" + visibility +
                ", description='" + description + '\'' +
                ", stats=" + stats +
                ", fileInfo=" + fileInfo +
                ", externalUrl='" + externalUrl + '\'' +
                ", author='" + author + '\'' +
                ", reviewState=" + reviewState +
                ", tags=" + tags +
                ", recommended=" + recommended +
                "} " + super.toString();
    }
}
