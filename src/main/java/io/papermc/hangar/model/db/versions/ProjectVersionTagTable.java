package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.List;

public class ProjectVersionTagTable extends Table implements Named {

    private final long versionId;
    private final String name;
    private final List<String> data;
    private final TagColor color;

    @JdbiConstructor
    public ProjectVersionTagTable(OffsetDateTime createdAt, long id, long versionId, String name, List<String> data, @EnumByOrdinal TagColor color) {
        super(createdAt, id);
        this.versionId = versionId;
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public ProjectVersionTagTable(long versionId, String name, List<String> data, TagColor color) {
        this.versionId = versionId;
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public long getVersionId() {
        return versionId;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getData() {
        return data;
    }

    @EnumByOrdinal
    public TagColor getColor() {
        return color;
    }
}
