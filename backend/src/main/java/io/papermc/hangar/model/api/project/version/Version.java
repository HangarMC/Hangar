package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.api.project.ProjectChannel;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class Version extends Model implements Named, Visible, Identified {

    private final long id;
    private final String name;
    private final Visibility visibility;
    private final String description;
    private final VersionStats stats;
    private final String author;
    private final ReviewState reviewState;
    private final ProjectChannel channel;
    private final PinnedStatus pinnedStatus;
    private final Map<Platform, PlatformVersionDownload> downloads;
    private final Map<Platform, Set<PluginDependency>> pluginDependencies;
    private final Map<Platform, Set<String>> platformDependencies;
    private final Map<Platform, List<String>> platformDependenciesFormatted;

    public Version(final OffsetDateTime createdAt,
                   final long id,
                   @ColumnName("version_string") final String name,
                   final Visibility visibility,
                   final String description,
                   @Nested("vs") final VersionStats stats,
                   final String author,
                   @EnumByOrdinal final ReviewState reviewState,
                   @Nested("pc") final ProjectChannel channel,
                   final PinnedStatus pinnedStatus,
                   final Map<Platform, PlatformVersionDownload> downloads,
                   final Map<Platform, Set<PluginDependency>> pluginDependencies,
                   final Map<Platform, Set<String>> platformDependencies,
                   final Map<Platform, List<String>> platformDependenciesFormatted) {
        super(createdAt);
        this.id = id;
        this.name = name;
        this.visibility = visibility;
        this.description = description;
        this.stats = stats;
        this.author = author;
        this.reviewState = reviewState;
        this.channel = channel;
        this.pinnedStatus = pinnedStatus;
        this.downloads = downloads;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.platformDependenciesFormatted = platformDependenciesFormatted;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    public String getDescription() {
        return this.description;
    }

    public VersionStats getStats() {
        return this.stats;
    }

    public String getAuthor() {
        return this.author;
    }

    public ReviewState getReviewState() {
        return this.reviewState;
    }

    public ProjectChannel getChannel() {
        return this.channel;
    }

    public PinnedStatus getPinnedStatus() {
        return this.pinnedStatus;
    }

    public Map<Platform, PlatformVersionDownload> getDownloads() {
        return this.downloads;
    }

    public Map<Platform, Set<PluginDependency>> getPluginDependencies() {
        return this.pluginDependencies;
    }

    public Map<Platform, Set<String>> getPlatformDependencies() {
        return this.platformDependencies;
    }

    public Map<Platform, List<String>> getPlatformDependenciesFormatted() {
        return this.platformDependenciesFormatted;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", visibility=" + this.visibility +
                ", description='" + this.description + '\'' +
                ", stats=" + this.stats +
                ", author='" + this.author + '\'' +
                ", reviewState=" + this.reviewState +
                ", channel=" + this.channel +
                ", pinnedStatus=" + this.pinnedStatus +
                ", downloads=" + this.downloads +
                ", pluginDependencies=" + this.pluginDependencies +
                ", platformDependencies=" + this.platformDependencies +
                ", platformDependenciesFormatted=" + this.platformDependenciesFormatted +
                "} " + super.toString();
    }

    @EnumByName
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum PinnedStatus {
        NONE,
        VERSION,
        CHANNEL
    }
}
