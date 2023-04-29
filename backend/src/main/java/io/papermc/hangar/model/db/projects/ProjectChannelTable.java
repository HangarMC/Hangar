package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectChannelTable extends Table implements Named {

    private final long projectId;
    private final Set<ChannelFlag> flags;
    private String name;
    private String description;
    private Color color;

    @JdbiConstructor
    public ProjectChannelTable(final OffsetDateTime createdAt, final long id, final String name, final String description, @EnumByOrdinal final Color color, final long projectId, final Set<ChannelFlag> flags) {
        super(createdAt, id);
        this.name = name;
        this.description = description;
        this.color = color;
        this.projectId = projectId;
        this.flags = new HashSet<>(flags); // to ensure mutability
    }

    public ProjectChannelTable(final String name, final String description, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.projectId = projectId;
        this.flags = new HashSet<>(flags); // to ensure mutability
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @EnumByOrdinal
    public Color getColor() {
        return this.color;
    }

    public void setColor(final Color color) {
        this.color = color;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public Set<ChannelFlag> getFlags() {
        return this.flags;
    }

    public void setFlags(final Set<ChannelFlag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
    }

    @Override
    public String toString() {
        return "ProjectChannelTable{" +
            "name='" + this.name + '\'' +
            ", description=" + this.description +
            ", color=" + this.color +
            ", projectId=" + this.projectId +
            ", flags=" + this.flags +
            "} " + super.toString();
    }
}
