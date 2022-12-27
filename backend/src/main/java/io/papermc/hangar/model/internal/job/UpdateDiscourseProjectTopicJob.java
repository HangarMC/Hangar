package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.model.db.JobTable;

public class UpdateDiscourseProjectTopicJob extends Job {

    private long projectId;

    public UpdateDiscourseProjectTopicJob(final long projectId) {
        super(JobType.UPDATE_DISCOURSE_PROJECT_TOPIC);
        this.projectId = projectId;
    }

    UpdateDiscourseProjectTopicJob() {
        super(JobType.UPDATE_DISCOURSE_PROJECT_TOPIC);
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() != null && this.getJobProperties().containsKey("projectId")) {
            this.projectId = Long.parseLong(this.getJobProperties().get("projectId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("projectId", this.projectId + "");
        this.setJobProperties(properties);
    }

    public static UpdateDiscourseProjectTopicJob loadFromTable(final JobTable table) {
        final UpdateDiscourseProjectTopicJob job  = new UpdateDiscourseProjectTopicJob();
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
        final UpdateDiscourseProjectTopicJob that = (UpdateDiscourseProjectTopicJob) o;
        return this.projectId == that.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.projectId);
    }

    @Override
    public String toString() {
        return "UpdateDiscourseProjectTopicJob{" +
               "createdAt=" + this.createdAt +
               ", projectId=" + this.projectId +
               "} " + super.toString();
    }
}
