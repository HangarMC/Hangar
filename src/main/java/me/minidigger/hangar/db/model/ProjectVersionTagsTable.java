package me.minidigger.hangar.db.model;


public class ProjectVersionTagsTable {

    private long id;
    private long versionId;
    private String name;
    private String data;
    private long color;


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


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }

}
