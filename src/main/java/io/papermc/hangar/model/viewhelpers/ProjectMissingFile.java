package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectVersionsTable;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectMissingFile {

    private String owner;
    private String name;
    private ProjectVersionsTable version;

    public ProjectVersionsTable getVersion() {
        return version;
    }

    @Nested("version")
    public void setVersion(ProjectVersionsTable version) {
        this.version = version;
    }

    public String getDisplayText(){
        return owner + "/" + name + "/" + version.getVersionString();
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner(){
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getVersionString(){
        return getVersion().getVersionString();
    }

    public String getFileName(){
        return getVersion().getFileName();
    }
}
