package io.papermc.hangar.db.model;

import io.papermc.hangar.model.TagColor;

import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.util.List;

public class ProjectVersionTagsTable {

    private long id;
    private long versionId;
    private String name;
    private List<String> data;
    private TagColor color;

    public ProjectVersionTagsTable(long id, long versionId, String name, List<String> data, TagColor color) {
        this.id = id;
        this.versionId = versionId;
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public ProjectVersionTagsTable() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }


    @EnumByOrdinal
    public TagColor getColor() {
        return color;
    }

    @EnumByOrdinal
    public void setColor(TagColor color) {
        this.color = color;
    }

}
