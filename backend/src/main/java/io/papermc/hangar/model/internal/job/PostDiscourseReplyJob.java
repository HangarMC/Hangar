package io.papermc.hangar.model.internal.job;

import io.papermc.hangar.model.db.JobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostDiscourseReplyJob extends Job {

    private long projectId;
    private String poster;
    private String content;

    public PostDiscourseReplyJob(final long projectId, final String poster, final String content) {
        super(JobType.POST_DISCOURSE_REPLY);
        this.projectId = projectId;
        this.poster = poster;
        this.content = content;
    }

    PostDiscourseReplyJob() {
        super(JobType.POST_DISCOURSE_REPLY);
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public String getPoster() {
        return this.poster;
    }

    public void setPoster(final String poster) {
        this.poster = poster;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() == null) {
            return;
        }
        if (this.getJobProperties().containsKey("projectId")) {
            this.projectId = Long.parseLong(this.getJobProperties().get("projectId"));
        }
        if (this.getJobProperties().containsKey("poster")) {
            this.poster = this.getJobProperties().get("poster");
        }
        if (this.getJobProperties().containsKey("content")) {
            this.content = this.getJobProperties().get("content");
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("projectId", this.projectId + "");
        properties.put("poster", this.poster);
        properties.put("content", this.content);
        this.setJobProperties(properties);
    }

    public static PostDiscourseReplyJob loadFromTable(final JobTable table) {
        final PostDiscourseReplyJob job = new PostDiscourseReplyJob();
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
        final PostDiscourseReplyJob that = (PostDiscourseReplyJob) o;
        return this.projectId == that.projectId && Objects.equals(this.poster, that.poster) && Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.projectId, this.poster, this.content);
    }

    @Override
    public String toString() {
        return "PostDiscourseReplyJob{" +
            "createdAt=" + this.createdAt +
            ", projectId=" + this.projectId +
            ", poster='" + this.poster + '\'' +
            ", content='" + this.content + '\'' +
            "} " + super.toString();
    }
}
