package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;

public abstract class Activity {

    private final ProjectNamespace namespace;

    protected Activity(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "namespace=" + namespace +
                '}';
    }
}
