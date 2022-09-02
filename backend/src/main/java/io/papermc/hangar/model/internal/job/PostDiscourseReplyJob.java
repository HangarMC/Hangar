package io.papermc.hangar.model.internal.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.db.JobTable;

public class PostDiscourseReplyJob extends Job {

    private long projectId;
    private String poster;
    private String content;

    public PostDiscourseReplyJob(long projectId, String poster, String content) {
        super(JobType.POST_DISCOURSE_REPLY);
        this.projectId = projectId;
        this.poster = poster;
        this.content = content;
    }

    PostDiscourseReplyJob() {
        super(JobType.POST_DISCOURSE_REPLY);
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void loadFromProperties() {
        if (getJobProperties() == null) {
            return;
        }
        if (getJobProperties().containsKey("projectId")) {
            this.projectId = Long.parseLong(getJobProperties().get("projectId"));
        }
        if (getJobProperties().containsKey("poster")) {
            this.poster = getJobProperties().get("poster");
        }
        if (getJobProperties().containsKey("content")) {
            this.content = getJobProperties().get("content");
        }
    }

    @Override
    public void saveIntoProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("projectId", projectId + "");
        properties.put("poster", poster);
        properties.put("content", content );
        setJobProperties(properties);
    }

    public static PostDiscourseReplyJob loadFromTable(JobTable table) {
        PostDiscourseReplyJob job  = new PostDiscourseReplyJob();
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
        PostDiscourseReplyJob that = (PostDiscourseReplyJob) o;
        return projectId == that.projectId && Objects.equals(poster, that.poster) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId, poster, content);
    }

    @Override
    public String toString() {
        return "PostDiscourseReplyJob{" +
               "createdAt=" + createdAt +
               ", projectId=" + projectId +
               ", poster='" + poster + '\'' +
               ", content='" + content + '\'' +
               "} " + super.toString();
    }
}
