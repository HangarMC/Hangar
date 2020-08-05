package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.generated.ProjectNamespace;

public abstract class Activity {

    private ProjectNamespace project;

    protected Activity(ProjectNamespace project) {
        this.project = project;
    }

    public ProjectNamespace getProject() {
        return project;
    }
}
