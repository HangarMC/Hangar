package io.papermc.hangar.model.internal.admin.activity;

import io.papermc.hangar.model.api.project.ProjectNamespace;

public abstract class Activity {

    private final ProjectNamespace namespace;

    protected Activity(final ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    @Override
    public String toString() {
        return "Activity{" +
            "namespace=" + this.namespace +
            '}';
    }
}
