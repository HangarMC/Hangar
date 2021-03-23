package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.projects.FlagReason;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class FlagForm {
    @NotBlank
    private final String comment;
    private final long projectId;
    @NotNull
    private final FlagReason reason;

    @JsonCreator
    public FlagForm(String comment, long projectId, FlagReason reason) {
        this.comment = comment;
        this.projectId = projectId;
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public long getProjectId() {
        return projectId;
    }

    public FlagReason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "FlagForm{" +
                "comment='" + comment + '\'' +
                ", projectId=" + projectId +
                ", reason=" + reason +
                '}';
    }
}
