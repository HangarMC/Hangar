package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.ProjectNamespace;

public abstract class Activity {

    private ProjectNamespace project;

    protected Activity() {
        //
    }

    protected Activity(ProjectNamespace project) {
        this.project = project;
    }

    public ProjectNamespace getProject() {
        return project;
    }

    public void setProject(ProjectNamespace project) {
        this.project = project;
    }
}
