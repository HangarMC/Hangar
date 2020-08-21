package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.generated.Project;
import org.jetbrains.annotations.Nullable;

public class LoggedProject {

    private Project project;
    private String pluginId;
    private String slug;
    private String owner;

    public LoggedProject(@Nullable Project project, @Nullable String pluginId, @Nullable String slug, @Nullable String owner) {
        this.project = project;
        this.pluginId = pluginId;
        this.slug = slug;
        this.owner = owner;
    }

    @Nullable
    public Project getProject() {
        return project;
    }

    @Nullable
    public String getPluginId() {
        return pluginId;
    }

    @Nullable
    public String getSlug() {
        return slug;
    }

    @Nullable
    public String getOwner() {
        return owner;
    }
}
