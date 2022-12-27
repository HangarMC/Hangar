package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.projects.Visibility;

import javax.validation.constraints.NotNull;

public class VisibilityChangeForm {

    private final @NotNull Visibility visibility;
    private final String comment;

    @JsonCreator
    public VisibilityChangeForm(final Visibility visibility, final String comment) {
        this.visibility = visibility;
        this.comment = comment;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public String getComment() {
        return this.comment;
    }

    @Override
    public String toString() {
        return "VisibilityChangeForm{" +
                "visibility=" + this.visibility +
                ", comment='" + this.comment + '\'' +
                '}';
    }
}
