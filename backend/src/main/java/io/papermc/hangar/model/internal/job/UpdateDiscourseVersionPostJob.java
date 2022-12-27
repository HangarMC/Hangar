package io.papermc.hangar.model.internal.job;

import io.papermc.hangar.model.db.JobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateDiscourseVersionPostJob extends Job {

    private long versionId;

    public UpdateDiscourseVersionPostJob(final long versionId) {
        super(JobType.UPDATE_DISCOURSE_VERSION_POST);
        this.versionId = versionId;
    }

    UpdateDiscourseVersionPostJob() {
        super(JobType.UPDATE_DISCOURSE_VERSION_POST);
    }

    public long getVersionId() {
        return this.versionId;
    }

    public void setVersionId(final long versionId) {
        this.versionId = versionId;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() != null && this.getJobProperties().containsKey("versionId")) {
            this.versionId = Long.parseLong(this.getJobProperties().get("versionId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("versionId", this.versionId + "");
        this.setJobProperties(properties);
    }

    public static UpdateDiscourseVersionPostJob loadFromTable(final JobTable table) {
        final UpdateDiscourseVersionPostJob job = new UpdateDiscourseVersionPostJob();
        job.fromTable(table);
        job.setJobProperties(table.getJobProperties().getMap());
        job.loadFromProperties();
        return job;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final UpdateDiscourseVersionPostJob that = (UpdateDiscourseVersionPostJob) o;
        return this.versionId == that.versionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.versionId);
    }

    @Override
    public String toString() {
        return "UpdateDiscourseVersionPostJob{" +
            "createdAt=" + this.createdAt +
            ", versionId=" + this.versionId +
            "} " + super.toString();
    }
}
