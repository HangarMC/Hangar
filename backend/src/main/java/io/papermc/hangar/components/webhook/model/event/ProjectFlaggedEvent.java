package io.papermc.hangar.components.webhook.model.event;

import java.util.List;
import java.util.Objects;

public class ProjectFlaggedEvent extends ProjectEvent {

    public static final String TYPE = "project_flagged";

    private final String flaggerName;
    private final String flaggerUrl;
    private final String flagUrl;
    private final String reason;
    private final String comment;

    public ProjectFlaggedEvent(final String flaggerName, final String flaggerUrl, final String reason, final String comment, final String projectAuthor, final String projectName, final String projectAvatar, final String projectUrl, final List<String> projectPlatforms, final String flagUrl) {
        super(TYPE, projectAuthor, projectName, projectAvatar, projectUrl, projectPlatforms);
        this.flaggerName = flaggerName;
        this.flaggerUrl = flaggerUrl;
        this.reason = reason;
        this.comment = comment;
        this.flagUrl = flagUrl;
    }

    public String getFlaggerName() {
        return this.flaggerName;
    }

    public String getFlaggerUrl() {
        return this.flaggerUrl;
    }

    public String getFlagUrl() {
        return this.flagUrl;
    }

    public String getReason() {
        return this.reason;
    }

    public String getComment() {
        return this.comment;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectFlaggedEvent that = (ProjectFlaggedEvent) o;
        return Objects.equals(this.flaggerName, that.flaggerName) && Objects.equals(this.flaggerUrl, that.flaggerUrl) && Objects.equals(this.flagUrl, that.flagUrl) && Objects.equals(this.reason, that.reason) && Objects.equals(this.comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.flaggerName, this.flaggerUrl, this.flagUrl, this.reason, this.comment);
    }

    @Override
    public String toString() {
        return "ProjectFlaggedEvent{" +
            "flaggerName='" + this.flaggerName + '\'' +
            ", flaggerUrl='" + this.flaggerUrl + '\'' +
            ", flagUrl='" + this.flagUrl + '\'' +
            ", reason='" + this.reason + '\'' +
            ", comment='" + this.comment + '\'' +
            "} " + super.toString();
    }
}
