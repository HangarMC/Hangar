package io.papermc.hangar.controllerold.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

import io.papermc.hangar.model.common.projects.FlagReason;

public class FlagForm {
    private final String comment;
    private final long projectId;
    private final FlagReason reason;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FlagForm(@JsonProperty(value = "comment", required = true) @NotNull String comment,
                         @JsonProperty(value = "project_id", required = true) long projectId,
                         @JsonProperty(value = "reason", required = true) @NotNull FlagReason reason) {
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
        return new StringJoiner(", ", FlagForm.class.getSimpleName() + "[", "]")
                .add("comment='" + comment + "'")
                .add("projectId=" + projectId)
                .add("reason=" + reason)
                .toString();
    }
}
