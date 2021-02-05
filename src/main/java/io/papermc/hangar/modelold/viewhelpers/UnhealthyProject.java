package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.generated.ProjectNamespace;
import org.jdbi.v3.core.enums.EnumByOrdinal;
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

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    @EnumByOrdinal
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
