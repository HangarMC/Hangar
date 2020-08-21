package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.ProjectNamespace;

import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class UnhealthyProject {

    private ProjectNamespace namespace;
    private Integer topicId;
    private Integer postId;
    private OffsetDateTime lastUpdated;
    private Visibility visibility;

    public UnhealthyProject() { }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    @Nested("pn")
    public void setNamespace(ProjectNamespace namespace) {
        this.namespace = namespace;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
