package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.model.generated.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectMissingFile {

    private ProjectNamespace namespace;
    private ProjectVersionsTable version;

    public ProjectVersionsTable getVersion() {
        return version;
    }

    @Nested("version")
    public void setVersion(ProjectVersionsTable version) {
        this.version = version;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    @Nested("pn")
    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public String getDisplayText(){
        return namespace.toString() + "/" + version.getVersionString();
    }

    public String getOwner(){
        return getNamespace().getOwner();
    }

    public String getSlug(){
        return getNamespace().getSlug();
    }

    public String getVersionString(){
        return getVersion().getVersionString();
    }

    public String getFileName(){
        return getVersion().getFileName();
    }
}
