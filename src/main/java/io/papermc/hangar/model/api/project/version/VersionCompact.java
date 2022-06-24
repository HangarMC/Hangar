package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.api.project.ProjectChannel;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class VersionCompact extends Model implements Named, Visible {

    private final String name;
    private final Visibility visibility;
    private final String description;
    private final VersionStats stats;
    private final FileInfo fileInfo;
    private final String externalUrl;
    private final String author;
    private final ReviewState reviewState;
    private final ProjectChannel channel;
    private final PinnedStatus pinnedStatus;
    private final List<Platform> recommended;
    private final Long postId;

    protected VersionCompact(final OffsetDateTime createdAt, @ColumnName("version_string") final String name, final Visibility visibility, final String description, @Nested("vs") final VersionStats stats, @Nested("fi") final FileInfo fileInfo, final String externalUrl, final String author, @EnumByOrdinal final ReviewState reviewState, @Nested("pc") final ProjectChannel channel, final PinnedStatus pinnedStatus, final List<Platform> recommended, final Long postId) {
        super(createdAt);
        this.name = name;
        this.visibility = visibility;
        this.description = description;
        this.stats = stats;
        this.fileInfo = fileInfo;
        this.externalUrl = externalUrl;
        this.author = author;
        this.reviewState = reviewState;
        this.channel = channel;
        this.pinnedStatus = pinnedStatus;
        this.recommended = recommended;
        this.postId = postId;
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

    public FileInfo getFileInfo() {
        return this.fileInfo;
    }

    public String getExternalUrl() {
        return this.externalUrl;
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

    public List<Platform> getRecommended() {
        return this.recommended;
    }

    public Long getPostId() {
        return this.postId;
    }

    @EnumByName
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum PinnedStatus {
        NONE,
        VERSION,
        CHANNEL
    }
}
