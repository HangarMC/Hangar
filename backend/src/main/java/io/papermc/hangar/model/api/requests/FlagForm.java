package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.projects.FlagReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FlagForm {
    @NotBlank
    private final String comment;
    private final long projectId;
    @NotNull
    private final FlagReason reason;

    @JsonCreator
    public FlagForm(final String comment, final long projectId, final FlagReason reason) {
        this.comment = comment;
        this.projectId = projectId;
        this.reason = reason;
    }

    public String getComment() {
        return this.comment;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public FlagReason getReason() {
        return this.reason;
    }

    @Override
    public String toString() {
        return "FlagForm{" +
            "comment='" + this.comment + '\'' +
            ", projectId=" + this.projectId +
            ", reason=" + this.reason +
            '}';
    }
}
