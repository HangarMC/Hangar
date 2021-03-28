package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.projects.Visibility;

import javax.validation.constraints.NotNull;

public class VisibilityChangeForm {

    @NotNull
    private final Visibility visibility;
    private final String comment;

    @JsonCreator
    public VisibilityChangeForm(Visibility visibility, String comment) {
        this.visibility = visibility;
        this.comment = comment;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "VisibilityChangeForm{" +
                "visibility=" + visibility +
                ", comment='" + comment + '\'' +
                '}';
    }
}
