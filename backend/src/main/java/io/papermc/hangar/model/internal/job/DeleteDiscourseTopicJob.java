package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.model.db.JobTable;

public class DeleteDiscourseTopicJob extends Job {

    private long topicId;

    public DeleteDiscourseTopicJob(long topicId) {
        super(JobType.DELETE_DISCOURSE_TOPIC);
        this.topicId = topicId;
    }

    DeleteDiscourseTopicJob() {
        super(JobType.DELETE_DISCOURSE_TOPIC);
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    @Override
    public void loadFromProperties() {
        if (getJobProperties() != null && getJobProperties().containsKey("topicId")) {
            topicId = Long.parseLong(getJobProperties().get("topicId"));
        }
    }

    @Override
    public void saveIntoProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("topicId", topicId + "");
        setJobProperties(properties);
    }

    public static DeleteDiscourseTopicJob loadFromTable(JobTable table) {
        DeleteDiscourseTopicJob job  = new DeleteDiscourseTopicJob();
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
        DeleteDiscourseTopicJob that = (DeleteDiscourseTopicJob) o;
        return topicId == that.topicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicId);
    }

    @Override
    public String toString() {
        return "DeleteDiscourseTopicJob{" +
               "createdAt=" + createdAt +
               ", topicId=" + topicId +
               "} " + super.toString();
    }
}
