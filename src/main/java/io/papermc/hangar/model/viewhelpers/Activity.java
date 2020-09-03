package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

public abstract class Activity {

    private ProjectNamespace project;

    public ProjectNamespace getProject() {
        return project;
    }

    @Nested
    public void setProject(ProjectNamespace project) {
        this.project = project;
    }
}
