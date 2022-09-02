package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

public class ProjectChannelTable extends Table implements Named {

    private final long projectId;
    private final Set<ChannelFlag> flags;
    private String name;
    private Color color;

    @JdbiConstructor
    public ProjectChannelTable(final OffsetDateTime createdAt, final long id, final String name, @EnumByOrdinal final Color color, final long projectId, final Set<ChannelFlag> flags) {
        super(createdAt, id);
        this.name = name;
        this.color = color;
        this.projectId = projectId;
        this.flags = new HashSet<>(flags); // to ensure mutability
    }

    public ProjectChannelTable(final String name, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        this.name = name;
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
            ", color=" + this.color +
            ", projectId=" + this.projectId +
            ", flags=" + this.flags +
            "} " + super.toString();
    }
}
