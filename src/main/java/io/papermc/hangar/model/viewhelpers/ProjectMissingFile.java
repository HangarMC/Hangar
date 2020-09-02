package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

public class ProjectMissingFile {

    private ProjectNamespace namespace;
    private String version;
    private String fileName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    @Nested("pn")
    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public String getDisplayText(){
        return namespace.toString() + "/" + version;
    }
}
