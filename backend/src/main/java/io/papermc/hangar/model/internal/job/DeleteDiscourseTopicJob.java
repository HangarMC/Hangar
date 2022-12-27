package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.model.db.JobTable;

public class DeleteDiscourseTopicJob extends Job {

    private long topicId;

    public DeleteDiscourseTopicJob(final long topicId) {
        super(JobType.DELETE_DISCOURSE_TOPIC);
        this.topicId = topicId;
    }

    DeleteDiscourseTopicJob() {
        super(JobType.DELETE_DISCOURSE_TOPIC);
    }

    public long getTopicId() {
        return this.topicId;
    }

    public void setTopicId(final long topicId) {
        this.topicId = topicId;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() != null && this.getJobProperties().containsKey("topicId")) {
            this.topicId = Long.parseLong(this.getJobProperties().get("topicId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("topicId", this.topicId + "");
        this.setJobProperties(properties);
    }

    public static DeleteDiscourseTopicJob loadFromTable(final JobTable table) {
        final DeleteDiscourseTopicJob job  = new DeleteDiscourseTopicJob();
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
        final DeleteDiscourseTopicJob that = (DeleteDiscourseTopicJob) o;
        return this.topicId == that.topicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.topicId);
    }

    @Override
    public String toString() {
        return "DeleteDiscourseTopicJob{" +
               "createdAt=" + this.createdAt +
               ", topicId=" + this.topicId +
               "} " + super.toString();
    }
}
