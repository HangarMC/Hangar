package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.model.db.JobTable;

public class UpdateDiscourseProjectTopicJob extends Job {

    private long projectId;

    public UpdateDiscourseProjectTopicJob(long projectId) {
        super(JobType.UPDATE_DISCOURSE_PROJECT_TOPIC);
        this.projectId = projectId;
    }

    UpdateDiscourseProjectTopicJob() {
        super(JobType.UPDATE_DISCOURSE_PROJECT_TOPIC);
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    @Override
    public void loadFromProperties() {
        if (getJobProperties() != null && getJobProperties().containsKey("projectId")) {
            projectId = Long.parseLong(getJobProperties().get("projectId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("projectId", projectId + "");
        setJobProperties(properties);
    }

    public static UpdateDiscourseProjectTopicJob loadFromTable(JobTable table) {
        UpdateDiscourseProjectTopicJob job  = new UpdateDiscourseProjectTopicJob();
        job.fromTable(table);
        job.setJobProperties(table.getJobProperties());
        job.loadFromProperties();
        return job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UpdateDiscourseProjectTopicJob that = (UpdateDiscourseProjectTopicJob) o;
        return projectId == that.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId);
    }

    @Override
    public String toString() {
        return "UpdateDiscourseProjectTopicJob{" +
               "createdAt=" + createdAt +
               ", projectId=" + projectId +
               "} " + super.toString();
    }
}
