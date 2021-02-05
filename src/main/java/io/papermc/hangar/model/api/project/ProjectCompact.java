package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.api.project.version.PromotedVersion;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;
import java.util.List;

public class ProjectCompact extends Model implements Named, Visible {
    
    protected final String name;
    protected final ProjectNamespace namespace;
    protected final List<PromotedVersion> promotedVersions;
    protected final ProjectStats stats;
    protected final Category category;
    protected final Visibility visibility;

    public ProjectCompact(OffsetDateTime createdAt, String name, @Nested ProjectNamespace namespace, List<PromotedVersion> promotedVersions, @Nested ProjectStats stats, @EnumByOrdinal Category category, @EnumByOrdinal Visibility visibility) {
        super(createdAt);
        this.name = name;
        this.namespace = namespace;
        this.promotedVersions = promotedVersions;
        this.stats = stats;
        this.category = category;
        this.visibility = visibility;
    }

    @Override
    public String getName() {
        return name;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public List<PromotedVersion> getPromotedVersions() {
        return promotedVersions;
    }

    public ProjectStats getStats() {
        return stats;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "ProjectCompact{" +
                "name='" + name + '\'' +
                ", namespace=" + namespace +
                ", promotedVersions=" + promotedVersions +
                ", stats=" + stats +
                ", category=" + category +
                ", visibility=" + visibility +
                "} " + super.toString();
    }
}
