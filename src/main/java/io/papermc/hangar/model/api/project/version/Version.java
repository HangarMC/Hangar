package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.ReviewState;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.Visible;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Version extends Model implements Named, Visible {

    private final String name;
    private final String urlPath;
    private final Map<Platform, List<Dependency>> dependencies;
    private final Visibility visibility;
    private final String description;
    private final VersionStats stats;
    private final FileInfo fileInfo;
    private final String author;
    private final ReviewState reviewState;
    private final List<Tag> tags;

    public Version(OffsetDateTime createdAt, @ColumnName("version_string") String name, String urlPath, @EnumByOrdinal Visibility visibility, String description, @Nested("vs") VersionStats stats, @Nested("fi") FileInfo fileInfo, String author, @EnumByOrdinal ReviewState reviewState) {
        super(createdAt);
        this.name = name;
        this.urlPath = urlPath;
        this.dependencies = new HashMap<>();
        this.visibility = visibility;
        this.description = description;
        this.stats = stats;
        this.fileInfo = fileInfo;
        this.author = author;
        this.reviewState = reviewState;
        this.tags = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public Map<Platform, List<Dependency>> getDependencies() {
        return dependencies;
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

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Version{" +
                "name='" + name + '\'' +
                ", urlPath='" + urlPath + '\'' +
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
