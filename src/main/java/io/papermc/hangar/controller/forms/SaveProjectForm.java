package io.papermc.hangar.controller.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controller.forms.objects.License;
import io.papermc.hangar.controller.forms.objects.ProjectLinks;
import io.papermc.hangar.model.Category;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class SaveProjectForm {

    private final Category category;
    private final String description;
    private final boolean forumSync;
    private final Collection<String> keywords;
    private final License license;
    private final ProjectLinks projectLinks;
    private final boolean iconChange;
    private final JoinableRoleUpdates projectRoleUpdates;

    @JsonCreator(mode = Mode.PROPERTIES)
    public SaveProjectForm(@NotNull @JsonProperty(value = "category", required = true) Category category,
                           @NotNull @JsonProperty(value = "description", required = true) String description,
                           @JsonProperty(value = "forumSync", required = true) boolean forumSync,
                           @NotNull @JsonProperty(value = "keywords", required = true) Collection<String> keywords,
                           @NotNull @JsonProperty(value = "license", required = true) License license,
                           @NotNull @JsonProperty(value = "links", required = true) ProjectLinks projectLinks,
                           @JsonProperty(value = "iconChange", required = true) boolean iconChange,
                           @NotNull @JsonProperty("members") JoinableRoleUpdates projectRoleUpdates) {
        this.category = category;
        this.description = description;
        this.forumSync = forumSync;
        this.keywords = keywords;
        this.license = license;
        this.projectLinks = projectLinks;
        this.iconChange = iconChange;
        this.projectRoleUpdates = projectRoleUpdates;
    }

    @NotNull
    public Category getCategory() {
        return category;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    @NotNull
    public Collection<String> getKeywords() {
        return keywords;
    }

    @NotNull
    public License getLicense() {
        return license;
    }

    @NotNull
    public ProjectLinks getProjectLinks() {
        return projectLinks;
    }

    public boolean isIconChange() {
        return iconChange;
    }

    public JoinableRoleUpdates getProjectRoleUpdates() {
        return projectRoleUpdates;
    }

    @Override
    public String toString() {
        return "SaveProjectForm{" +
                "category=" + category +
                ", description='" + description + '\'' +
                ", forumSync=" + forumSync +
                ", keywords=" + keywords +
                ", license=" + license +
                ", projectLinks=" + projectLinks +
                ", iconChange=" + iconChange +
                ", joinableRoleUpdates=" + projectRoleUpdates +
                '}';
    }
}
