package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectChannelTable extends Table implements Named {

    private String name;
    private Color color;
    private final long projectId;
    private boolean nonReviewed;
    private boolean editable;

    @JdbiConstructor
    public ProjectChannelTable(OffsetDateTime createdAt, long id, String name, @EnumByOrdinal Color color, long projectId, boolean nonReviewed, boolean editable) {
        super(createdAt, id);
        this.name = name;
        this.color = color;
        this.projectId = projectId;
        this.nonReviewed = nonReviewed;
        this.editable = editable;
    }

    public ProjectChannelTable(String name, Color color, long projectId, boolean nonReviewed, boolean editable) {
        this.name = name;
        this.color = color;
        this.projectId = projectId;
        this.nonReviewed = nonReviewed;
        this.editable = editable;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @EnumByOrdinal
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public long getProjectId() {
        return projectId;
    }

    public boolean isNonReviewed() {
        return nonReviewed;
    }

    public void setNonReviewed(boolean nonReviewed) {
        this.nonReviewed = nonReviewed;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public String toString() {
        return "ProjectChannelTable{" +
                "name='" + name + '\'' +
                ", color=" + color +
                ", projectId=" + projectId +
                ", nonReviewed=" + nonReviewed +
               ", editable=" + editable +
                "} " + super.toString();
    }
}
