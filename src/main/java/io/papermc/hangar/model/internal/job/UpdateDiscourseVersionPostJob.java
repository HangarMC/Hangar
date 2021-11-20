package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.model.db.JobTable;

public class UpdateDiscourseVersionPostJob extends Job {

    private long versionId;

    public UpdateDiscourseVersionPostJob(long versionId) {
        super(JobType.UPDATE_DISCOURSE_VERSION_POST);
        this.versionId = versionId;
    }

    UpdateDiscourseVersionPostJob() {
        super(JobType.UPDATE_DISCOURSE_VERSION_POST);
    }

    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }

    @Override
    public void loadFromProperties() {
        if (getJobProperties() != null && getJobProperties().containsKey("versionId")) {
            versionId = Long.parseLong(getJobProperties().get("versionId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("versionId", versionId + "");
        setJobProperties(properties);
    }

    public static UpdateDiscourseVersionPostJob loadFromTable(JobTable table) {
        UpdateDiscourseVersionPostJob job  = new UpdateDiscourseVersionPostJob();
        job.fromTable(table);
        job.setJobProperties(table.getJobProperties().getMap());
        job.loadFromProperties();
        return job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UpdateDiscourseVersionPostJob that = (UpdateDiscourseVersionPostJob) o;
        return versionId == that.versionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), versionId);
    }

    @Override
    public String toString() {
        return "UpdateDiscourseVersionPostJob{" +
               "createdAt=" + createdAt +
               ", versionId=" + versionId +
               "} " + super.toString();
    }
}
